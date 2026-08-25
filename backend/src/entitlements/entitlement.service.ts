import {
  ForbiddenException,
  HttpException,
  Injectable,
  ServiceUnavailableException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { GoogleAuth } from 'google-auth-library';
import { PrismaService } from '../common/prisma/prisma.service';

const LIMITS = {
  GUEST: { saves: 10, imports: 3 },
  FREE: { saves: 20, imports: 5 },
  PLUS: { saves: null, imports: 40 },
} as const;

@Injectable()
export class EntitlementService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly config: ConfigService,
  ) {}

  async getSummary(userId: string) {
    const user = await (this.prisma as any).user.findUniqueOrThrow({ where: { id: userId } });
    const plan = this.effectivePlan(user);
    const [savedCount, pendingCount, socialImportsUsed] = await Promise.all([
      this.prisma.userContent.count({ where: { userId } }),
      this.prisma.ingestion.count({
        where: { userId, status: { in: ['RECEIVED', 'PROCESSING'] } },
      }),
      this.socialImportCount(userId, plan),
    ]);
    const limits = LIMITS[plan];
    const saveCount = savedCount + pendingCount;
    return {
      plan,
      isAnonymous: user.isAnonymous,
      email: user.email,
      saveCount,
      saveLimit: limits.saves,
      socialImportsUsed,
      socialImportLimit: limits.imports,
      accountPromptRecommended: user.isAnonymous && saveCount >= 5,
      plusExpiresAt: user.plusExpiresAt,
    };
  }

  async assertCanSave(userId: string) {
    const summary = await this.getSummary(userId);
    if (summary.saveLimit == null || summary.saveCount < summary.saveLimit) return summary;
    if (summary.isAnonymous) {
      throw new ForbiddenException({
        code: 'ACCOUNT_REQUIRED',
        message: 'Create a free account to keep saving. Your existing thoughts will stay with you.',
        entitlement: summary,
      });
    }
    throw new HttpException(
      {
        code: 'UPGRADE_REQUIRED',
        message: 'You have reached the 20-save Free limit. Upgrade to Plus for unlimited saves.',
        entitlement: summary,
      },
      402,
    );
  }

  async assertCanStartOriginalImport(userId: string) {
    const summary = await this.assertCanSave(userId);
    if (summary.socialImportsUsed < summary.socialImportLimit) return summary;
    if (summary.isAnonymous) {
      throw new ForbiddenException({
        code: 'ACCOUNT_REQUIRED',
        message: 'Create a free account to process more social posts.',
        entitlement: summary,
      });
    }
    throw new HttpException(
      {
        code: 'SOCIAL_IMPORT_LIMIT',
        message:
          summary.plan === 'PLUS'
            ? 'Your 40 monthly AI imports are used. Reused links remain available without consuming quota.'
            : 'Your 5 monthly AI imports are used. Upgrade to Plus for 40 per month.',
        entitlement: summary,
      },
      402,
    );
  }

  async verifyGooglePlaySubscription(userId: string, purchaseToken: string, productId: string) {
    const db = this.prisma as any;
    const credentialsJson = this.config.get<string>('GOOGLE_PLAY_SERVICE_ACCOUNT_JSON');
    const packageName = this.config.get<string>('GOOGLE_PLAY_PACKAGE_NAME') || 'ai.pinglet.app';
    if (!credentialsJson) {
      throw new ServiceUnavailableException({
        code: 'BILLING_NOT_CONFIGURED',
        message: 'Google Play purchase verification is not configured.',
      });
    }

    let credentials: object;
    try {
      credentials = JSON.parse(credentialsJson);
    } catch {
      throw new ServiceUnavailableException('Invalid Google Play service account configuration.');
    }

    const auth = new GoogleAuth({
      credentials,
      scopes: ['https://www.googleapis.com/auth/androidpublisher'],
    });
    const client = await auth.getClient();
    const url =
      `https://androidpublisher.googleapis.com/androidpublisher/v3/applications/` +
      `${encodeURIComponent(packageName)}/purchases/subscriptionsv2/tokens/${encodeURIComponent(purchaseToken)}`;
    const response = await client.request<any>({ url });
    const purchase = response.data;
    const allowedStates = ['SUBSCRIPTION_STATE_ACTIVE', 'SUBSCRIPTION_STATE_IN_GRACE_PERIOD'];
    const item = purchase.lineItems?.find((line: any) => line.productId === productId);
    if (!allowedStates.includes(purchase.subscriptionState) || !item?.expiryTime) {
      throw new ForbiddenException({ code: 'PURCHASE_INVALID', message: 'This subscription is not active.' });
    }
    const expiresAt = new Date(item.expiryTime);
    await db.$transaction([
      db.purchaseEntitlement.upsert({
        where: { purchaseToken },
        create: {
          userId,
          provider: 'GOOGLE_PLAY',
          productId,
          purchaseToken,
          status: purchase.subscriptionState,
          expiresAt,
          rawData: purchase,
        },
        update: { userId, productId, status: purchase.subscriptionState, expiresAt, rawData: purchase },
      }),
      db.user.update({ where: { id: userId }, data: { plan: 'PLUS', plusExpiresAt: expiresAt } }),
    ]);
    return this.getSummary(userId);
  }

  private effectivePlan(user: { isAnonymous: boolean; plan: string; plusExpiresAt: Date | null }) {
    if (user.plan === 'PLUS' && user.plusExpiresAt && user.plusExpiresAt > new Date()) return 'PLUS' as const;
    return user.isAnonymous ? ('GUEST' as const) : ('FREE' as const);
  }

  private socialImportCount(userId: string, plan: 'GUEST' | 'FREE' | 'PLUS') {
    const createdAt = plan === 'GUEST' ? undefined : { gte: new Date(Date.UTC(new Date().getUTCFullYear(), new Date().getUTCMonth(), 1)) };
    return this.prisma.ingestion.count({
      where: {
        userId,
        createdAt,
        processingStage: { not: 'REUSED_EXISTING_EXTRACTION' },
        status: { in: ['RECEIVED', 'PROCESSING', 'READY', 'REJECTED'] },
      },
    });
  }
}

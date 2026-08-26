import {
  ForbiddenException,
  HttpException,
  Injectable,
  ConflictException,
  ServiceUnavailableException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { GoogleAuth } from 'google-auth-library';
import { PrismaService } from '../common/prisma/prisma.service';

const LIMITS = {
  GUEST: { saves: 10, imports: 5 },
  FREE: { saves: 30, imports: 10 },
  PLUS: { saves: null, imports: 50 },
} as const;

@Injectable()
export class EntitlementService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly config: ConfigService,
  ) {}

  async getSummary(userId: string) {
    const user = await (this.prisma as any).user.findUniqueOrThrow({ where: { id: userId } });
    const now = new Date();
    const [purchaseCount, activePurchaseCount] = await Promise.all([
      this.prisma.purchaseEntitlement.count({ where: { userId } }),
      this.prisma.purchaseEntitlement.count({ where: { userId, expiresAt: { gt: now } } }),
    ]);
    const entitlement = this.resolveEntitlement(user, now, activePurchaseCount > 0, purchaseCount > 0);
    const plan = entitlement.plan;
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
      accessExpiresAt: entitlement.accessExpiresAt,
      entitlementSource: entitlement.source,
      trialStatus: entitlement.trialStatus,
      trialEligible: entitlement.trialStatus === 'ELIGIBLE',
      trialStartedAt: user.plusTrialStartedAt,
      trialEndsAt: user.plusTrialEndsAt,
      trialDaysRemaining: entitlement.trialDaysRemaining,
      paidPlansEnabled: this.booleanConfig('GOOGLE_PLAY_BILLING_ENABLED', false),
    };
  }

  private booleanConfig(key: string, fallback: boolean) {
    const value = this.config.get<string>(key);
    if (value === undefined) return fallback;
    return value.trim().toLowerCase() === 'true';
  }

  async startTrial(userId: string) {
    const db = this.prisma as any;
    const user = await db.user.findUniqueOrThrow({ where: { id: userId } });
    if (user.isAnonymous || !user.emailVerifiedAt) {
      throw new ForbiddenException({
        code: 'EMAIL_VERIFICATION_REQUIRED',
        message: 'Verify your email before starting your free Plus access.',
      });
    }

    const now = new Date();
    if (user.plusTrialStartedAt) {
      if (user.plusTrialEndsAt && user.plusTrialEndsAt > now) return this.getSummary(userId);
      throw new ConflictException({ code: 'TRIAL_ALREADY_USED', message: 'This account has already used its free Plus access.' });
    }
    const purchaseCount = await this.prisma.purchaseEntitlement.count({ where: { userId } });
    if (purchaseCount > 0 || (user.plan === 'PLUS' && user.plusExpiresAt && user.plusExpiresAt > now)) {
      throw new ForbiddenException({ code: 'TRIAL_NOT_ELIGIBLE', message: 'This account is not eligible for a Plus trial.' });
    }

    const configuredDays = Number(this.config.get<string>('PLUS_REVERSE_TRIAL_DAYS') || '7');
    const days = Number.isFinite(configuredDays) ? Math.max(1, Math.min(30, Math.floor(configuredDays))) : 7;
    const endsAt = new Date(now.getTime() + days * 24 * 60 * 60 * 1000);
    const claimed = await db.user.updateMany({
      where: { id: userId, plusTrialStartedAt: null, isAnonymous: false, emailVerifiedAt: { not: null } },
      data: { plusTrialStartedAt: now, plusTrialEndsAt: endsAt },
    });
    if (claimed.count !== 1) {
      const summary = await this.getSummary(userId);
      if (summary.trialStatus === 'ACTIVE') return summary;
      throw new ConflictException({ code: 'TRIAL_ALREADY_USED', message: 'This account has already used its free Plus access.' });
    }
    return this.getSummary(userId);
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
        message: 'You have reached the 30-save Free limit. Upgrade to Plus for unlimited saves.',
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
            ? 'Your 50 monthly AI imports are used. Reused links remain available without consuming quota.'
            : 'Your 10 monthly AI imports are used. Upgrade to Plus for 50 per month.',
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

  private resolveEntitlement(
    user: {
      isAnonymous: boolean;
      emailVerifiedAt: Date | null;
      plan: string;
      plusExpiresAt: Date | null;
      plusTrialStartedAt: Date | null;
      plusTrialEndsAt: Date | null;
    },
    now: Date,
    hasActivePurchase: boolean,
    hasPurchaseHistory: boolean,
  ) {
    const paidActive = user.plan === 'PLUS' && !!user.plusExpiresAt && user.plusExpiresAt > now;
    const trialActive = !paidActive && !!user.plusTrialEndsAt && user.plusTrialEndsAt > now;
    const trialDaysRemaining = trialActive
      ? Math.max(1, Math.ceil((user.plusTrialEndsAt!.getTime() - now.getTime()) / (24 * 60 * 60 * 1000)))
      : 0;
    const trialStatus = trialActive
      ? 'ACTIVE'
      : user.plusTrialStartedAt
        ? 'EXPIRED'
        : !user.isAnonymous && !!user.emailVerifiedAt && !hasPurchaseHistory && !paidActive
          ? 'ELIGIBLE'
          : 'INELIGIBLE';
    return {
      plan: paidActive || trialActive ? ('PLUS' as const) : user.isAnonymous ? ('GUEST' as const) : ('FREE' as const),
      source: paidActive ? (hasActivePurchase ? 'GOOGLE_PLAY' : 'ADMIN') : trialActive ? 'TRIAL' : 'NONE',
      accessExpiresAt: paidActive ? user.plusExpiresAt : trialActive ? user.plusTrialEndsAt : null,
      trialStatus,
      trialDaysRemaining,
    };
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

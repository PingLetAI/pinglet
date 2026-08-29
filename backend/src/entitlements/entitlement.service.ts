import {
  ForbiddenException,
  HttpException,
  Injectable,
  ConflictException,
  ServiceUnavailableException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { Environment, SignedDataVerifier } from '@apple/app-store-server-library';
import { readFileSync } from 'fs';
import { join } from 'path';
import { GoogleAuth } from 'google-auth-library';
import { PrismaService } from '../common/prisma/prisma.service';

const LIMITS = {
  GUEST: { saves: 10, imports: 5 },
  FREE: { saves: 30, imports: 10 },
  PLUS: { saves: null, imports: 50 },
} as const;

const APPLE_PRODUCT_IDS = new Set(['ai.pinglet.app.plus.monthly', 'ai.pinglet.app.plus.annual']);
const ACTIVE_PURCHASE_STATES = ['ACTIVE', 'SUBSCRIPTION_STATE_ACTIVE', 'SUBSCRIPTION_STATE_IN_GRACE_PERIOD'];

@Injectable()
export class EntitlementService {
  private appleRoots?: Buffer[];
  constructor(
    private readonly prisma: PrismaService,
    private readonly config: ConfigService,
  ) {}

  async getSummary(userId: string) {
    const user = await (this.prisma as any).user.findUniqueOrThrow({ where: { id: userId } });
    const now = new Date();
    const [purchaseCount, activePurchase] = await Promise.all([
      this.prisma.purchaseEntitlement.count({ where: { userId } }),
      this.prisma.purchaseEntitlement.findFirst({
        where: { userId, expiresAt: { gt: now }, status: { in: ACTIVE_PURCHASE_STATES } },
        orderBy: { expiresAt: 'desc' },
      }),
    ]);
    const entitlement = this.resolveEntitlement(user, now, activePurchase?.provider, purchaseCount > 0);
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
      paidPlansEnabled:
        this.booleanConfig('PAID_PLANS_ENABLED', false) ||
        this.booleanConfig('GOOGLE_PLAY_BILLING_ENABLED', false) ||
        this.booleanConfig('APPLE_STORE_BILLING_ENABLED', false),
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

  async verifyAppleSubscription(userId: string, signedTransaction: string) {
    if (!this.booleanConfig('APPLE_STORE_BILLING_ENABLED', false)) {
      throw new ServiceUnavailableException({ code: 'BILLING_NOT_CONFIGURED', message: 'Apple purchases are not enabled.' });
    }
    const transaction = await this.verifyAppleTransaction(signedTransaction);
    return this.applyAppleTransaction(transaction, userId);
  }

  async processAppleNotification(signedPayload: string) {
    const environment = this.environmentHint(signedPayload, true);
    const notification = await this.appleVerifier(environment).verifyAndDecodeNotification(signedPayload);
    const signedTransaction = notification.data?.signedTransactionInfo;
    if (!signedTransaction) return;
    const transaction = await this.appleVerifier(environment).verifyAndDecodeTransaction(signedTransaction);
    await this.applyAppleTransaction(transaction);
  }

  private async verifyAppleTransaction(signedTransaction: string) {
    try {
      const environment = this.environmentHint(signedTransaction, false);
      return await this.appleVerifier(environment).verifyAndDecodeTransaction(signedTransaction);
    } catch {
      throw new ForbiddenException({ code: 'PURCHASE_INVALID', message: 'This Apple subscription could not be verified.' });
    }
  }

  private async applyAppleTransaction(transaction: any, requestedUserId?: string) {
    const productId = transaction.productId;
    const originalTransactionId = transaction.originalTransactionId;
    const expiresDate = transaction.expiresDate;
    if (!APPLE_PRODUCT_IDS.has(productId) || !originalTransactionId || !expiresDate) {
      throw new ForbiddenException({ code: 'PURCHASE_INVALID', message: 'This is not a PingLet Plus subscription.' });
    }
    const purchaseToken = `apple:${originalTransactionId}`;
    const existing = await this.prisma.purchaseEntitlement.findUnique({ where: { purchaseToken } });
    const userId = requestedUserId || existing?.userId;
    if (!userId) return this.getSummaryForMissingNotification();
    const expiresAt = new Date(expiresDate);
    const active = !transaction.revocationDate && expiresAt > new Date();
    const status = transaction.revocationDate ? 'REVOKED' : active ? 'ACTIVE' : 'EXPIRED';
    const db = this.prisma as any;
    await db.purchaseEntitlement.upsert({
      where: { purchaseToken },
      create: { userId, provider: 'APPLE_APP_STORE', productId, purchaseToken, status, expiresAt, rawData: transaction },
      update: { userId, productId, status, expiresAt, rawData: transaction },
    });
    const latest = await db.purchaseEntitlement.findFirst({
      where: { userId, expiresAt: { gt: new Date() }, status: { in: ACTIVE_PURCHASE_STATES } },
      orderBy: { expiresAt: 'desc' },
    });
    await db.user.update({
      where: { id: userId },
      data: { plan: latest ? 'PLUS' : 'FREE', plusExpiresAt: latest?.expiresAt || null },
    });
    if (requestedUserId && !active) {
      throw new ForbiddenException({ code: 'PURCHASE_INVALID', message: 'This Apple subscription is not active.' });
    }
    return this.getSummary(userId);
  }

  private getSummaryForMissingNotification() {
    return undefined;
  }

  private appleVerifier(environment: Environment) {
    const bundleId = this.config.get<string>('APPLE_IAP_BUNDLE_ID') || 'ai.pinglet.app';
    const appAppleId = Number(this.config.get<string>('APPLE_IAP_APP_ID'));
    if (environment === Environment.PRODUCTION && !Number.isFinite(appAppleId)) {
      throw new ServiceUnavailableException({ code: 'BILLING_NOT_CONFIGURED', message: 'Apple production verification is not configured.' });
    }
    return new SignedDataVerifier(
      this.appleRootCertificates(),
      this.booleanConfig('APPLE_IAP_ONLINE_CHECKS', true),
      environment,
      bundleId,
      environment === Environment.PRODUCTION ? appAppleId : undefined,
    );
  }

  private appleRootCertificates() {
    if (this.appleRoots) return this.appleRoots;
    const directory = this.config.get<string>('APPLE_IAP_CERTIFICATES_DIR') || join(process.cwd(), 'certs', 'apple');
    try {
      this.appleRoots = ['AppleIncRootCertificate.cer', 'AppleRootCA-G2.cer', 'AppleRootCA-G3.cer'].map((name) =>
        readFileSync(join(directory, name)),
      );
      return this.appleRoots;
    } catch {
      throw new ServiceUnavailableException({ code: 'BILLING_NOT_CONFIGURED', message: 'Apple verification certificates are missing.' });
    }
  }

  private environmentHint(jws: string, notification: boolean): Environment {
    try {
      const payload = JSON.parse(Buffer.from(jws.split('.')[1], 'base64url').toString('utf8'));
      const value = notification ? payload.data?.environment : payload.environment;
      return value === Environment.PRODUCTION ? Environment.PRODUCTION : Environment.SANDBOX;
    } catch {
      throw new ForbiddenException({ code: 'PURCHASE_INVALID', message: 'Invalid Apple signed payload.' });
    }
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
    activePurchaseProvider: string | undefined,
    hasPurchaseHistory: boolean,
  ) {
    const paidActive = !!activePurchaseProvider || (user.plan === 'PLUS' && !!user.plusExpiresAt && user.plusExpiresAt > now);
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
      source: paidActive ? (activePurchaseProvider || 'ADMIN') : trialActive ? 'TRIAL' : 'NONE',
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

import {
  BadRequestException,
  HttpException,
  Injectable,
  Logger,
  OnModuleDestroy,
  ServiceUnavailableException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { createHash, randomInt, timingSafeEqual } from 'crypto';
import Redis from 'ioredis';
import * as nodemailer from 'nodemailer';
import type { Transporter } from 'nodemailer';
import { PrismaService } from '../common/prisma/prisma.service';
import { AuthService } from './auth.service';

@Injectable()
export class EmailOtpService implements OnModuleDestroy {
  private readonly logger = new Logger(EmailOtpService.name);
  private readonly redis: Redis;
  private readonly mailer?: Transporter;

  constructor(private readonly prisma: PrismaService, private readonly config: ConfigService, private readonly auth: AuthService) {
    this.redis = new Redis(this.config.get<string>('REDIS_URL') || 'redis://localhost:6379');
    const user = this.config.get<string>('SMTP_USER');
    const password = this.config.get<string>('SMTP_PASSWORD');
    if (user && password) {
      const port = Number(this.config.get<string>('SMTP_PORT') || '465');
      this.mailer = nodemailer.createTransport({
        host: this.config.get<string>('SMTP_HOST') || 'smtppro.zoho.com',
        port,
        secure: this.booleanConfig('SMTP_SECURE', port === 465),
        auth: { user, pass: password },
        connectionTimeout: 15_000,
        greetingTimeout: 10_000,
        socketTimeout: 30_000,
      });
    }
  }

  async request(userId: string, rawEmail: string) {
    const email = rawEmail.trim().toLowerCase();
    const cooldownKey = `email-otp-cooldown:${userId}:${email}`;
    const acquired = await this.redis.set(cooldownKey, '1', 'EX', 60, 'NX');
    if (!acquired) throw new HttpException('Wait one minute before requesting another code.', 429);
    const reviewEmail = this.config.get<string>('PLAY_REVIEW_EMAIL')?.trim().toLowerCase();
    const reviewCode = this.config.get<string>('PLAY_REVIEW_OTP')?.trim();
    const isReviewer = email === reviewEmail && /^\d{6}$/.test(reviewCode || '');
    const code = isReviewer ? reviewCode! : randomInt(100000, 1000000).toString();
    const payload = JSON.stringify({ hash: this.hash(userId, email, code), attempts: 0 });
    const otpKey = `email-otp:${userId}:${email}`;
    try {
      if (!isReviewer) await this.send(email, code);
      await this.redis.set(otpKey, payload, 'EX', 600);
    } catch (error) {
      await this.redis.del(cooldownKey, otpKey);
      throw error;
    }
    const response: Record<string, unknown> = { sent: true, expiresInSeconds: 600 };
    if (this.booleanConfig('EMAIL_OTP_DEV_MODE', false)) response.devCode = code;
    return response;
  }

  async verify(userId: string, deviceId: string, installationId: string, rawEmail: string, code: string) {
    const db = this.prisma as any;
    const email = rawEmail.trim().toLowerCase();
    await this.consumeCode(userId, email, code);
    const current = await db.user.findUnique({ where: { id: userId } });
    if (!current) throw new BadRequestException('This session is no longer available.');
    const owner = await db.user.findUnique({ where: { email } });
    let accountId = userId;
    if (owner && owner.id !== userId) {
      if (current.isAnonymous) await this.mergeAnonymousAccount(userId, owner.id, deviceId);
      else {
        await db.$transaction([
          db.refreshToken.deleteMany({ where: { deviceId } }),
          db.device.update({ where: { id: deviceId }, data: { userId: owner.id } }),
        ]);
      }
      accountId = owner.id;
    } else {
      await db.user.update({
        where: { id: userId },
        data: { email, emailVerifiedAt: new Date(), isAnonymous: false, plan: current.plan === 'GUEST' ? 'FREE' : current.plan },
      });
    }
    const account = await db.user.findUniqueOrThrow({ where: { id: accountId } });
    const session = await this.auth.issueSession(accountId, deviceId, installationId);
    return { verified: true, email, plan: account.plan, ...session };
  }

  async deleteAccount(userId: string, rawEmail: string, code: string) {
    const db = this.prisma as any;
    const email = rawEmail.trim().toLowerCase();
    const user = await db.user.findUnique({ where: { id: userId } });
    if (!user || user.isAnonymous || !user.email || user.email.toLowerCase() !== email) {
      throw new BadRequestException('Enter the verified email address for this account.');
    }
    await this.consumeCode(userId, email, code);
    await db.$transaction(async (tx: any) => {
      const owned = await tx.contentItem.findMany({ where: { ownerUserId: userId }, select: { id: true } });
      const ownedIds = owned.map((item: { id: string }) => item.id);
      await tx.contentItem.updateMany({ where: { ownerUserId: userId }, data: { ownerUserId: null } });
      await tx.user.delete({ where: { id: userId } });
      if (ownedIds.length) {
        await tx.contentItem.deleteMany({
          where: { id: { in: ownedIds }, visibility: 'PRIVATE', userContents: { none: {} }, ingestions: { none: {} } },
        });
      }
    });
    return { deleted: true };
  }

  onModuleDestroy() {
    this.redis.disconnect();
  }

  private hash(userId: string, email: string, code: string) {
    const secret = this.config.get<string>('OTP_SECRET') || this.config.get<string>('JWT_SECRET') || 'linger-dev-otp';
    return createHash('sha256').update(`${secret}:${userId}:${email}:${code}`).digest('hex');
  }

  private async consumeCode(userId: string, email: string, code: string) {
    const key = `email-otp:${userId}:${email}`;
    const stored = await this.redis.get(key);
    if (!stored) throw new BadRequestException({ code: 'OTP_EXPIRED', message: 'That code expired. Request a new one.' });
    const payload = JSON.parse(stored) as { hash: string; attempts: number };
    if (payload.attempts >= 5) throw new HttpException('Too many incorrect attempts. Request a new code.', 429);
    const actual = Buffer.from(this.hash(userId, email, code));
    const expected = Buffer.from(payload.hash);
    if (actual.length !== expected.length || !timingSafeEqual(actual, expected)) {
      await this.redis.set(key, JSON.stringify({ ...payload, attempts: payload.attempts + 1 }), 'KEEPTTL');
      throw new BadRequestException({ code: 'OTP_INVALID', message: 'That code is incorrect.' });
    }
    await this.redis.del(key);
  }

  private async mergeAnonymousAccount(sourceUserId: string, targetUserId: string, deviceId: string) {
    const db = this.prisma as any;
    await db.$transaction(async (tx: any) => {
      const sourceContents = await tx.userContent.findMany({ where: { userId: sourceUserId } });
      for (const item of sourceContents) {
        const existing = await tx.userContent.findUnique({ where: { userId_contentItemId: { userId: targetUserId, contentItemId: item.contentItemId } } });
        await tx.userContent.upsert({
          where: { userId_contentItemId: { userId: targetUserId, contentItemId: item.contentItemId } },
          create: { userId: targetUserId, contentItemId: item.contentItemId, favorite: item.favorite, archived: item.archived, priority: item.priority },
          update: { favorite: Boolean(existing?.favorite || item.favorite), archived: Boolean(existing?.archived && item.archived), priority: Math.max(existing?.priority || 0, item.priority) },
        });
      }
      const favorites = await tx.favorite.findMany({ where: { userId: sourceUserId }, select: { contentItemId: true } });
      for (const favorite of favorites) {
        await tx.favorite.upsert({ where: { userId_contentItemId: { userId: targetUserId, contentItemId: favorite.contentItemId } }, create: { userId: targetUserId, contentItemId: favorite.contentItemId }, update: {} });
      }
      const catalogPrefs = await tx.userCatalogPreference.findMany({ where: { userId: sourceUserId } });
      for (const preference of catalogPrefs) {
        await tx.userCatalogPreference.upsert({ where: { userId_catalogId: { userId: targetUserId, catalogId: preference.catalogId } }, create: { userId: targetUserId, catalogId: preference.catalogId, enabled: preference.enabled, weight: preference.weight }, update: {} });
      }
      await tx.ingestion.updateMany({ where: { userId: sourceUserId }, data: { userId: targetUserId } });
      await tx.event.updateMany({ where: { userId: sourceUserId }, data: { userId: targetUserId } });
      await tx.pendingAction.updateMany({ where: { userId: sourceUserId }, data: { userId: targetUserId } });
      await tx.purchaseEntitlement.updateMany({ where: { userId: sourceUserId }, data: { userId: targetUserId } });
      await tx.contentItem.updateMany({ where: { ownerUserId: sourceUserId }, data: { ownerUserId: targetUserId } });
      await tx.refreshToken.deleteMany({ where: { deviceId } });
      await tx.device.update({ where: { id: deviceId }, data: { userId: targetUserId } });
      await tx.user.delete({ where: { id: sourceUserId } });
    });
  }

  private async send(email: string, code: string) {
    if (!this.mailer) {
      if (this.booleanConfig('EMAIL_OTP_DEV_MODE', false)) {
        this.logger.warn('SMTP is not configured; returning the OTP because EMAIL_OTP_DEV_MODE is enabled.');
        return;
      }
      throw new ServiceUnavailableException('Email delivery is not configured.');
    }

    const from = this.config.get<string>('EMAIL_FROM') || 'PingLet <hello@pinglet.ai>';
    const replyTo = this.config.get<string>('EMAIL_REPLY_TO') || 'hello@pinglet.ai';
    try {
      await this.mailer.sendMail({
        from,
        replyTo,
        to: email,
        subject: 'Your PingLet verification code',
        text: `Your PingLet verification code is ${code}. It expires in 10 minutes. If you did not request this code, you can ignore this email.`,
        html: this.otpEmail(code),
      });
    } catch (error) {
      const smtpCode = typeof error === 'object' && error && 'code' in error ? String(error.code) : 'UNKNOWN';
      this.logger.error(`OTP email delivery failed (SMTP code: ${smtpCode}).`);
      throw new ServiceUnavailableException('We could not send the verification email. Please try again.');
    }
  }

  private booleanConfig(key: string, fallback: boolean) {
    const value = this.config.get<string>(key);
    if (value === undefined) return fallback;
    return value.trim().toLowerCase() === 'true';
  }

  private otpEmail(code: string) {
    return `<!doctype html>
<html lang="en">
  <body style="margin:0;background:#f4f2eb;color:#17211b;font-family:Arial,sans-serif">
    <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#f4f2eb;padding:32px 16px">
      <tr><td align="center">
        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:520px;background:#fff;border:1px solid #dedbd1;border-radius:20px;overflow:hidden">
          <tr><td style="padding:28px 32px 10px;font-size:20px;font-weight:700;letter-spacing:-.3px">PingLet</td></tr>
          <tr><td style="padding:14px 32px 8px;font-size:25px;font-weight:700">Verify your email</td></tr>
          <tr><td style="padding:0 32px 22px;color:#59635d;font-size:15px;line-height:1.6">Enter this code in PingLet to finish securing your account.</td></tr>
          <tr><td align="center" style="padding:0 32px 24px"><div style="background:#e8f2ec;border-radius:14px;padding:18px;font-size:32px;font-weight:700;letter-spacing:8px;color:#173f2c">${code}</div></td></tr>
          <tr><td style="padding:0 32px 30px;color:#68716c;font-size:13px;line-height:1.6">This code expires in 10 minutes. If you did not request it, you can safely ignore this email.</td></tr>
        </table>
      </td></tr>
    </table>
  </body>
</html>`;
  }
}

import { BadRequestException, ConflictException, HttpException, Injectable, OnModuleDestroy } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { createHash, randomInt, timingSafeEqual } from 'crypto';
import Redis from 'ioredis';
import { PrismaService } from '../common/prisma/prisma.service';

@Injectable()
export class EmailOtpService implements OnModuleDestroy {
  private readonly redis: Redis;

  constructor(private readonly prisma: PrismaService, private readonly config: ConfigService) {
    this.redis = new Redis(this.config.get<string>('REDIS_URL') || 'redis://localhost:6379');
  }

  async request(userId: string, rawEmail: string) {
    const email = rawEmail.trim().toLowerCase();
    const cooldownKey = `email-otp-cooldown:${userId}:${email}`;
    if (await this.redis.get(cooldownKey)) throw new HttpException('Wait one minute before requesting another code.', 429);
    const code = randomInt(100000, 1000000).toString();
    const payload = JSON.stringify({ hash: this.hash(userId, email, code), attempts: 0 });
    await this.redis.set(`email-otp:${userId}:${email}`, payload, 'EX', 600);
    await this.redis.set(cooldownKey, '1', 'EX', 60);
    await this.send(email, code);
    const response: Record<string, unknown> = { sent: true, expiresInSeconds: 600 };
    if ((this.config.get<string>('NODE_ENV') || 'development') !== 'production') response.devCode = code;
    return response;
  }

  async verify(userId: string, rawEmail: string, code: string) {
    const db = this.prisma as any;
    const email = rawEmail.trim().toLowerCase();
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
    const owner = await db.user.findUnique({ where: { email } });
    if (owner && owner.id !== userId) throw new ConflictException('That email is already linked to another account.');
    await db.user.update({
      where: { id: userId },
      data: { email, emailVerifiedAt: new Date(), isAnonymous: false, plan: 'FREE' },
    });
    await this.redis.del(key);
    return { verified: true, email, plan: 'FREE' };
  }

  onModuleDestroy() {
    this.redis.disconnect();
  }

  private hash(userId: string, email: string, code: string) {
    const secret = this.config.get<string>('OTP_SECRET') || this.config.get<string>('JWT_SECRET') || 'linger-dev-otp';
    return createHash('sha256').update(`${secret}:${userId}:${email}:${code}`).digest('hex');
  }

  private async send(email: string, code: string) {
    const apiKey = this.config.get<string>('RESEND_API_KEY');
    if (!apiKey) {
      if ((this.config.get<string>('NODE_ENV') || 'development') === 'production') {
        throw new BadRequestException('Email delivery is not configured.');
      }
      console.log(`[email-otp] ${email}: ${code}`);
      return;
    }
    const response = await fetch('https://api.resend.com/emails', {
      method: 'POST',
      headers: { Authorization: `Bearer ${apiKey}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({
        from: this.config.get<string>('EMAIL_FROM') || 'Linger <hello@linger.app>',
        to: [email],
        subject: 'Your Linger verification code',
        html: `<p>Your Linger code is <strong>${code}</strong>.</p><p>It expires in 10 minutes.</p>`,
      }),
    });
    if (!response.ok) throw new BadRequestException('We could not send the verification email.');
  }
}

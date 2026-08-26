import { Injectable, Logger, OnModuleDestroy } from '@nestjs/common';
import Redis from 'ioredis';

@Injectable()
export class RateLimitService implements OnModuleDestroy {
  private readonly logger = new Logger(RateLimitService.name);
  private readonly redis = new Redis(process.env.REDIS_URL || 'redis://localhost:6379', {
    enableOfflineQueue: false,
    maxRetriesPerRequest: 1,
    connectTimeout: 2_000,
  });
  private lastRedisWarningAt = 0;

  constructor() {
    this.redis.on('error', () => {
      const now = Date.now();
      if (now - this.lastRedisWarningAt > 60_000) {
        this.lastRedisWarningAt = now;
        this.logger.warn('Rate-limit storage is temporarily unavailable.');
      }
    });
  }

  async consume(key: string, limit: number, windowSeconds: number) {
    const script = `
      local count = redis.call('INCR', KEYS[1])
      if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end
      local ttl = redis.call('TTL', KEYS[1])
      return {count, ttl}
    `;
    const result = await this.redis.eval(script, 1, key, windowSeconds) as [number, number];
    return { allowed: Number(result[0]) <= limit, remaining: Math.max(0, limit - Number(result[0])), retryAfter: Math.max(1, Number(result[1])) };
  }

  async onModuleDestroy() {
    this.redis.disconnect();
  }
}

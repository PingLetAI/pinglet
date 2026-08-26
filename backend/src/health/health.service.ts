import { Injectable, OnModuleDestroy, ServiceUnavailableException } from '@nestjs/common';
import Redis from 'ioredis';
import { PrismaService } from '../common/prisma/prisma.service';

@Injectable()
export class HealthService implements OnModuleDestroy {
  private readonly redis = new Redis(process.env.REDIS_URL || 'redis://localhost:6379', {
    enableOfflineQueue: false,
    maxRetriesPerRequest: 1,
    connectTimeout: 2_000,
  });

  constructor(private readonly prisma: PrismaService) {
    this.redis.on('error', () => undefined);
  }

  live() {
    return { status: 'ok', service: 'pinglet-api', timestamp: new Date().toISOString() };
  }

  async ready() {
    const checks = await Promise.allSettled([
      this.withTimeout(this.prisma.$queryRaw`SELECT 1`, 3_000),
      this.withTimeout(this.redis.ping(), 3_000),
    ]);
    const database = checks[0].status === 'fulfilled';
    const redis = checks[1].status === 'fulfilled' && checks[1].value === 'PONG';
    if (!database || !redis) {
      throw new ServiceUnavailableException({ message: 'Service dependencies are unavailable', checks: { database, redis } });
    }
    return { status: 'ready', checks: { database, redis }, timestamp: new Date().toISOString() };
  }

  async onModuleDestroy() {
    this.redis.disconnect();
  }

  private withTimeout<T>(operation: Promise<T>, milliseconds: number): Promise<T> {
    return Promise.race([
      operation,
      new Promise<T>((_, reject) => setTimeout(() => reject(new Error('Health check timed out')), milliseconds)),
    ]);
  }
}

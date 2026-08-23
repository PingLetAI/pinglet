import { Injectable, OnModuleDestroy } from '@nestjs/common';
import { Queue } from 'bullmq';
import IORedis from 'ioredis';

export const INGESTION_QUEUE = 'social-ingestions';

@Injectable()
export class IngestionQueueService implements OnModuleDestroy {
  private readonly connection = new IORedis(process.env.REDIS_URL || 'redis://localhost:6379', {
    maxRetriesPerRequest: null,
  });
  private readonly queue = new Queue(INGESTION_QUEUE, { connection: this.connection });

  enqueue(ingestionId: string) {
    return this.queue.add(
      'process-social-post',
      { ingestionId },
      {
        jobId: ingestionId,
        attempts: 3,
        backoff: { type: 'exponential', delay: 5000 },
        removeOnComplete: 500,
        removeOnFail: 1000,
      },
    );
  }

  async onModuleDestroy() {
    await this.queue.close();
    await this.connection.quit();
  }
}

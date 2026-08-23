import { Logger } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { Worker } from 'bullmq';
import IORedis from 'ioredis';
import { AppModule } from './app.module';
import { INGESTION_QUEUE } from './ingestion/ingestion-queue.service';
import { IngestionProcessor } from './ingestion/ingestion.processor';

async function bootstrap() {
  const app = await NestFactory.createApplicationContext(AppModule);
  const processor = app.get(IngestionProcessor);
  const connection = new IORedis(process.env.REDIS_URL || 'redis://localhost:6379', { maxRetriesPerRequest: null });
  const worker = new Worker(
    INGESTION_QUEUE,
    (job) => processor.process(job.data.ingestionId),
    { connection, concurrency: Number(process.env.INGESTION_CONCURRENCY || 2) },
  );
  worker.on('completed', (job) => Logger.log(`Completed ingestion ${job.id}`, 'IngestionWorker'));
  worker.on('failed', (job, error) => Logger.error(`Failed ingestion ${job?.id}: ${error.message}`, '', 'IngestionWorker'));

  const shutdown = async () => {
    await worker.close();
    await connection.quit();
    await app.close();
    process.exit(0);
  };
  process.on('SIGTERM', shutdown);
  process.on('SIGINT', shutdown);
}

bootstrap().catch((error) => {
  Logger.error(error, '', 'IngestionWorker');
  process.exit(1);
});

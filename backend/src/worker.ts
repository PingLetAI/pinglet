import { Logger } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { Worker } from 'bullmq';
import IORedis from 'ioredis';
import { AppModule } from './app.module';
import { INGESTION_QUEUE } from './ingestion/ingestion-queue.service';
import { IngestionProcessor } from './ingestion/ingestion.processor';
import { writeFile } from 'fs/promises';
import { validateProductionEnvironment } from './common/config/production-config';

async function bootstrap() {
  validateProductionEnvironment();
  const app = await NestFactory.createApplicationContext(AppModule);
  const processor = app.get(IngestionProcessor);
  const connection = new IORedis(process.env.REDIS_URL || 'redis://localhost:6379', { maxRetriesPerRequest: null });
  const worker = new Worker(
    INGESTION_QUEUE,
    (job) => processor.process(job.data.ingestionId),
    { connection, concurrency: Number(process.env.INGESTION_CONCURRENCY || 2) },
  );
  const heartbeat = async () => {
    if (connection.status === 'ready') await writeFile('/tmp/pinglet-worker-heartbeat', Date.now().toString());
  };
  await connection.ping();
  await heartbeat();
  const heartbeatTimer = setInterval(() => heartbeat().catch(() => undefined), 15_000);
  heartbeatTimer.unref();
  worker.on('completed', (job) => Logger.log(`Completed ingestion ${job.id}`, 'IngestionWorker'));
  worker.on('failed', (job, error) => Logger.error(`Failed ingestion ${job?.id}: ${error.message}`, '', 'IngestionWorker'));

  const shutdown = async () => {
    clearInterval(heartbeatTimer);
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

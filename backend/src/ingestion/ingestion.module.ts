import { Module } from '@nestjs/common';
import { IngestionController } from './ingestion.controller';
import { IngestionService } from './ingestion.service';
import { PrismaService } from '../common/prisma/prisma.service';
import { ContentModule } from '../content/content.module';
import { IngestionQueueService } from './ingestion-queue.service';
import { IngestionProcessor } from './ingestion.processor';
import { MediaRunnerService } from './media-runner.service';
import { OpenAiExtractionService } from './openai-extraction.service';
import { EntitlementModule } from '../entitlements/entitlement.module';

@Module({
  imports: [ContentModule, EntitlementModule],
  controllers: [IngestionController],
  providers: [
    IngestionService,
    IngestionQueueService,
    IngestionProcessor,
    MediaRunnerService,
    OpenAiExtractionService,
    PrismaService,
  ],
  exports: [IngestionProcessor],
})
export class IngestionModule {}

import { Module } from '@nestjs/common';
import { ContentController } from './content.controller';
import { ContentService } from './content.service';
import { PrismaService } from '../common/prisma/prisma.service';
import { EntitlementModule } from '../entitlements/entitlement.module';

@Module({
  imports: [EntitlementModule],
  controllers: [ContentController],
  providers: [ContentService, PrismaService],
  exports: [ContentService],
})
export class ContentModule {}

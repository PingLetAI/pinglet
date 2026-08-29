import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { PrismaService } from '../common/prisma/prisma.service';
import { AppStoreNotificationController, EntitlementController } from './entitlement.controller';
import { EntitlementService } from './entitlement.service';

@Module({
  imports: [ConfigModule],
  controllers: [EntitlementController, AppStoreNotificationController],
  providers: [EntitlementService, PrismaService],
  exports: [EntitlementService],
})
export class EntitlementModule {}

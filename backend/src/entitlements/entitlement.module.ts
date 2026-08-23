import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { PrismaService } from '../common/prisma/prisma.service';
import { EntitlementController } from './entitlement.controller';
import { EntitlementService } from './entitlement.service';

@Module({
  imports: [ConfigModule],
  controllers: [EntitlementController],
  providers: [EntitlementService, PrismaService],
  exports: [EntitlementService],
})
export class EntitlementModule {}

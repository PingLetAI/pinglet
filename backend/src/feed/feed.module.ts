import { Module } from '@nestjs/common';
import { FeedController } from './feed.controller';
import { FeedService } from './feed.service';
import { PrismaService } from '../common/prisma/prisma.service';

@Module({
  controllers: [FeedController],
  providers: [FeedService, PrismaService],
})
export class FeedModule {}

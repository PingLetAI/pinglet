import { Injectable } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';

@Injectable()
export class QueueService {
  constructor(private readonly prisma: PrismaService) {}

  async replaceQueueForUser(userId: string, contentItemIds: string[]) {
    await this.prisma.queueItem.deleteMany();
    if (contentItemIds.length === 0) return { inserted: 0 };

    await this.prisma.queueItem.createMany({
      data: contentItemIds.map((contentItemId, idx) => ({
        contentItemId,
        slotIndex: idx,
        source: 'SYSTEM',
      } as any)),
      skipDuplicates: true,
    });

    return { inserted: contentItemIds.length };
  }
}

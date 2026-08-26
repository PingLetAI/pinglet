import { Injectable } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';

@Injectable()
export class EventsService {
  constructor(private readonly prisma: PrismaService) {}

  async batchCreate(userId: string, events: Array<{ type: string; contentItemId?: string; surface: string; timestamp?: string; metadata?: string }>) {
    const data = events
      .filter((event) => event.type && event.surface)
      .map((event) => ({
        userId,
        type: event.type as any,
        contentItemId: event.contentItemId,
        surface: event.surface as any,
        timestamp: event.timestamp ? new Date(event.timestamp) : new Date(),
        metadata: event.metadata?.slice(0, 500),
      }));

    if (data.length === 0) return { received: 0, created: 0 };

    await this.prisma.event.createMany({ data });
    return { received: data.length, created: data.length };
  }
}

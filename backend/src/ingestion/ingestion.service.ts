import { BadRequestException, Injectable } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';

@Injectable()
export class IngestionService {
  constructor(private readonly prisma: PrismaService) {}

  async ingestText(userId: string, rawText: string) {
    if (!rawText?.trim()) throw new BadRequestException('text required');
    return this.prisma.ingestion.create({
      data: {
        userId,
        type: 'TEXT',
        rawText,
        status: 'READY',
      },
    });
  }

  async getAdminReadyRows(limit = 100) {
    return this.prisma.ingestion.findMany({
      where: { status: 'READY' },
      take: limit,
      orderBy: { createdAt: 'desc' },
    });
  }
}

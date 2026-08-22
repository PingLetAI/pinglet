import { Injectable } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';

@Injectable()
export class CatalogsService {
  constructor(private readonly prisma: PrismaService) {}

  list() {
    return this.prisma.catalog.findMany({
      where: { isActive: true },
      orderBy: { name: 'asc' },
    });
  }

  async listItemsById(id: string) {
    return this.prisma.catalogItem.findMany({
      where: { catalogId: id },
      orderBy: { priority: 'desc' },
      include: {
        contentItem: {
          include: {
            categories: {
              include: { category: true },
            },
          },
        },
      },
    });
  }
}

import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';

@Injectable()
export class ContentService {
  constructor(private readonly prisma: PrismaService) {}

  async listUserContent(userId: string, includeArchived = false) {
    return this.prisma.userContent.findMany({
      where: {
        userId,
        archived: includeArchived ? undefined : false,
      },
      orderBy: { createdAt: 'desc' },
      include: {
        contentItem: {
          include: {
            categories: { include: { category: true } },
            favorites: true,
          },
        },
      },
    });
  }

  async createUserContent(userId: string, payload: any) {
    const rawText = `${payload?.text ?? ''}`.trim();
    if (!rawText) throw new BadRequestException('text is required');

    const normalized = this.normalizeText(rawText);
    const hash = this.hashText(rawText);

    const textType = payload.type || 'QUOTE';

    const matchingItem = await this.prisma.contentItem.findFirst({
      where: {
        normalizedText: normalized,
        OR: [
          { ownerUserId: userId },
          {
            ownerUserId: null,
            visibility: 'SYSTEM',
            status: 'ACTIVE',
          },
        ],
      },
    });

    let contentItem = matchingItem;

    if (!contentItem) {
      contentItem = await this.prisma.contentItem.create({
        data: {
          text: rawText,
          type: textType,
          author: payload.author,
          visibility: 'PRIVATE',
          ownerUserId: userId,
          normalizedText: normalized,
          contentHash: hash,
        },
      });
    }

    const categorySlugs = Array.isArray(payload.categories) ? payload.categories : [];
    if (categorySlugs.length > 0) {
      await this.syncCategories(contentItem.id, categorySlugs);
    }

    try {
      await this.prisma.userContent.create({
        data: {
          userId,
          contentItemId: contentItem.id,
          favorite: !!payload.favorite,
          priority: payload.priority ?? 1,
          archived: false,
        },
      });
    } catch {
      throw new BadRequestException('Duplicate user content item');
    }

    return this.prisma.userContent.findUnique({
      where: {
        userId_contentItemId: {
          userId,
          contentItemId: contentItem.id,
        },
      },
      include: {
        contentItem: {
          include: {
            categories: { include: { category: true } },
          },
        },
      },
    });
  }

  async patchUserContent(userId: string, id: string, payload: any) {
    const relation = await this.prisma.userContent.findFirst({ where: { id, userId } });
    if (!relation) throw new NotFoundException();

    const data: any = {};
    if (payload.favorite !== undefined) data.favorite = payload.favorite;
    if (payload.archived !== undefined) data.archived = payload.archived;
    if (payload.priority !== undefined) data.priority = payload.priority;

    await this.prisma.userContent.update({ where: { id }, data });

    if (payload.type || payload.text || payload.author || payload.categories) {
      const normalized = payload.text ? this.normalizeText(payload.text) : undefined;
      await this.prisma.contentItem.update({
        where: { id: relation.contentItemId },
        data: {
          text: payload.text ? payload.text.trim() : undefined,
          type: payload.type,
          author: payload.author,
          normalizedText: normalized,
          contentHash: normalized ? this.hashText(payload.text) : undefined,
        },
      });

      if (payload.categories !== undefined) {
        await this.syncCategories(relation.contentItemId, payload.categories || []);
      }
    }

    return this.prisma.userContent.findUnique({
      where: { id },
      include: { contentItem: true },
    });
  }

  async deleteUserContent(userId: string, id: string) {
    const relation = await this.prisma.userContent.findFirst({ where: { id, userId } });
    if (!relation) throw new NotFoundException();

    await this.prisma.userContent.update({ where: { id }, data: { archived: true } });
    return { success: true, id };
  }

  async toggleFavorite(userId: string, contentItemId: string, favorite: boolean) {
    const relation = await this.prisma.userContent.findFirst({ where: { userId, contentItemId } });

    if (!relation) {
      const content = await this.prisma.contentItem.findFirst({ where: { id: contentItemId, status: 'ACTIVE' } });
      if (!content) throw new NotFoundException('No content item');

      await this.prisma.userContent.create({
        data: {
          userId,
          contentItemId,
          favorite,
          priority: 1,
          archived: false,
        },
      });
    } else {
      await this.prisma.userContent.update({ where: { id: relation.id }, data: { favorite } });
    }

    if (favorite) {
      await this.prisma.favorite.upsert({
        where: { userId_contentItemId: { userId, contentItemId } },
        create: { userId, contentItemId },
        update: { contentItemId },
      });
    } else {
      await this.prisma.favorite.deleteMany({ where: { userId, contentItemId } });
    }

    return { ok: true, contentItemId, favorite };
  }

  async findSystemItemsByCatalogSlug(slug: string) {
    const catalog = await this.prisma.catalog.findUnique({
      where: { slug },
      include: {
        items: {
          include: {
            contentItem: {
              include: {
                categories: { include: { category: true } },
              },
            },
          },
        },
      },
    });

    return catalog?.items ?? [];
  }

  normalizeText(value: string) {
    if (!value) return '';
    return value
      .normalize('NFKC')
      .toLowerCase()
      .replace(/[\u2018\u2019]/g, "'")
      .replace(/[\u201C\u201D]/g, '"')
      .replace(/[\s]+/g, ' ')
      .replace(/^"|"$|^'|'$/g, '')
      .trim();
  }

  hashText(text: string) {
    // Lightweight deterministic duplicate hash for seed/manual comparison.
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const crypto = require('crypto');
    return crypto.createHash('sha256').update(this.normalizeText(text)).digest('hex');
  }

  labelFromSlug(slug: string) {
    return slug
      .split(/[\s_-]+/)
      .map((s) => (s[0] ? `${s[0].toUpperCase()}${s.slice(1)}` : s))
      .join(' ');
  }

  private async syncCategories(contentItemId: string, categorySlugs: string[]) {
    await this.prisma.contentItemCategory.deleteMany({
      where: { contentItemId },
    });

    for (const raw of categorySlugs) {
      const slug = `${raw || ''}`.trim().toLowerCase();
      if (!slug) continue;

      const category = await this.prisma.category.upsert({
        where: { slug },
        create: { slug, name: this.labelFromSlug(slug), isActive: true },
        update: {},
      });

      await this.prisma.contentItemCategory.upsert({
        where: {
          contentItemId_categoryId: {
            contentItemId,
            categoryId: category.id,
          },
        },
        create: {
          contentItemId,
          categoryId: category.id,
        },
        update: {},
      });
    }
  }
}

import { Injectable } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';

export type FeedItem = {
  id: string;
  text: string;
  type: string;
  author: string | null;
  sourceUrl: string | null;
  categories: string[];
  source: 'PERSONAL' | 'SYSTEM';
  favorite: boolean;
  updatedAt: string;
};

export function resolvePersonalWeight(personalCount: number, mix: 'MOSTLY_MINE' | 'BALANCED' | 'MORE_DISCOVERY' = 'BALANCED') {
  if (personalCount <= 0) return 0;
  if (mix === 'MOSTLY_MINE') return 0.8;
  if (mix === 'MORE_DISCOVERY') return 0.2;
  if (personalCount <= 10) return 0.5;
  if (personalCount <= 30) return 0.7;
  return 0.8;
}

@Injectable()
export class FeedService {
  constructor(private readonly prisma: PrismaService) {}

  async getFeed(userId: string, limit = 200) {
    const safeLimit = Math.min(Math.max(Number(limit) || 200, 1), 500);

    const personalCount = await this.prisma.userContent.count({
      where: {
        userId,
        archived: false,
        contentItem: { status: 'ACTIVE' },
      },
    });

    const userPrefs = await this.prisma.userPreference.findUnique({
      where: { userId },
    });

    const mix = userPrefs?.personalSystemMix || 'BALANCED';
    const personalWeight = resolvePersonalWeight(personalCount, mix);
    let personalLimit = Math.floor(safeLimit * personalWeight);
    let systemLimit = Math.max(1, safeLimit - personalLimit);

    if (personalCount < personalLimit) {
      systemLimit += personalLimit - personalCount;
      personalLimit = personalCount;
    }

    const recentHistory = await this.prisma.event.findMany({
      where: {
        userId,
        type: 'CONTENT_SHOWN',
        surface: 'WIDGET',
        contentItemId: { not: null },
      },
      orderBy: { timestamp: 'desc' },
      take: 40,
      select: { contentItemId: true },
    });

    const recentlyShown = new Set(recentHistory.map((entry) => entry.contentItemId).filter(Boolean) as string[]);

    const personalItems = await this.prisma.userContent.findMany({
      where: {
        userId,
        archived: false,
        contentItem: {
          status: 'ACTIVE',
          visibility: { in: ['PRIVATE', 'COMMUNITY'] },
        },
      },
      include: {
        contentItem: {
          include: {
            categories: {
              include: {
                category: true,
              },
            },
          },
        },
      },
      orderBy: [
        { favorite: 'desc' },
        { priority: 'desc' },
        { createdAt: 'desc' },
      ],
      take: Math.max(personalLimit, 1),
    });

    const catalogPrefs = await this.prisma.userCatalogPreference.findMany({
      where: { userId },
      select: { catalogId: true, weight: true },
    });

    const enabledCatalogPrefs = await this.prisma.userCatalogPreference.findMany({
      where: { userId, enabled: true },
      select: { catalogId: true, weight: true },
    });

    const catalogFilter = catalogPrefs.length > 0
      ? {
          catalog: {
            id: {
              in: enabledCatalogPrefs.map((item) => item.catalogId),
            },
          },
        }
      : {
          catalog: { isActive: true },
        };

    const weightByCatalog = new Map<string, number>(
      enabledCatalogPrefs.map((row) => [row.catalogId, Number(row.weight || 1)]),
    );

    const rankedSystemCandidates = await this.prisma.catalogItem.findMany({
      where: {
        catalog: catalogFilter.catalog,
        contentItem: {
          status: 'ACTIVE',
          visibility: 'SYSTEM',
        },
      },
      include: {
        catalog: true,
        contentItem: {
          include: {
            categories: { include: { category: true } },
            favorites: true,
          },
        },
      },
      orderBy: { priority: 'desc' },
      take: safeLimit * 2,
    });

    const topSystemByCatalogWeight = rankedSystemCandidates
      .map((entry) => ({
        entry,
        weight: Number(entry.priority) * (weightByCatalog.get(entry.catalogId) || 1),
      }))
      .sort((a, b) => b.weight - a.weight)
      .map(({ entry }) => entry)
      .filter((entry) => !recentlyShown.has(entry.contentItem.id));

    const uniqueSystem = new Map<string, FeedItem>();

    for (const row of topSystemByCatalogWeight) {
      if (uniqueSystem.has(row.contentItem.id)) continue;
      uniqueSystem.set(row.contentItem.id, {
        id: row.contentItem.id,
        text: row.contentItem.text,
        type: row.contentItem.type,
        author: row.contentItem.author,
        sourceUrl: row.contentItem.sourceUrl,
        categories: row.contentItem.categories.map((cat) => cat.category.slug),
        source: 'SYSTEM',
        favorite: row.contentItem.favorites.length > 0,
        updatedAt: row.contentItem.updatedAt.toISOString(),
      });
      if (uniqueSystem.size >= safeLimit) break;
    }

    const items: FeedItem[] = [...personalItems
      .filter((item) => item.contentItemId)
      .map<FeedItem>((entry) => ({
        id: entry.contentItem.id,
        text: entry.contentItem.text,
        type: entry.contentItem.type,
        author: entry.contentItem.author,
        sourceUrl: entry.contentItem.sourceUrl,
        categories: entry.contentItem.categories.map((cat) => cat.category.slug),
        source: 'PERSONAL',
        favorite: entry.favorite,
        updatedAt: entry.contentItem.updatedAt.toISOString(),
      }))
      , ...Array.from(uniqueSystem.values())
    ].filter((item, index, all) => index === all.findIndex((candidate) => candidate.id === item.id))
      .slice(0, safeLimit);

    return { items };
  }
}

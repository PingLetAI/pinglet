import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';
import { CURRENT_TERMS_VERSION } from '../common/legal/terms.constants';

type PersonalSystemMix = 'MOSTLY_MINE' | 'BALANCED' | 'MORE_DISCOVERY';

@Injectable()
export class UsersService {
  constructor(private readonly prisma: PrismaService) {}

  async createAnonymous(installationId: string) {
    return this.prisma.user.create({
      data: {
        installationId,
        isAnonymous: true,
        preferences: {
          create: {
            refreshMinutes: 30,
            personalSystemMix: 'BALANCED',
          },
        },
      },
    });
  }

  async getById(id: string) {
    return this.prisma.user.findUnique({
      where: { id },
      include: {
        devices: true,
        preferences: true,
        userContents: {
          where: { archived: false },
          include: { contentItem: true },
        },
      },
    });
  }

  async getPreferences(userId: string) {
    const existing = await this.prisma.userPreference.findUnique({
      where: { userId },
    });

    if (existing) return existing;

    return this.prisma.userPreference.create({
      data: { userId },
    });
  }

  async getTermsStatus(userId: string) {
    const user = await this.prisma.user.findUniqueOrThrow({
      where: { id: userId },
      select: { termsAcceptedVersion: true, termsAcceptedAt: true },
    });
    return {
      currentVersion: CURRENT_TERMS_VERSION,
      accepted: user.termsAcceptedVersion === CURRENT_TERMS_VERSION,
      acceptedAt: user.termsAcceptedAt,
    };
  }

  async acceptCurrentTerms(userId: string) {
    const acceptedAt = new Date();
    await this.prisma.user.update({
      where: { id: userId },
      data: { termsAcceptedVersion: CURRENT_TERMS_VERSION, termsAcceptedAt: acceptedAt },
    });
    return { currentVersion: CURRENT_TERMS_VERSION, accepted: true, acceptedAt };
  }

  async patchPreferences(
    userId: string,
    updates: { refreshMinutes?: number; personalSystemMix?: PersonalSystemMix; theme?: string },
  ) {
    return this.prisma.userPreference.upsert({
      where: { userId },
      update: {
        refreshMinutes: updates.refreshMinutes,
        personalSystemMix: updates.personalSystemMix,
        theme: updates.theme,
      },
      create: {
        userId,
        refreshMinutes: updates.refreshMinutes ?? 30,
        personalSystemMix: updates.personalSystemMix ?? 'BALANCED',
        theme: updates.theme ?? 'system',
      },
    });
  }

  async getCatalogPreferences(userId: string) {
    const exclusions = await this.getExploreExclusions(userId);
    const catalogs = await this.prisma.catalog.findMany({
      where: { isActive: true },
      orderBy: { name: 'asc' },
      include: {
        userCatalogSettings: { where: { userId } },
        _count: { select: { items: true } },
        items: {
          orderBy: { priority: 'desc' },
          take: 2,
          include: { contentItem: true },
        },
      },
    });
    return catalogs.map(({ userCatalogSettings, items, _count, ...catalog }) => ({
      ...catalog,
      enabled: userCatalogSettings[0]?.enabled ?? true,
      itemCount: _count.items,
      previewItems: items.filter(({ contentItem }) => this.isExploreVisible(contentItem, exclusions)).map(({ contentItem }) => ({
        id: contentItem.id, text: contentItem.text, type: contentItem.type,
        author: contentItem.author, sourceUrl: contentItem.sourceUrl,
      })),
    }));
  }

  async getCatalogDetail(userId: string, catalogId: string) {
    const exclusions = await this.getExploreExclusions(userId);
    const catalog = await this.prisma.catalog.findFirst({
      where: { id: catalogId, isActive: true },
      include: {
        userCatalogSettings: { where: { userId } },
        _count: { select: { items: true } },
        items: {
          orderBy: { priority: 'desc' },
          take: 100,
          include: { contentItem: true },
        },
      },
    });
    if (!catalog) throw new NotFoundException('Collection not found');
    const visibleItems = catalog.items.filter(({ contentItem }) => this.isExploreVisible(contentItem, exclusions));
    return {
      id: catalog.id, slug: catalog.slug, name: catalog.name,
      description: catalog.description, enabled: catalog.userCatalogSettings[0]?.enabled ?? true,
      itemCount: visibleItems.length,
      items: visibleItems.map(({ contentItem }) => ({
        id: contentItem.id, text: contentItem.text, type: contentItem.type,
        author: contentItem.author, sourceUrl: contentItem.sourceUrl,
      })),
    };
  }

  async patchCatalogPreference(userId: string, catalogId: string, enabled: boolean) {
    await this.prisma.catalog.findFirstOrThrow({ where: { id: catalogId, isActive: true } });
    await this.prisma.userCatalogPreference.upsert({
      where: { userId_catalogId: { userId, catalogId } },
      create: { userId, catalogId, enabled },
      update: { enabled },
    });
    return { catalogId, enabled };
  }

  async reportExploreItem(userId: string, contentItemId: string, reason: string) {
    await this.requireExploreItem(contentItemId);
    await this.prisma.contentReport.upsert({
      where: { reporterUserId_contentItemId: { reporterUserId: userId, contentItemId } },
      create: { reporterUserId: userId, contentItemId, reason },
      update: { reason, status: 'PENDING' },
    });
    return { success: true, hiddenContentIds: [contentItemId] };
  }

  async hideExploreSource(userId: string, contentItemId: string) {
    const item = await this.requireExploreItem(contentItemId);
    const sourceKey = this.exploreSourceKey(item);
    await this.prisma.blockedExploreSource.upsert({
      where: { userId_sourceKey: { userId, sourceKey } },
      create: { userId, sourceKey },
      update: {},
    });
    const candidates = item.author?.trim()
      ? await this.prisma.contentItem.findMany({
          where: {
            visibility: 'COMMUNITY',
            sourcePlatform: item.sourcePlatform,
            author: { equals: item.author, mode: 'insensitive' },
          },
          select: { id: true },
        })
      : [{ id: item.id }];
    return { success: true, hiddenContentIds: candidates.map((candidate) => candidate.id) };
  }

  private requireExploreItem(contentItemId: string) {
    return this.prisma.contentItem.findFirstOrThrow({
      where: { id: contentItemId, visibility: 'COMMUNITY', status: 'ACTIVE', catalogItems: { some: {} } },
    });
  }

  private async getExploreExclusions(userId: string) {
    const [reports, blocks] = await Promise.all([
      this.prisma.contentReport.findMany({ where: { reporterUserId: userId }, select: { contentItemId: true } }),
      this.prisma.blockedExploreSource.findMany({ where: { userId }, select: { sourceKey: true } }),
    ]);
    return {
      contentIds: new Set(reports.map((report) => report.contentItemId)),
      sourceKeys: new Set(blocks.map((block) => block.sourceKey)),
    };
  }

  private isExploreVisible(item: { id: string; author: string | null; sourcePlatform: string | null; sourceUrl: string | null }, exclusions: { contentIds: Set<string>; sourceKeys: Set<string> }) {
    return !exclusions.contentIds.has(item.id) && !exclusions.sourceKeys.has(this.exploreSourceKey(item));
  }

  private exploreSourceKey(item: { id: string; author: string | null; sourcePlatform: string | null; sourceUrl: string | null }) {
    const author = item.author?.trim().toLowerCase();
    if (author) return `${item.sourcePlatform || 'SOURCE'}:${author}`;
    return `CONTENT:${item.id}`;
  }
}

import { Injectable } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';

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
    const catalogs = await this.prisma.catalog.findMany({
      where: { isActive: true },
      orderBy: { name: 'asc' },
      include: { userCatalogSettings: { where: { userId } } },
    });
    return catalogs.map(({ userCatalogSettings, ...catalog }) => ({
      ...catalog,
      enabled: userCatalogSettings[0]?.enabled ?? true,
    }));
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
}

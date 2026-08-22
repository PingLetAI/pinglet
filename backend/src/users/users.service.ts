import { Injectable } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';

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

  async patchPreferences(userId: string, updates: { refreshMinutes?: number; personalSystemMix?: 'MOSTLY_MINE' | 'BALANCED' | 'MORE_DISCOVERY'; theme?: string }) {
    return this.prisma.userPreference.upsert({
      where: { userId },
      update: {
        refreshMinutes: updates.refreshMinutes,
        personalSystemMix: updates.personalSystemMix as any,
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
}

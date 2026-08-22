import { Injectable } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';

export interface DeviceInput {
  userId: string;
  installationId: string;
  platform: 'ANDROID' | 'IOS';
  timezone: string;
  locale: string;
  appVersion: string;
}

@Injectable()
export class DeviceService {
  constructor(private readonly prisma: PrismaService) {}

  async findOrCreate(data: DeviceInput) {
    const existing = await this.prisma.device.findUnique({
      where: { installationId: data.installationId },
    });

    if (existing) {
      return this.prisma.device.update({
        where: { installationId: data.installationId },
        data: {
          userId: data.userId,
          platform: data.platform,
          timezone: data.timezone,
          locale: data.locale,
          appVersion: data.appVersion,
          lastSyncAt: new Date(),
        },
      });
    }

    return this.prisma.device.create({
      data: {
        userId: data.userId,
        installationId: data.installationId,
        platform: data.platform,
        timezone: data.timezone,
        locale: data.locale,
        appVersion: data.appVersion,
      },
    });
  }
}

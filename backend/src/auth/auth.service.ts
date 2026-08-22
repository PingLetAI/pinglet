import { Injectable } from '@nestjs/common';
import { BadRequestException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { randomUUID } from 'crypto';
import { DeviceService } from '../devices/device.service';
import { UsersService } from '../users/users.service';
import { PrismaService } from '../common/prisma/prisma.service';

@Injectable()
export class AuthService {
  constructor(
    private readonly users: UsersService,
    private readonly devices: DeviceService,
    private readonly prisma: PrismaService,
    private readonly jwt: JwtService,
    private readonly config: ConfigService,
  ) {}

  async createAnonymous(installationId: string, platform: 'ANDROID' | 'IOS' = 'ANDROID', timezone = 'UTC', locale = 'en', appVersion = '0.1.0') {
    if (!installationId?.trim()) {
      throw new Error('installationId is required');
    }

    let user = await this.prisma.user.findUnique({
      where: { installationId },
    });

    if (!user) {
      user = await this.users.createAnonymous(installationId);
    }

    const device = await this.devices.findOrCreate({
      userId: user.id,
      installationId,
      platform,
      timezone,
      locale,
      appVersion,
    });

    const accessExpires = Number(this.config.get('JWT_EXPIRATION', 900));
    const refreshExpires = Number(this.config.get('JWT_REFRESH_EXPIRATION', 1209600));

    await this.prisma.refreshToken.deleteMany({
      where: {
        userId: user.id,
        deviceId: device.id,
        expiresAt: { lt: new Date() },
      },
    });

    const accessToken = await this.jwt.signAsync({
      sub: user.id,
      deviceId: device.id,
      installationId,
      type: 'access',
    }, { expiresIn: accessExpires });

    const refreshTokenValue = randomUUID();
    await this.prisma.refreshToken.create({
      data: {
        token: refreshTokenValue,
        userId: user.id,
        deviceId: device.id,
        expiresAt: new Date(Date.now() + refreshExpires * 1000),
      },
    });

    return {
      userId: user.id,
      accessToken,
      refreshToken: refreshTokenValue,
      expiresIn: accessExpires,
      tokenType: 'Bearer',
      deviceId: device.id,
    };
  }

  async refreshAccess(refreshTokenValue: string) {
    const existing = await this.prisma.refreshToken.findUnique({
      where: { token: refreshTokenValue },
      include: { user: true, device: true },
    });

    if (!existing || existing.revokedAt || existing.expiresAt < new Date()) {
      throw new BadRequestException('Invalid or expired refresh token');
    }

    const accessExpires = Number(this.config.get('JWT_EXPIRATION', 900));

    const accessToken = await this.jwt.signAsync({
      sub: existing.userId,
      deviceId: existing.deviceId,
      installationId: existing.device?.installationId,
      type: 'access',
    }, { expiresIn: accessExpires });

    return {
      accessToken,
      expiresIn: accessExpires,
      tokenType: 'Bearer',
    };
  }
}

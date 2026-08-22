import { Module } from '@nestjs/common';
import { DeviceService } from './device.service';
import { PrismaService } from '../common/prisma/prisma.service';

@Module({
  providers: [DeviceService, PrismaService],
  exports: [DeviceService],
})
export class DevicesModule {}

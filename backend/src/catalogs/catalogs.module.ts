import { Module } from '@nestjs/common';
import { CatalogsController } from './catalogs.controller';
import { CatalogsService } from './catalogs.service';
import { PrismaService } from '../common/prisma/prisma.service';

@Module({
  controllers: [CatalogsController],
  providers: [CatalogsService, PrismaService],
})
export class CatalogsModule {}

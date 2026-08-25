import { Controller, Get, Patch, Body, UseGuards, Req, Param } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { UsersService } from './users.service';
import { JwtAuthGuard } from '../common/auth/jwt-auth.guard';
import { IsBoolean, IsIn, IsInt, IsOptional, IsString, Max, Min } from 'class-validator';

class PreferencePatchDto {
  @IsOptional() @IsInt() @Min(15) @Max(1440)
  refreshMinutes?: number;
  @IsOptional() @IsIn(['MOSTLY_MINE', 'BALANCED', 'MORE_DISCOVERY'])
  personalSystemMix?: 'MOSTLY_MINE' | 'BALANCED' | 'MORE_DISCOVERY';
  @IsOptional() @IsString()
  theme?: string;
}

class CatalogPreferencePatchDto {
  @IsBoolean()
  enabled!: boolean;
}

@ApiTags('users')
@ApiBearerAuth()
@Controller('me')
@UseGuards(JwtAuthGuard)
export class UsersController {
  constructor(private readonly users: UsersService) {}

  @Get()
  me(@Req() req: any) {
    return this.users.getById(req.user.sub);
  }

  @Get('preferences')
  getPreferences(@Req() req: any) {
    return this.users.getPreferences(req.user.sub);
  }

  @Patch('preferences')
  patchPreferences(@Req() req: any, @Body() dto: PreferencePatchDto) {
    return this.users.patchPreferences(req.user.sub, dto);
  }

  @Get('catalogs')
  getCatalogPreferences(@Req() req: any) {
    return this.users.getCatalogPreferences(req.user.sub);
  }

  @Patch('catalogs/:catalogId')
  patchCatalogPreference(@Req() req: any, @Param('catalogId') catalogId: string, @Body() dto: CatalogPreferencePatchDto) {
    return this.users.patchCatalogPreference(req.user.sub, catalogId, dto.enabled);
  }
}

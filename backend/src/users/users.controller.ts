import { Controller, Get, Patch, Post, Body, UseGuards, Req, Param } from '@nestjs/common';
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

class ExploreReportDto {
  @IsIn(['UNSAFE', 'MISLEADING_SPAM', 'PRIVACY_RIGHTS', 'OTHER'])
  reason!: 'UNSAFE' | 'MISLEADING_SPAM' | 'PRIVACY_RIGHTS' | 'OTHER';
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

  @Get('terms')
  getTermsStatus(@Req() req: any) {
    return this.users.getTermsStatus(req.user.sub);
  }

  @Post('terms/accept')
  acceptTerms(@Req() req: any) {
    return this.users.acceptCurrentTerms(req.user.sub);
  }

  @Patch('preferences')
  patchPreferences(@Req() req: any, @Body() dto: PreferencePatchDto) {
    return this.users.patchPreferences(req.user.sub, dto);
  }

  @Get('catalogs')
  getCatalogPreferences(@Req() req: any) {
    return this.users.getCatalogPreferences(req.user.sub);
  }

  @Get('catalogs/:catalogId/items')
  getCatalogDetail(@Req() req: any, @Param('catalogId') catalogId: string) {
    return this.users.getCatalogDetail(req.user.sub, catalogId);
  }

  @Patch('catalogs/:catalogId')
  patchCatalogPreference(@Req() req: any, @Param('catalogId') catalogId: string, @Body() dto: CatalogPreferencePatchDto) {
    return this.users.patchCatalogPreference(req.user.sub, catalogId, dto.enabled);
  }

  @Post('catalogs/items/:contentItemId/report')
  reportExploreItem(@Req() req: any, @Param('contentItemId') contentItemId: string, @Body() dto: ExploreReportDto) {
    return this.users.reportExploreItem(req.user.sub, contentItemId, dto.reason);
  }

  @Post('catalogs/items/:contentItemId/hide-source')
  hideExploreSource(@Req() req: any, @Param('contentItemId') contentItemId: string) {
    return this.users.hideExploreSource(req.user.sub, contentItemId);
  }
}

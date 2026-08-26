import { Body, Controller, Delete, Get, Param, Post, UseGuards, Patch } from '@nestjs/common';
import { ApiTags, ApiBearerAuth } from '@nestjs/swagger';
import { IsEmail, IsIn, IsInt, IsOptional, Max, Min } from 'class-validator';
import { AdminGuard } from '../common/auth/admin.guard';
import { AdminService } from './admin.service';

class GrantPlusDto {
  @IsEmail()
  email: string;

  @IsOptional()
  @IsInt()
  @Min(1)
  @Max(3650)
  durationDays?: number;
}

class ResolveReportDto {
  @IsIn(['DISMISS', 'REMOVE'])
  action!: 'DISMISS' | 'REMOVE';
}

@ApiTags('admin')
@Controller('admin')
@UseGuards(AdminGuard)
export class AdminController {
  constructor(private readonly service: AdminService) {}

  @Post('content')
  createContent(@Body() body: any) {
    return this.service.upsertContent([body]);
  }

  @Post('content/import')
  importContent(@Body() body: any) {
    if (body.format === 'csv') {
      return this.service.importCsv(body.payload);
    }
    return this.service.importJson(body);
  }

  @Patch('content/:id')
  patchContent(@Param('id') id: string, @Body() body: any) {
    return this.service.patchContent(id, body);
  }

  @Delete('content/:id')
  deleteContent(@Param('id') id: string) {
    return this.service.deleteContent(id);
  }

  @Post('catalogs')
  createCatalog(@Body() body: any) {
    return this.service.upsertCatalog(body);
  }

  @Patch('catalogs/:id')
  patchCatalog(@Param('id') id: string, @Body() body: any) {
    return this.service.patchCatalog(id, body);
  }

  @Post('users/plus')
  grantPlus(@Body() body: GrantPlusDto) {
    return this.service.grantPlus(body.email, body.durationDays);
  }

  @Get('reports')
  listReports() {
    return this.service.listReports();
  }

  @Patch('reports/:id')
  resolveReport(@Param('id') id: string, @Body() body: ResolveReportDto) {
    return this.service.resolveReport(id, body.action);
  }
}

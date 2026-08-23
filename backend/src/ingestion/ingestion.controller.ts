import { Body, Controller, Get, HttpCode, Param, Post, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { IsOptional, IsString, IsUrl, MaxLength } from 'class-validator';
import { JwtAuthGuard } from '../common/auth/jwt-auth.guard';
import { IngestionService } from './ingestion.service';

class IngestionCreateDto {
  @IsUrl({ protocols: ['https'], require_protocol: true })
  url: string;

  @IsOptional()
  @IsString()
  @MaxLength(5000)
  contextText?: string;
}

@ApiTags('ingestion')
@ApiBearerAuth()
@Controller('me/ingestions')
@UseGuards(JwtAuthGuard)
export class IngestionController {
  constructor(private readonly service: IngestionService) {}

  @Post()
  @HttpCode(202)
  create(@Req() req: any, @Body() body: IngestionCreateDto) {
    return this.service.createUrlIngestion(req.user.sub, body.url, body.contextText);
  }

  @Get()
  list(@Req() req: any) {
    return this.service.listForUser(req.user.sub);
  }

  @Get(':id')
  get(@Req() req: any, @Param('id') id: string) {
    return this.service.getForUser(req.user.sub, id);
  }
}

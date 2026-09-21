import { Body, Controller, Get, HttpCode, Param, Post, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { IsOptional, IsString, MaxLength } from 'class-validator';
import { JwtAuthGuard } from '../common/auth/jwt-auth.guard';
import { IngestionService } from './ingestion.service';
import { RateLimit } from '../common/security/rate-limit.decorator';

class IngestionCreateDto {
  // URL syntax and supported-platform validation live in the service so the
  // same canonicalization is used by every client.
  @IsString()
  @MaxLength(2048)
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
  @RateLimit(60, 3600, 'ingestion-create')
  @HttpCode(202)
  create(@Req() req: any, @Body() body: IngestionCreateDto) {
    // Older clients may send contextText. Accept the field for compatibility,
    // but never store it or include it in generalized public-post analysis.
    return this.service.createUrlIngestion(req.user.sub, body.url);
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

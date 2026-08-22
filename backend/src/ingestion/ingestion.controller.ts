import { Controller, Post, Req, Body, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../common/auth/jwt-auth.guard';
import { IngestionService } from './ingestion.service';

class IngestionCreateDto {
  text?: string;
}

@ApiTags('ingestion')
@ApiBearerAuth()
@Controller('me/ingestions')
@UseGuards(JwtAuthGuard)
export class IngestionController {
  constructor(private readonly service: IngestionService) {}

  @Post()
  create(@Req() req: any, @Body() body: IngestionCreateDto) {
    return this.service.ingestText(req.user.sub, body.text || '');
  }
}

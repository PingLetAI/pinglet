import { Body, Controller, Post, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../common/auth/jwt-auth.guard';
import { EventsService } from './events.service';

class EventBatchDto {
  events: Array<{
    type: 'CONTENT_SHOWN' | 'CONTENT_SKIPPED' | 'CONTENT_FAVORITED' | 'CONTENT_OPENED';
    contentItemId?: string;
    surface: 'WIDGET' | 'APP' | 'NOTIFICATION';
    timestamp?: string;
  }> = [];
}

@ApiTags('events')
@ApiBearerAuth()
@Controller('events')
@UseGuards(JwtAuthGuard)
export class EventsController {
  constructor(private readonly service: EventsService) {}

  @Post('batch')
  batch(@Req() req: any, @Body() body: EventBatchDto) {
    return this.service.batchCreate(req.user.sub, body.events || []);
  }
}

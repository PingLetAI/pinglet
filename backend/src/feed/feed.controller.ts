import { Controller, Get, ParseIntPipe, Query, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { FeedService } from './feed.service';
import { JwtAuthGuard } from '../common/auth/jwt-auth.guard';

@ApiTags('feed')
@ApiBearerAuth()
@Controller('me/feed')
@UseGuards(JwtAuthGuard)
export class FeedController {
  constructor(private readonly service: FeedService) {}

  @Get()
  async feed(@Req() req: any, @Query('limit', ParseIntPipe) limit?: number) {
    return this.service.getFeed(req.user.sub, limit ?? 200);
  }
}

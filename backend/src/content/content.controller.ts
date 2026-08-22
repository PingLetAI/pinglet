import { Body, Controller, Delete, Get, Param, Patch, Post, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { ContentService } from './content.service';
import { JwtAuthGuard } from '../common/auth/jwt-auth.guard';

@ApiTags('content')
@ApiBearerAuth()
@Controller('me/content')
@UseGuards(JwtAuthGuard)
export class ContentController {
  constructor(private readonly service: ContentService) {}

  @Get()
  list(@Req() req: any) {
    return this.service.listUserContent(req.user.sub);
  }

  @Post()
  create(@Req() req: any, @Body() body: any) {
    return this.service.createUserContent(req.user.sub, body);
  }

  @Patch(':id')
  patch(@Req() req: any, @Param('id') id: string, @Body() body: any) {
    return this.service.patchUserContent(req.user.sub, id, body);
  }

  @Delete(':id')
  remove(@Req() req: any, @Param('id') id: string) {
    return this.service.deleteUserContent(req.user.sub, id);
  }

  @Post(':id/favorite')
  favorite(@Req() req: any, @Param('id') id: string) {
    return this.service.toggleFavorite(req.user.sub, id, true);
  }

  @Delete(':id/favorite')
  unfavorite(@Req() req: any, @Param('id') id: string) {
    return this.service.toggleFavorite(req.user.sub, id, false);
  }
}

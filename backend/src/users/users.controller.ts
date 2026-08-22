import { Controller, Get, Patch, Body, UseGuards, Req } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { UsersService } from './users.service';
import { JwtAuthGuard } from '../common/auth/jwt-auth.guard';

class PreferencePatchDto {
  refreshMinutes?: number;
  personalSystemMix?: 'MOSTLY_MINE' | 'BALANCED' | 'MORE_DISCOVERY';
  theme?: string;
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
}

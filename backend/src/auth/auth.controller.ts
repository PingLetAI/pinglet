import { Body, Controller, Post, HttpCode } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { IsNotEmpty, IsOptional, IsString } from 'class-validator';
import { AuthService } from './auth.service';

class AnonymousDto {
  @IsString()
  @IsNotEmpty()
  installationId!: string;

  @IsOptional()
  platform: 'ANDROID' | 'IOS' = 'ANDROID';

  @IsOptional()
  @IsString()
  timezone?: string;

  @IsOptional()
  @IsString()
  locale?: string;

  @IsOptional()
  @IsString()
  appVersion?: string;
}

class RefreshDto {
  @IsString()
  @IsNotEmpty()
  refreshToken!: string;
}

@ApiTags('auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly service: AuthService) {}

  @Post('anonymous')
  @HttpCode(200)
  async anonymous(@Body() body: AnonymousDto) {
    return this.service.createAnonymous(
      body.installationId,
      body.platform,
      body.timezone || 'UTC',
      body.locale || 'en',
      body.appVersion || '0.1.0',
    );
  }

  @Post('refresh')
  @HttpCode(200)
  async refresh(@Body() body: RefreshDto) {
    return this.service.refreshAccess(body.refreshToken);
  }
}

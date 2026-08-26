import { Body, Controller, Delete, Post, HttpCode, Req, UseGuards } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { IsEmail, IsNotEmpty, IsOptional, IsString, Length } from 'class-validator';
import { JwtAuthGuard } from '../common/auth/jwt-auth.guard';
import { AuthService } from './auth.service';
import { EmailOtpService } from './email-otp.service';

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

class RequestEmailOtpDto {
  @IsEmail() email!: string;
}

class VerifyEmailOtpDto {
  @IsEmail() email!: string;
  @IsString() @Length(6, 6) code!: string;
}

class DeleteAccountDto extends VerifyEmailOtpDto {}

@ApiTags('auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly service: AuthService, private readonly emailOtp: EmailOtpService) {}

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

  @Post('email/request')
  @UseGuards(JwtAuthGuard)
  requestEmailOtp(@Req() req: any, @Body() body: RequestEmailOtpDto) {
    return this.emailOtp.request(req.user.sub, body.email);
  }

  @Post('email/verify')
  @UseGuards(JwtAuthGuard)
  verifyEmailOtp(@Req() req: any, @Body() body: VerifyEmailOtpDto) {
    return this.emailOtp.verify(req.user.sub, req.user.deviceId, req.user.installationId, body.email, body.code);
  }

  @Post('logout')
  @UseGuards(JwtAuthGuard)
  @HttpCode(200)
  logout(@Req() req: any) {
    return this.service.logout(req.user.sub, req.user.deviceId, req.user.installationId);
  }

  @Delete('account')
  @UseGuards(JwtAuthGuard)
  deleteAccount(@Req() req: any, @Body() body: DeleteAccountDto) {
    return this.emailOtp.deleteAccount(req.user.sub, body.email, body.code);
  }
}

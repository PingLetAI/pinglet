import { Body, Controller, Get, HttpCode, Post, Req, UseGuards } from '@nestjs/common';
import { IsNotEmpty, IsString } from 'class-validator';
import { JwtAuthGuard } from '../common/auth/jwt-auth.guard';
import { EntitlementService } from './entitlement.service';

class VerifyGooglePlayDto {
  @IsString() @IsNotEmpty() purchaseToken!: string;
  @IsString() @IsNotEmpty() productId!: string;
}

class VerifyAppleDto {
  @IsString() @IsNotEmpty() signedTransaction!: string;
}

class AppStoreNotificationDto {
  @IsString() @IsNotEmpty() signedPayload!: string;
}

@Controller('me/entitlements')
@UseGuards(JwtAuthGuard)
export class EntitlementController {
  constructor(private readonly entitlements: EntitlementService) {}

  @Get()
  get(@Req() req: any) {
    return this.entitlements.getSummary(req.user.sub, req.headers['x-pinglet-platform']);
  }

  @Post('google-play')
  verifyGooglePlay(@Req() req: any, @Body() body: VerifyGooglePlayDto) {
    return this.entitlements.verifyGooglePlaySubscription(req.user.sub, body.purchaseToken, body.productId);
  }

  @Post('apple')
  verifyApple(@Req() req: any, @Body() body: VerifyAppleDto) {
    return this.entitlements.verifyAppleSubscription(req.user.sub, body.signedTransaction);
  }

  @Post('trial')
  startTrial(@Req() req: any) {
    return this.entitlements.startTrial(req.user.sub, req.headers['x-pinglet-platform']);
  }
}

@Controller('app-store')
export class AppStoreNotificationController {
  constructor(private readonly entitlements: EntitlementService) {}

  @Post('notifications')
  @HttpCode(200)
  async notification(@Body() body: AppStoreNotificationDto) {
    await this.entitlements.processAppleNotification(body.signedPayload);
    return {};
  }
}

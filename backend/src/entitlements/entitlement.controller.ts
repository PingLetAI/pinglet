import { Body, Controller, Get, Post, Req, UseGuards } from '@nestjs/common';
import { IsNotEmpty, IsString } from 'class-validator';
import { JwtAuthGuard } from '../common/auth/jwt-auth.guard';
import { EntitlementService } from './entitlement.service';

class VerifyGooglePlayDto {
  @IsString() @IsNotEmpty() purchaseToken!: string;
  @IsString() @IsNotEmpty() productId!: string;
}

@Controller('me/entitlements')
@UseGuards(JwtAuthGuard)
export class EntitlementController {
  constructor(private readonly entitlements: EntitlementService) {}

  @Get()
  get(@Req() req: any) {
    return this.entitlements.getSummary(req.user.sub);
  }

  @Post('google-play')
  verifyGooglePlay(@Req() req: any, @Body() body: VerifyGooglePlayDto) {
    return this.entitlements.verifyGooglePlaySubscription(req.user.sub, body.purchaseToken, body.productId);
  }

  @Post('trial')
  startTrial(@Req() req: any) {
    return this.entitlements.startTrial(req.user.sub);
  }
}

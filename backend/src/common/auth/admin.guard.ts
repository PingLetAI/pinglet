import { CanActivate, ExecutionContext, Injectable, ForbiddenException } from '@nestjs/common';
import { createHash, timingSafeEqual } from 'crypto';

@Injectable()
export class AdminGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const req = context.switchToHttp().getRequest();
    const configuredSecret = process.env.ADMIN_SECRET || '';
    const provided = req.headers['x-admin-key'];

    if (!configuredSecret || typeof provided !== 'string' || !this.matches(provided, configuredSecret)) {
      throw new ForbiddenException('Admin key missing or invalid');
    }
    return true;
  }

  private matches(provided: string, configured: string) {
    const left = createHash('sha256').update(provided).digest();
    const right = createHash('sha256').update(configured).digest();
    return timingSafeEqual(left, right);
  }
}

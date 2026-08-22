import { CanActivate, ExecutionContext, Injectable, ForbiddenException } from '@nestjs/common';

@Injectable()
export class AdminGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const req = context.switchToHttp().getRequest();
    const configuredSecret = process.env.ADMIN_SECRET || '';
    const provided = req.headers['x-admin-key'];

    if (!configuredSecret || typeof provided !== 'string' || provided !== configuredSecret) {
      throw new ForbiddenException('Admin key missing or invalid');
    }
    return true;
  }
}

import { CanActivate, ExecutionContext, HttpException, Injectable, Logger } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { createHash } from 'crypto';
import { RATE_LIMIT_METADATA, RateLimitOptions } from './rate-limit.decorator';
import { RateLimitService } from './rate-limit.service';

@Injectable()
export class RateLimitGuard implements CanActivate {
  private readonly logger = new Logger(RateLimitGuard.name);

  constructor(private readonly reflector: Reflector, private readonly limits: RateLimitService) {}

  async canActivate(context: ExecutionContext) {
    const options = this.reflector.getAllAndOverride<RateLimitOptions>(RATE_LIMIT_METADATA, [context.getHandler(), context.getClass()]);
    if (!options) return true;

    const request = context.switchToHttp().getRequest();
    const response = context.switchToHttp().getResponse();
    const identity = this.identity(request);
    const digest = createHash('sha256').update(identity).digest('hex');
    const prefix = process.env.RATE_LIMIT_REDIS_PREFIX || 'pinglet:rate';

    try {
      const result = await this.limits.consume(`${prefix}:${options.scope}:${digest}`, options.limit, options.windowSeconds);
      response.setHeader('X-RateLimit-Limit', options.limit);
      response.setHeader('X-RateLimit-Remaining', result.remaining);
      if (!result.allowed) {
        response.setHeader('Retry-After', result.retryAfter);
        throw new HttpException({ code: 'RATE_LIMITED', message: 'Too many requests. Try again later.' }, 429);
      }
      return true;
    } catch (error) {
      if (error instanceof HttpException) throw error;
      this.logger.warn(`Rate limiting unavailable for ${options.scope}; request allowed.`);
      return true;
    }
  }

  private identity(request: any) {
    const authorization = typeof request.headers?.authorization === 'string' ? request.headers.authorization : '';
    if (authorization) return `token:${authorization}`;
    const installationId = typeof request.body?.installationId === 'string' ? request.body.installationId : '';
    const ip = request.ip || request.socket?.remoteAddress || 'unknown';
    return installationId ? `installation:${installationId}:ip:${ip}` : `ip:${ip}`;
  }
}

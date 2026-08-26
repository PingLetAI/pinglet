import { SetMetadata } from '@nestjs/common';

export const RATE_LIMIT_METADATA = 'pinglet:rate-limit';

export interface RateLimitOptions {
  limit: number;
  windowSeconds: number;
  scope: string;
}

export const RateLimit = (limit: number, windowSeconds: number, scope: string) =>
  SetMetadata(RATE_LIMIT_METADATA, { limit, windowSeconds, scope } satisfies RateLimitOptions);

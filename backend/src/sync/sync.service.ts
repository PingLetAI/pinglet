import { Injectable } from '@nestjs/common';

@Injectable()
export class SyncService {
  async refreshCatalogCache(userId: string) {
    return { userId, refreshedAt: new Date().toISOString() };
  }
}

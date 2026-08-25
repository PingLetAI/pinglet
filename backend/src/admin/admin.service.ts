import { Injectable, NotFoundException } from '@nestjs/common';
import { parse } from 'csv-parse/sync';
import { PrismaService } from '../common/prisma/prisma.service';
import { createHash } from 'crypto';

@Injectable()
export class AdminService {
  constructor(private readonly prisma: PrismaService) {}

  async upsertContent(payload: Array<{ text: string; type: string; author?: string; categories?: string[]; catalog?: string }>, _importMode = false) {
    let created = 0;
    let duplicates = 0;
    const failed: Array<{ reason: string; row: any }> = [];

    for (const row of payload) {
      try {
        const normalized = this.normalizeText(row.text);
        const hash = this.hashText(normalized);
        const existing = await this.prisma.contentItem.findFirst({ where: { contentHash: hash, ownerUserId: null, visibility: 'SYSTEM' } });
        if (existing) {
          duplicates += 1;
          continue;
        }

        const createdItem = await this.prisma.contentItem.create({
          data: {
            text: row.text.trim(),
            type: row.type as any,
            author: row.author,
            visibility: 'SYSTEM',
            normalizedText: normalized,
            contentHash: hash,
          },
        });

        if (row.categories?.length) {
          await this.linkCategories(createdItem.id, row.categories);
        }

        if (row.catalog) {
          await this.linkCatalog(row.catalog, createdItem.id);
        }

        created += 1;
      } catch (error) {
        failed.push({ reason: String(error), row });
      }
    }

    return {
      received: payload.length,
      created,
      duplicates,
      failed: failed.length,
      failures: failed,
    };
  }

  async importJson(payload: { items: Array<any> }) {
    return this.upsertContent(payload.items || []);
  }

  async importCsv(csvText: string) {
    const records = parse(csvText, {
      columns: true,
      skip_empty_lines: true,
      trim: true,
    });

    const rows = records.map((row: any) => ({
      text: row.text,
      type: row.type,
      author: row.author,
      categories: row.categories ? row.categories.split('|') : [],
      catalog: row.catalog,
    }));

    return this.upsertContent(rows, true);
  }

  async patchContent(id: string, payload: any) {
    return this.prisma.contentItem.update({
      where: { id },
      data: {
        text: payload.text,
        author: payload.author,
        type: payload.type,
        status: payload.status,
        visibility: payload.visibility,
      },
    });
  }

  async deleteContent(id: string) {
    await this.prisma.contentItem.delete({ where: { id } });
    return { id, deleted: true };
  }

  async upsertCatalog(payload: { id?: string; slug: string; name: string; description?: string; isActive?: boolean }) {
    if (payload.id) {
      return this.prisma.catalog.update({
        where: { id: payload.id },
        data: {
          slug: payload.slug,
          name: payload.name,
          description: payload.description,
          isActive: payload.isActive,
        },
      });
    }

    return this.prisma.catalog.create({
      data: {
        slug: payload.slug,
        name: payload.name,
        description: payload.description,
        isActive: payload.isActive ?? true,
      },
    });
  }

  async patchCatalog(id: string, payload: { slug?: string; name?: string; description?: string; isActive?: boolean }) {
    return this.prisma.catalog.update({
      where: { id },
      data: payload,
    });
  }

  async grantPlus(rawEmail: string, durationDays = 365) {
    const email = rawEmail.trim().toLowerCase();
    const user = await this.prisma.user.findUnique({ where: { email } });
    if (!user) throw new NotFoundException('No PingLet account exists for that email address');

    const now = new Date();
    const currentExpiry = user.plusExpiresAt && user.plusExpiresAt > now ? user.plusExpiresAt : now;
    const expiresAt = new Date(currentExpiry.getTime() + durationDays * 24 * 60 * 60 * 1000);
    const purchaseToken = `admin:${user.id}:${now.getTime()}`;

    await this.prisma.$transaction([
      this.prisma.purchaseEntitlement.create({
        data: {
          userId: user.id,
          provider: 'ADMIN',
          productId: 'pinglet_plus_manual',
          purchaseToken,
          status: 'ACTIVE',
          expiresAt,
          rawData: { grantedAt: now.toISOString(), durationDays },
        },
      }),
      this.prisma.user.update({
        where: { id: user.id },
        data: { plan: 'PLUS', plusExpiresAt: expiresAt },
      }),
    ]);

    return { email: user.email, plan: 'PLUS', expiresAt, durationDays };
  }

  normalizeText(value: string) {
    return value
      .normalize('NFKC')
      .toLowerCase()
      .replace(/[\u2018\u2019]/g, "'")
      .replace(/[\u201C\u201D]/g, '"')
      .replace(/[\s]+/g, ' ')
      .replace(/^"|"$|^'|'$/g, '')
      .trim();
  }

  hashText(text: string) {
    return createHash('sha256').update(this.normalizeText(text)).digest('hex');
  }

  async linkCategories(contentItemId: string, slugs: string[]) {
    for (const slug of slugs) {
      const clean = slug.trim().toLowerCase();
      const category = await this.prisma.category.upsert({
        where: { slug: clean },
        create: { slug: clean, name: this.toTitleCase(clean), isActive: true },
        update: {},
      });

      await this.prisma.contentItemCategory.upsert({
        where: { contentItemId_categoryId: { contentItemId, categoryId: category.id } },
        create: { contentItemId, categoryId: category.id },
        update: {},
      });
    }
  }

  async linkCatalog(catalogSlug: string, contentItemId: string) {
    const catalog = await this.prisma.catalog.findUnique({
      where: { slug: catalogSlug },
    });

    if (!catalog) return;

    await this.prisma.catalogItem.upsert({
      where: { catalogId_contentItemId: { catalogId: catalog.id, contentItemId } },
      create: { catalogId: catalog.id, contentItemId, priority: 1 },
      update: {},
    });
  }

  toTitleCase(text: string) {
    return text
      .split(/\s+/)
      .map((w) => w[0]?.toUpperCase() + w.slice(1))
      .join(' ');
  }
}

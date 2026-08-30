import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';
import { EntitlementService } from '../entitlements/entitlement.service';

@Injectable()
export class ContentService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly entitlements: EntitlementService,
  ) {}

  async listUserContent(userId: string, includeArchived = false) {
    const rows = await this.prisma.userContent.findMany({
      where: {
        userId,
        archived: includeArchived ? undefined : false,
      },
      orderBy: { createdAt: 'desc' },
      include: {
        contentItem: {
          include: {
            categories: { include: { category: true } },
            favorites: true,
          },
        },
      },
    });
    return rows.map((row) => ({
      id: row.id,
      contentItemId: row.contentItemId,
      favorite: row.favorite,
      archived: row.archived,
      contentItem: {
        id: row.contentItem.id,
        text: row.contentItem.text,
        type: row.contentItem.type,
        author: row.contentItem.author,
        sourceUrl: row.contentItem.sourceUrl,
        categories: row.contentItem.categories.map((entry) => entry.category.slug),
        source: 'PERSONAL' as const,
        favorite: row.favorite,
        updatedAt: row.contentItem.updatedAt.toISOString(),
      },
    }));
  }

  async getUserContentDetail(userId: string, contentItemId: string, platform?: string) {
    const saved = await this.prisma.userContent.findUnique({
      where: { userId_contentItemId: { userId, contentItemId } },
      include: { contentItem: { include: { categories: { include: { category: true } } } } },
    });
    if (!saved) throw new NotFoundException('Saved content not found');

    const [ingestion, entitlement] = await Promise.all([
      this.prisma.ingestion.findFirst({
        where: { userId, contentItemId, status: 'READY', moderationStatus: 'APPROVED' },
        orderBy: { finishedAt: 'desc' },
      }),
      this.entitlements.getSummary(userId, platform),
    ]);
    const analysis = ingestion?.analysis as any;
    const plus = entitlement.plan === 'PLUS';
    const hasAnalysis = Boolean(ingestion && (analysis || ingestion.takeaways || ingestion.transcript || ingestion.ocrText));
    const previewInsight = Array.isArray(analysis?.insights) ? analysis.insights.slice(0, 1) : [];

    return {
      content: {
        id: saved.contentItem.id, text: saved.contentItem.text, type: saved.contentItem.type,
        author: saved.contentItem.author, sourceUrl: saved.contentItem.sourceUrl,
        sourcePlatform: saved.contentItem.sourcePlatform, favorite: saved.favorite,
        categories: saved.contentItem.categories.map((entry) => entry.category.slug),
      },
      overview: analysis?.summary?.short || null,
      insights: plus ? analysis?.insights || [] : previewInsight,
      comprehensiveSummary: plus ? analysis?.summary?.comprehensive || null : null,
      actions: plus ? analysis?.actions || [] : [],
      themes: plus ? analysis?.themes || [] : [],
      takeaways: plus ? ingestion?.takeaways || [] : Array.isArray(ingestion?.takeaways) ? ingestion!.takeaways.slice(0, 1) : [],
      transcript: plus ? ingestion?.transcript || null : null,
      visibleText: plus ? ingestion?.ocrText || null : null,
      caption: plus ? ingestion?.caption || null : null,
      access: {
        plan: entitlement.plan,
        isAnonymous: entitlement.isAnonymous,
        entitlementSource: entitlement.entitlementSource,
        accessExpiresAt: entitlement.accessExpiresAt,
        trialStatus: entitlement.trialStatus,
        trialEligible: entitlement.trialEligible,
        trialEndsAt: entitlement.trialEndsAt,
        trialDaysRemaining: entitlement.trialDaysRemaining,
        paidPlansEnabled: entitlement.paidPlansEnabled,
        hasAnalysis,
        fullDetailsUnlocked: plus,
        lockedSections: !plus && hasAnalysis ? ['FULL_SUMMARY', 'ALL_INSIGHTS', 'ACTIONS', 'TRANSCRIPT', 'VISIBLE_TEXT', 'RELATED'] : [],
      },
    };
  }

  async createUserContent(userId: string, payload: any) {
    if (!payload?.skipEntitlementCheck) await this.entitlements.assertCanSave(userId);
    const rawText = `${payload?.text ?? ''}`.trim();
    if (!rawText) throw new BadRequestException('text is required');

    const normalized = this.normalizeText(rawText);
    const hash = this.hashText(rawText);

    const textType = payload.type || 'QUOTE';

    const matchingItem = await this.prisma.contentItem.findFirst({
      where: {
        normalizedText: normalized,
        OR: [
          { ownerUserId: userId },
          {
            ownerUserId: null,
            visibility: 'SYSTEM',
            status: 'ACTIVE',
          },
        ],
      },
    });

    let contentItem = matchingItem;

    if (!contentItem) {
      contentItem = await this.prisma.contentItem.create({
        data: {
          text: rawText,
          type: textType,
          author: payload.author,
          sourceUrl: payload.sourceUrl,
          sourcePlatform: payload.sourcePlatform,
          visibility: 'PRIVATE',
          ownerUserId: userId,
          normalizedText: normalized,
          contentHash: hash,
        },
      });
    }

    const categorySlugs = Array.isArray(payload.categories) ? payload.categories : [];
    if (categorySlugs.length > 0) {
      await this.syncCategories(contentItem.id, categorySlugs);
    }

    try {
      await this.prisma.userContent.create({
        data: {
          userId,
          contentItemId: contentItem.id,
          favorite: !!payload.favorite,
          priority: payload.priority ?? 1,
          archived: false,
        },
      });
    } catch {
      throw new BadRequestException('Duplicate user content item');
    }

    return this.prisma.userContent.findUnique({
      where: {
        userId_contentItemId: {
          userId,
          contentItemId: contentItem.id,
        },
      },
      include: {
        contentItem: {
          include: {
            categories: { include: { category: true } },
          },
        },
      },
    });
  }

  async ingestUrl(userId: string, payload: any) {
    const sourceUrl = this.parseSupportedUrl(`${payload?.url ?? ''}`);
    const sourcePlatform = this.platformFor(sourceUrl.hostname);
    const ingestion = await this.prisma.ingestion.create({
      data: { userId, type: 'URL', sourceUrl: sourceUrl.toString(), rawText: payload?.contextText, status: 'RECEIVED' },
    });

    try {
      await this.prisma.ingestion.update({ where: { id: ingestion.id }, data: { status: 'PROCESSING' } });
      const metadata = await this.readPublicMetadata(sourceUrl);
      const contextText = this.stripUrls(`${payload?.contextText ?? ''}`);
      const takeaway = this.extractTakeaway(metadata.description || contextText || metadata.title);
      if (!takeaway) {
        throw new BadRequestException('This post is private or its text is unavailable. Add the takeaway manually and keep the link as its source.');
      }
      this.assertBaselinePolicy(takeaway);

      const saved = await this.createUserContent(userId, {
        text: takeaway,
        type: 'QUOTE',
        author: metadata.author || sourcePlatform,
        sourceUrl: metadata.canonicalUrl || sourceUrl.toString(),
        sourcePlatform,
        categories: ['social-save'],
        priority: 2,
      });
      await this.prisma.ingestion.update({ where: { id: ingestion.id }, data: { status: 'READY' } });

      return {
        ingestionId: ingestion.id,
        status: 'READY',
        item: {
          id: saved?.contentItem.id,
          text: saved?.contentItem.text,
          type: saved?.contentItem.type,
          author: saved?.contentItem.author,
          sourceUrl: saved?.contentItem.sourceUrl,
          sourcePlatform: saved?.contentItem.sourcePlatform,
        },
      };
    } catch (error) {
      await this.prisma.ingestion.update({ where: { id: ingestion.id }, data: { status: 'FAILED' } });
      if (error instanceof BadRequestException) throw error;
      throw new BadRequestException('The public post could not be read. Add its takeaway manually and keep the link as its source.');
    }
  }

  private parseSupportedUrl(value: string) {
    let parsed: URL;
    try {
      parsed = new URL(value.trim());
    } catch {
      throw new BadRequestException('A valid Instagram, TikTok, or Facebook link is required');
    }
    if (parsed.protocol !== 'https:' || !this.platformFor(parsed.hostname, false)) {
      throw new BadRequestException('Only HTTPS links from Instagram, TikTok, and Facebook are currently supported');
    }
    parsed.hash = '';
    return parsed;
  }

  private platformFor(hostname: string, required = true) {
    const host = hostname.toLowerCase().replace(/^www\./, '');
    if (host === 'instagram.com' || host.endsWith('.instagram.com')) return 'INSTAGRAM';
    if (host === 'tiktok.com' || host.endsWith('.tiktok.com')) return 'TIKTOK';
    if (host === 'facebook.com' || host.endsWith('.facebook.com') || host === 'fb.watch') return 'FACEBOOK';
    if (required) throw new BadRequestException('Unsupported social platform');
    return null;
  }

  private async readPublicMetadata(initialUrl: URL) {
    let current = initialUrl;
    for (let redirects = 0; redirects <= 3; redirects += 1) {
      this.parseSupportedUrl(current.toString());
      const response = await fetch(current, {
        redirect: 'manual',
        signal: AbortSignal.timeout(8000),
        headers: {
          'user-agent': 'Mozilla/5.0 (compatible; PingLetLinkPreview/1.0)',
          accept: 'text/html,application/xhtml+xml',
        },
      });
      if (response.status >= 300 && response.status < 400) {
        const location = response.headers.get('location');
        if (!location) break;
        current = new URL(location, current);
        continue;
      }
      if (!response.ok) throw new Error(`source returned ${response.status}`);
      const html = (await response.text()).slice(0, 1_000_000);
      return {
        title: this.meta(html, 'og:title') || this.meta(html, 'twitter:title'),
        description: this.meta(html, 'og:description') || this.meta(html, 'twitter:description') || this.meta(html, 'description'),
        author: this.meta(html, 'author'),
        canonicalUrl: this.meta(html, 'og:url') || current.toString(),
      };
    }
    throw new Error('too many redirects');
  }

  private meta(html: string, key: string) {
    const escaped = key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const patterns = [
      new RegExp(`<meta[^>]+(?:property|name)=["']${escaped}["'][^>]+content=["']([^"']+)["']`, 'i'),
      new RegExp(`<meta[^>]+content=["']([^"']+)["'][^>]+(?:property|name)=["']${escaped}["']`, 'i'),
    ];
    for (const pattern of patterns) {
      const value = html.match(pattern)?.[1];
      if (value) return this.decodeHtml(value).trim();
    }
    return '';
  }

  private decodeHtml(value: string) {
    return value
      .replace(/&quot;/gi, '"').replace(/&#39;|&apos;/gi, "'")
      .replace(/&amp;/gi, '&').replace(/&lt;/gi, '<').replace(/&gt;/gi, '>')
      .replace(/&#(\d+);/g, (_, code) => String.fromCharCode(Number(code)));
  }

  private stripUrls(value: string) {
    return value.replace(/https?:\/\/\S+/gi, '').replace(/\s+/g, ' ').trim();
  }

  private extractTakeaway(value: string) {
    const clean = this.decodeHtml(`${value || ''}`)
      .replace(/\s+/g, ' ')
      .replace(/^.*? on (Instagram|TikTok|Facebook):\s*/i, '')
      .trim();
    if (clean.length < 12) return '';
    const sentence = clean.match(/^.{12,280}?[.!?](?:\s|$)/)?.[0] || clean.slice(0, 280);
    return sentence.replace(/[\s.,;:]+$/, '').trim();
  }

  private assertBaselinePolicy(text: string) {
    if (text.length > 500 || /\u0000/.test(text)) throw new BadRequestException('The extracted text did not pass content checks');
    const prohibited = /\b(child sexual|terrorist recruitment|buy illegal drugs)\b/i;
    if (prohibited.test(text)) throw new BadRequestException('This content is not eligible to be saved');
  }

  async patchUserContent(userId: string, id: string, payload: any) {
    const relation = await this.prisma.userContent.findFirst({ where: { id, userId } });
    if (!relation) throw new NotFoundException();

    const data: any = {};
    if (payload.favorite !== undefined) data.favorite = payload.favorite;
    if (payload.archived !== undefined) data.archived = payload.archived;
    if (payload.priority !== undefined) data.priority = payload.priority;

    await this.prisma.userContent.update({ where: { id }, data });

    if (payload.type || payload.text || payload.author || payload.categories) {
      const normalized = payload.text ? this.normalizeText(payload.text) : undefined;
      await this.prisma.contentItem.update({
        where: { id: relation.contentItemId },
        data: {
          text: payload.text ? payload.text.trim() : undefined,
          type: payload.type,
          author: payload.author,
          normalizedText: normalized,
          contentHash: normalized ? this.hashText(payload.text) : undefined,
        },
      });

      if (payload.categories !== undefined) {
        await this.syncCategories(relation.contentItemId, payload.categories || []);
      }
    }

    return this.prisma.userContent.findUnique({
      where: { id },
      include: { contentItem: true },
    });
  }

  async deleteUserContent(userId: string, id: string) {
    const relation = await this.prisma.userContent.findFirst({ where: { id, userId } });
    if (!relation) throw new NotFoundException();

    await this.prisma.userContent.update({ where: { id }, data: { archived: true } });
    return { success: true, id };
  }

  async toggleFavorite(userId: string, contentItemId: string, favorite: boolean) {
    const relation = await this.prisma.userContent.findFirst({ where: { userId, contentItemId } });

    if (!relation) {
      const content = await this.prisma.contentItem.findFirst({ where: { id: contentItemId, status: 'ACTIVE' } });
      if (!content) throw new NotFoundException('No content item');

      await this.prisma.userContent.create({
        data: {
          userId,
          contentItemId,
          favorite,
          priority: 1,
          archived: false,
        },
      });
    } else {
      await this.prisma.userContent.update({ where: { id: relation.id }, data: { favorite } });
    }

    if (favorite) {
      await this.prisma.favorite.upsert({
        where: { userId_contentItemId: { userId, contentItemId } },
        create: { userId, contentItemId },
        update: { contentItemId },
      });
    } else {
      await this.prisma.favorite.deleteMany({ where: { userId, contentItemId } });
    }

    return { ok: true, contentItemId, favorite };
  }

  async findSystemItemsByCatalogSlug(slug: string) {
    const catalog = await this.prisma.catalog.findUnique({
      where: { slug },
      include: {
        items: {
          include: {
            contentItem: {
              include: {
                categories: { include: { category: true } },
              },
            },
          },
        },
      },
    });

    return catalog?.items ?? [];
  }

  normalizeText(value: string) {
    if (!value) return '';
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
    // Lightweight deterministic duplicate hash for seed/manual comparison.
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const crypto = require('crypto');
    return crypto.createHash('sha256').update(this.normalizeText(text)).digest('hex');
  }

  labelFromSlug(slug: string) {
    return slug
      .split(/[\s_-]+/)
      .map((s) => (s[0] ? `${s[0].toUpperCase()}${s.slice(1)}` : s))
      .join(' ');
  }

  private async syncCategories(contentItemId: string, categorySlugs: string[]) {
    await this.prisma.contentItemCategory.deleteMany({
      where: { contentItemId },
    });

    for (const raw of categorySlugs) {
      const slug = `${raw || ''}`.trim().toLowerCase();
      if (!slug) continue;

      const category = await this.prisma.category.upsert({
        where: { slug },
        create: { slug, name: this.labelFromSlug(slug), isActive: true },
        update: {},
      });

      await this.prisma.contentItemCategory.upsert({
        where: {
          contentItemId_categoryId: {
            contentItemId,
            categoryId: category.id,
          },
        },
        create: {
          contentItemId,
          categoryId: category.id,
        },
        update: {},
      });
    }
  }
}

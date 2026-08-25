import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';
import { IngestionQueueService } from './ingestion-queue.service';
import { EntitlementService } from '../entitlements/entitlement.service';

@Injectable()
export class IngestionService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly queue: IngestionQueueService,
    private readonly entitlements: EntitlementService,
  ) {}

  async createUrlIngestion(userId: string, rawUrl: string, contextText?: string) {
    const url = this.parseSupportedUrl(rawUrl);
    const normalizedUrl = url.toString();
    const reusable = await this.prisma.ingestion.findFirst({
      where: {
        status: 'READY',
        moderationStatus: 'APPROVED',
        contentItemId: { not: null },
        OR: [{ sourceUrl: normalizedUrl }, { assetUrl: normalizedUrl }],
      },
      include: { contentItem: true },
      orderBy: { finishedAt: 'desc' },
    });

    if (reusable?.contentItemId) {
      const existingForUser = reusable.userId === userId
        ? reusable
        : await this.prisma.ingestion.findFirst({
            where: {
              userId,
              contentItemId: reusable.contentItemId,
              status: 'READY',
            },
            include: { contentItem: true },
          });
      if (existingForUser) return this.presentForUser(userId, existingForUser);

      await this.entitlements.assertCanSave(userId);

      const reusedForUser = await this.prisma.$transaction(async (tx) => {
        await tx.userContent.upsert({
          where: { userId_contentItemId: { userId, contentItemId: reusable.contentItemId! } },
          create: {
            userId,
            contentItemId: reusable.contentItemId!,
            favorite: false,
            archived: false,
            priority: 2,
          },
          update: { archived: false, priority: 2 },
        });
        return tx.ingestion.create({
          data: {
            userId,
            type: 'URL',
            rawText: contextText?.trim() || null,
            sourceUrl: normalizedUrl,
            sourcePlatform: this.platformFor(url.hostname),
            assetUrl: reusable.assetUrl,
            status: 'READY',
            processingStage: 'REUSED_EXISTING_EXTRACTION',
            caption: reusable.caption,
            transcript: reusable.transcript,
            ocrText: reusable.ocrText,
            sourceDocument: reusable.sourceDocument,
            takeaways: reusable.takeaways as any,
            analysis: reusable.analysis as any,
            frameReferences: reusable.frameReferences as any,
            extractionConfidence: reusable.extractionConfidence,
            moderationStatus: 'APPROVED',
            moderationResult: reusable.moderationResult as any,
            contentItemId: reusable.contentItemId,
            startedAt: new Date(),
            finishedAt: new Date(),
          },
          include: { contentItem: true },
        });
      });
      return this.presentForUser(userId, reusedForUser);
    }

    await this.entitlements.assertCanStartOriginalImport(userId);

    const ingestion = await this.prisma.ingestion.create({
      data: {
        userId,
        type: 'URL',
        rawText: contextText?.trim() || null,
        sourceUrl: normalizedUrl,
        sourcePlatform: this.platformFor(url.hostname),
        status: 'RECEIVED',
        processingStage: 'QUEUED',
      },
    });

    try {
      await this.queue.enqueue(ingestion.id);
    } catch (error) {
      await this.prisma.ingestion.update({
        where: { id: ingestion.id },
        data: { status: 'FAILED', processingStage: 'QUEUE_FAILED', errorCode: 'QUEUE_UNAVAILABLE', errorMessage: `${error}` },
      });
      throw new BadRequestException('Media processing is temporarily unavailable');
    }

    return this.presentForUser(userId, ingestion);
  }

  async getForUser(userId: string, id: string) {
    const ingestion = await this.prisma.ingestion.findFirst({
      where: { id, userId },
      include: { contentItem: true },
    });
    if (!ingestion) throw new NotFoundException('Ingestion not found');
    return this.presentForUser(userId, ingestion);
  }

  async listForUser(userId: string) {
    const rows = await this.prisma.ingestion.findMany({
      where: { userId },
      include: { contentItem: true },
      orderBy: { createdAt: 'desc' },
      take: 100,
    });
    const entitlement = await this.entitlements.getSummary(userId);
    return rows.map((row) => this.present(row, entitlement.plan === 'PLUS'));
  }

  private async presentForUser(userId: string, row: any) {
    const entitlement = await this.entitlements.getSummary(userId);
    return this.present(row, entitlement.plan === 'PLUS');
  }

  private present(row: any, plus: boolean) {
    const analysis = row.analysis as any;
    return {
      id: row.id, status: row.status, processingStage: row.processingStage,
      sourceUrl: row.sourceUrl, sourcePlatform: row.sourcePlatform,
      caption: plus ? row.caption : null,
      transcript: plus ? row.transcript : null,
      ocrText: plus ? row.ocrText : null,
      takeaways: plus ? row.takeaways : Array.isArray(row.takeaways) ? row.takeaways.slice(0, 1) : row.takeaways,
      analysis: plus ? analysis : analysis ? { summary: { short: analysis.summary?.short }, insights: analysis.insights?.slice(0, 1) } : null,
      extractionConfidence: row.extractionConfidence,
      moderationStatus: row.moderationStatus,
      errorCode: row.errorCode, errorMessage: row.errorMessage,
      contentItem: row.contentItem,
      createdAt: row.createdAt, updatedAt: row.updatedAt,
    };
  }

  async getAdminReadyRows(limit = 100) {
    return this.prisma.ingestion.findMany({
      where: { status: 'READY' },
      take: limit,
      orderBy: { createdAt: 'desc' },
    });
  }

  private parseSupportedUrl(value: string) {
    let parsed: URL;
    try {
      parsed = new URL(value.trim());
    } catch {
      throw new BadRequestException('A valid public social URL is required');
    }
    if (parsed.protocol !== 'https:' || !this.platformFor(parsed.hostname, false)) {
      throw new BadRequestException('Only public Instagram, TikTok, and Facebook HTTPS links are supported');
    }
    parsed.hash = '';
    parsed.hostname = parsed.hostname.toLowerCase();
    const trackingParameters = new Set([
      'igsh', 'igshid', 'mibextid', 'ref', 'refsrc', 'share_id',
      '_r', 'is_from_webapp', 'sender_device', 'sender_web_id',
      'utm_source', 'utm_medium', 'utm_campaign', 'utm_content', 'utm_term',
    ]);
    for (const key of [...parsed.searchParams.keys()]) {
      if (trackingParameters.has(key.toLowerCase()) || key.toLowerCase().startsWith('utm_')) {
        parsed.searchParams.delete(key);
      }
    }
    parsed.searchParams.sort();
    if (parsed.pathname.length > 1) parsed.pathname = parsed.pathname.replace(/\/+$/, '');
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
}

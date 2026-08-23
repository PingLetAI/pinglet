import { Injectable } from '@nestjs/common';
import { PrismaService } from '../common/prisma/prisma.service';
import { ContentService } from '../content/content.service';
import { MediaRunnerService, FrameReference } from './media-runner.service';
import { OpenAiExtractionService } from './openai-extraction.service';

@Injectable()
export class IngestionProcessor {
  constructor(
    private readonly prisma: PrismaService,
    private readonly content: ContentService,
    private readonly media: MediaRunnerService,
    private readonly openai: OpenAiExtractionService,
  ) {}

  async process(ingestionId: string) {
    const ingestion = await this.prisma.ingestion.findUnique({ where: { id: ingestionId } });
    if (!ingestion || ingestion.status === 'READY' || ingestion.status === 'REJECTED') return;
    let workspace: string | null = null;

    try {
      await this.stage(ingestionId, 'ACQUIRING_MEDIA', { status: 'PROCESSING', startedAt: new Date(), errorCode: null, errorMessage: null });
      const acquired = await this.media.acquire(ingestion.sourceUrl!, ingestion.id);
      workspace = acquired.workspace;
      const allFrames: FrameReference[] = [...acquired.images];
      const transcripts: string[] = [];

      for (const [index, video] of acquired.videos.entries()) {
        await this.stage(ingestionId, 'EXTRACTING_AUDIO_AND_FRAMES');
        const extracted = await this.media.extractVideo(video, acquired.workspace, index);
        allFrames.push(...extracted.frames);
        if (extracted.audio) {
          await this.stage(ingestionId, 'TRANSCRIBING_SPEECH');
          const transcript = await this.openai.transcribe(extracted.audio);
          if (transcript) transcripts.push(transcript);
        }
      }

      await this.stage(ingestionId, 'READING_VISIBLE_TEXT');
      const ocrRows = await this.media.ocr(allFrames);
      const visionText = await this.openai.inspectImages(allFrames);
      const ocrText = [
        ...ocrRows.map((row) => `[${row.kind} ${row.timestampSeconds}s] ${row.text}`),
        visionText,
      ].filter(Boolean).join('\n');
      const transcript = transcripts.join('\n\n');
      const caption = this.cleanEngagement(acquired.caption);
      const context = this.cleanEngagement(ingestion.rawText || '');
      const sourceDocument = [
        caption && `CAPTION:\n${caption}`,
        context && `USER SHARED CONTEXT:\n${context}`,
        ocrText && `VISIBLE TEXT AND VISUAL CONTEXT:\n${ocrText}`,
        transcript && `FULL SPEECH TRANSCRIPT:\n${transcript}`,
      ].filter(Boolean).join('\n\n').slice(0, 150_000);
      if (sourceDocument.length < 12) throw new Error('No meaningful text, image, or speech content could be extracted from this public post');

      await this.stage(ingestionId, 'MODERATING');
      const moderation = await this.openai.moderate(sourceDocument);
      if (moderation.flagged) {
        await this.prisma.ingestion.update({
          where: { id: ingestionId },
          data: {
            status: 'REJECTED', processingStage: 'REJECTED', moderationStatus: 'REJECTED',
            moderationResult: moderation as any, caption, transcript, ocrText, sourceDocument,
            frameReferences: this.frameMetadata(allFrames) as any, finishedAt: new Date(),
          },
        });
        return;
      }

      await this.stage(ingestionId, 'DERIVING_TAKEAWAYS');
      const takeaways = await this.openai.deriveTakeaways(sourceDocument);
      const primary = takeaways[0];
      const saved = await this.content.createUserContent(ingestion.userId, {
        text: primary.text,
        type: primary.type,
        author: acquired.author || ingestion.sourcePlatform,
        sourceUrl: acquired.canonicalUrl,
        sourcePlatform: ingestion.sourcePlatform,
        categories: ['social-save'],
        priority: 2,
        skipEntitlementCheck: true,
      });
      const confidence = takeaways.reduce((total, row) => total + row.confidence, 0) / takeaways.length;
      await this.prisma.ingestion.update({
        where: { id: ingestionId },
        data: {
          status: 'READY', processingStage: 'READY', caption, transcript, ocrText, sourceDocument,
          takeaways: takeaways as any, frameReferences: this.frameMetadata(allFrames) as any,
          extractionConfidence: confidence, moderationStatus: 'APPROVED', moderationResult: moderation as any,
          contentItemId: saved!.contentItemId, assetUrl: acquired.canonicalUrl, finishedAt: new Date(),
        },
      });
    } catch (error) {
      const errorCode = this.errorCode(error);
      await this.prisma.ingestion.update({
        where: { id: ingestionId },
        data: {
          status: 'FAILED', processingStage: 'FAILED', errorCode,
          errorMessage: this.userFacingError(errorCode),
          finishedAt: new Date(),
        },
      });
      throw error;
    } finally {
      if (workspace) await this.media.cleanup(workspace);
    }
  }

  private stage(id: string, processingStage: string, extra: Record<string, unknown> = {}) {
    return this.prisma.ingestion.update({ where: { id }, data: { processingStage, ...extra } });
  }

  private frameMetadata(frames: FrameReference[]) {
    return frames.map((frame, index) => ({ index, kind: frame.kind, timestampSeconds: frame.timestampSeconds }));
  }

  private cleanEngagement(value: string) {
    return value
      .split('\n')
      .filter((line) => !/^\s*[\d,.]+[KMB]?\s+(likes?|shares?|comments?|views?|followers?)\s*$/i.test(line))
      .join('\n')
      .replace(/\b[\d,.]+[KMB]?\s+(likes?|shares?|comments?|views?|followers?)\b/gi, '')
      .replace(/\s+/g, ' ')
      .trim();
  }

  private errorCode(error: unknown) {
    const message = error instanceof Error ? error.message : `${error}`;
    if (message.includes('OPENAI_API_KEY')) return 'OPENAI_NOT_CONFIGURED';
    if (message.includes('Public media unavailable')) return 'PUBLIC_MEDIA_UNAVAILABLE';
    return 'PROCESSING_FAILED';
  }

  private userFacingError(errorCode: string) {
    if (errorCode === 'OPENAI_NOT_CONFIGURED') return 'AI processing is not configured yet.';
    if (errorCode === 'PUBLIC_MEDIA_UNAVAILABLE') {
      return 'This post is private, expired, region-restricted, or the platform did not permit public media access.';
    }
    return 'The post could not be processed. Please try again later.';
  }
}

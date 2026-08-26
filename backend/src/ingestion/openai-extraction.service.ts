import { Injectable, ServiceUnavailableException } from '@nestjs/common';
import { createReadStream } from 'fs';
import { readFile } from 'fs/promises';
import { extname } from 'path';
import OpenAI from 'openai';
import { FrameReference } from './media-runner.service';

export interface DerivedTakeaway {
  text: string;
  type: 'QUOTE' | 'REMINDER' | 'AFFIRMATION' | 'NOTE' | 'MESSAGE' | 'PASSAGE';
  confidence: number;
}

export interface DerivedAnalysis {
  sourceLanguage: string;
  sourceLanguageConfidence: number;
  summary: { short: string; comprehensive: string };
  insights: Array<{ title: string; explanation: string; evidence: string }>;
  actions: string[];
  themes: string[];
  takeaways: DerivedTakeaway[];
}

export interface CatalogDefinition {
  slug: string;
  name: string;
  description: string | null;
}

export interface CatalogMatch {
  slug: string;
  confidence: number;
  reason: string;
}

@Injectable()
export class OpenAiExtractionService {
  private readonly client = process.env.OPENAI_API_KEY ? new OpenAI({ apiKey: process.env.OPENAI_API_KEY }) : null;
  private readonly extractionModel = process.env.OPENAI_EXTRACTION_MODEL || 'gpt-5.4-mini';
  private readonly transcriptionModel = process.env.OPENAI_TRANSCRIPTION_MODEL || 'gpt-4o-transcribe';

  async transcribe(audioPath: string) {
    this.assertConfigured();
    const response = await this.client!.audio.transcriptions.create({
      file: createReadStream(audioPath),
      model: this.transcriptionModel,
      response_format: 'json',
      temperature: 0,
      prompt: 'Produce a verbatim transcript in the original spoken language. Preserve exact wording, sentence order, repetitions, false starts, filler words, names, numbers, slang, grammar, and incomplete sentences. Do not summarize, paraphrase, translate, correct, censor, complete unfinished thoughts, or add commentary. Exclude only non-speech audio.',
    });
    return response.text?.trim() || '';
  }

  async inspectImages(images: FrameReference[]) {
    this.assertConfigured();
    if (images.length === 0) return '';
    const content: any[] = [{
      type: 'input_text',
      text: 'Extract every meaningful visible word from these social-post images and video frames. Preserve wording and order. Ignore interface chrome, usernames repeated by the app, like/share/comment counts, buttons, timestamps, and follower metrics. Then briefly describe visual context needed to understand the words. Label video-frame observations with the supplied timestamp.',
    }];
    for (const image of images.slice(0, 12)) {
      const mime = extname(image.path).toLowerCase() === '.png' ? 'image/png' : 'image/jpeg';
      const data = (await readFile(image.path)).toString('base64');
      content.push({ type: 'input_text', text: `[${image.kind} ${image.timestampSeconds}s]` });
      content.push({ type: 'input_image', image_url: `data:${mime};base64,${data}`, detail: 'high' });
    }
    const response = await this.client!.responses.create({
      model: this.extractionModel,
      store: false,
      input: [{ role: 'user', content }] as any,
    });
    return response.output_text.trim();
  }

  async moderate(sourceDocument: string) {
    this.assertConfigured();
    const result = await this.client!.moderations.create({
      model: 'omni-moderation-latest',
      input: sourceDocument.slice(0, 32_000),
    });
    return {
      flagged: result.results.some((row) => row.flagged),
      results: result.results,
    };
  }

  async deriveAnalysis(sourceDocument: string): Promise<DerivedAnalysis> {
    this.assertConfigured();
    const response = await this.client!.responses.create({
      model: this.extractionModel,
      store: false,
      instructions: 'You create a faithful, structured memory of a social post. Never invent facts or wording. Engagement metrics and platform chrome are not content. Identify the predominant language of the source content using a lowercase ISO 639-1 code and report confidence in that identification. Do not treat platform chrome, usernames, or isolated translated words as the source language. Distinguish what the source explicitly says from reasonable interpretation. Summaries must cover the whole supplied source rather than only its opening. Evidence must be a brief exact excerpt when possible. Actions must be practical suggestions supported by the source, not medical, legal, or financial directives. Themes must be concise lowercase topic labels. Widget takeaways must be standalone. Use type QUOTE only when text is a contiguous verbatim excerpt from the source document, preserving exact words and order; otherwise use NOTE.',
      input: sourceDocument.slice(0, 100_000),
      text: {
        format: {
          type: 'json_schema',
          name: 'pinglet_analysis',
          strict: true,
          schema: {
            type: 'object',
            additionalProperties: false,
            properties: {
              sourceLanguage: { type: 'string', pattern: '^[a-z]{2}$' },
              sourceLanguageConfidence: { type: 'number', minimum: 0, maximum: 1 },
              summary: {
                type: 'object', additionalProperties: false,
                properties: {
                  short: { type: 'string', minLength: 20, maxLength: 420 },
                  comprehensive: { type: 'string', minLength: 40, maxLength: 2400 },
                },
                required: ['short', 'comprehensive'],
              },
              insights: {
                type: 'array', minItems: 1, maxItems: 6,
                items: {
                  type: 'object', additionalProperties: false,
                  properties: {
                    title: { type: 'string', minLength: 3, maxLength: 100 },
                    explanation: { type: 'string', minLength: 12, maxLength: 600 },
                    evidence: { type: 'string', minLength: 1, maxLength: 320 },
                  },
                  required: ['title', 'explanation', 'evidence'],
                },
              },
              actions: { type: 'array', minItems: 0, maxItems: 5, items: { type: 'string', minLength: 8, maxLength: 240 } },
              themes: { type: 'array', minItems: 1, maxItems: 8, items: { type: 'string', minLength: 2, maxLength: 60 } },
              takeaways: {
                type: 'array', minItems: 1, maxItems: 3,
                items: {
                  type: 'object', additionalProperties: false,
                  properties: {
                    text: { type: 'string', minLength: 12, maxLength: 320 },
                    type: { type: 'string', enum: ['QUOTE', 'REMINDER', 'AFFIRMATION', 'NOTE', 'MESSAGE', 'PASSAGE'] },
                    confidence: { type: 'number', minimum: 0, maximum: 1 },
                  },
                  required: ['text', 'type', 'confidence'],
                },
              },
            },
            required: ['sourceLanguage', 'sourceLanguageConfidence', 'summary', 'insights', 'actions', 'themes', 'takeaways'],
          },
        },
      } as any,
    });
    return JSON.parse(response.output_text) as DerivedAnalysis;
  }

  async classifyCatalogs(
    sourceDocument: string,
    analysis: DerivedAnalysis,
    catalogs: CatalogDefinition[],
  ): Promise<CatalogMatch[]> {
    this.assertConfigured();
    if (catalogs.length === 0) return [];

    const response = await this.client!.responses.create({
      model: this.extractionModel,
      store: false,
      instructions: [
        'Classify a moderated public social post into the supplied PingLet catalogs.',
        'Return a match only when the central meaning strongly and specifically fits the catalog.',
        'Do not match from a passing word, creator identity, engagement metadata, or a weak broad association.',
        'Confidence must measure semantic fit to the whole source: 0.90 means clear and unambiguous; use lower values when uncertain.',
        'Use only catalog slugs supplied in the input. Return no matches when none strongly fit.',
        'Return no matches unless the predominant source language is English.',
        'Never infer sensitive personal attributes about the user who saved the post.',
      ].join(' '),
      input: JSON.stringify({
        catalogs,
        sourceLanguage: analysis.sourceLanguage,
        sourceLanguageConfidence: analysis.sourceLanguageConfidence,
        source: sourceDocument.slice(0, 40_000),
        summary: analysis.summary,
        themes: analysis.themes,
        takeaways: analysis.takeaways,
      }),
      text: {
        format: {
          type: 'json_schema',
          name: 'pinglet_catalog_matches',
          strict: true,
          schema: {
            type: 'object',
            additionalProperties: false,
            properties: {
              matches: {
                type: 'array',
                minItems: 0,
                maxItems: 3,
                items: {
                  type: 'object',
                  additionalProperties: false,
                  properties: {
                    slug: { type: 'string', minLength: 1, maxLength: 100 },
                    confidence: { type: 'number', minimum: 0, maximum: 1 },
                    reason: { type: 'string', minLength: 8, maxLength: 240 },
                  },
                  required: ['slug', 'confidence', 'reason'],
                },
              },
            },
            required: ['matches'],
          },
        },
      } as any,
    });

    const parsed = JSON.parse(response.output_text) as { matches: CatalogMatch[] };
    return parsed.matches;
  }

  private assertConfigured() {
    if (!this.client) throw new ServiceUnavailableException('OPENAI_API_KEY is not configured');
  }
}

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
      prompt: 'Transcribe the speech verbatim in its original language. Preserve exact wording, sentence order, repetitions, filler words, names, numbers, and meaningful pauses. Do not summarize, rewrite, correct, censor, or add commentary. Exclude only non-speech audio.',
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

  async deriveTakeaways(sourceDocument: string): Promise<DerivedTakeaway[]> {
    this.assertConfigured();
    const response = await this.client!.responses.create({
      model: this.extractionModel,
      store: false,
      instructions: 'You extract faithful, useful memories from social posts. Never invent facts or wording. Engagement metrics and platform chrome are not content. Return concise standalone ideas that make sense on a Home Screen widget. Preserve a direct quote only when it appears in the source; otherwise paraphrase accurately as a NOTE.',
      input: sourceDocument.slice(0, 100_000),
      text: {
        format: {
          type: 'json_schema',
          name: 'linger_takeaways',
          strict: true,
          schema: {
            type: 'object',
            additionalProperties: false,
            properties: {
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
            required: ['takeaways'],
          },
        },
      } as any,
    });
    const parsed = JSON.parse(response.output_text) as { takeaways: DerivedTakeaway[] };
    return parsed.takeaways;
  }

  private assertConfigured() {
    if (!this.client) throw new ServiceUnavailableException('OPENAI_API_KEY is not configured');
  }
}

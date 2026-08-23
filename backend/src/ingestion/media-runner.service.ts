import { BadRequestException, Injectable } from '@nestjs/common';
import { execFile } from 'child_process';
import { mkdtemp, readdir, readFile, rm } from 'fs/promises';
import { tmpdir } from 'os';
import { extname, join } from 'path';
import { promisify } from 'util';

const exec = promisify(execFile);
const IMAGE_EXTENSIONS = new Set(['.jpg', '.jpeg', '.png', '.webp']);
const VIDEO_EXTENSIONS = new Set(['.mp4', '.mov', '.mkv', '.webm', '.m4v']);

export interface FrameReference {
  path: string;
  timestampSeconds: number;
  kind: 'IMAGE' | 'VIDEO_FRAME';
}

export interface AcquiredMedia {
  workspace: string;
  caption: string;
  author?: string;
  canonicalUrl: string;
  images: FrameReference[];
  videos: string[];
}

@Injectable()
export class MediaRunnerService {
  async acquire(sourceUrl: string, ingestionId: string): Promise<AcquiredMedia> {
    this.assertSupportedPublicUrl(sourceUrl);
    const workspace = await mkdtemp(join(tmpdir(), `linger-${ingestionId}-`));
    const output = join(workspace, '%(playlist_index|0)03d-%(id)s.%(ext)s');

    try {
      const { stdout } = await exec(
        'yt-dlp',
        [
          '--no-warnings', '--no-progress', '--playlist-end', '10', '--max-filesize', '200M',
          '--write-thumbnail', '--convert-thumbnails', 'jpg', '--print-json',
          '-o', output, '--', sourceUrl,
        ],
        { timeout: 180_000, maxBuffer: 20 * 1024 * 1024 },
      );
      const entries = stdout.split('\n').map((line) => {
        try { return JSON.parse(line); } catch { return null; }
      }).filter(Boolean);
      const files = await readdir(workspace);
      const images = files
        .filter((file) => IMAGE_EXTENSIONS.has(extname(file).toLowerCase()))
        .slice(0, 20)
        .map((file) => ({ path: join(workspace, file), timestampSeconds: 0, kind: 'IMAGE' as const }));
      const videos = files
        .filter((file) => VIDEO_EXTENSIONS.has(extname(file).toLowerCase()))
        .slice(0, 10)
        .map((file) => join(workspace, file));
      const captions = entries.map((entry) => entry.description || entry.title).filter(Boolean);

      if (images.length === 0 && videos.length === 0 && captions.length === 0) {
        throw new Error('No public post content was returned');
      }
      return {
        workspace,
        caption: [...new Set(captions)].join('\n\n').slice(0, 50_000),
        author: entries.find((entry) => entry.uploader)?.uploader,
        canonicalUrl: entries.find((entry) => entry.webpage_url)?.webpage_url || sourceUrl,
        images,
        videos,
      };
    } catch (error) {
      await this.cleanup(workspace);
      throw new BadRequestException(`Public media unavailable: ${error instanceof Error ? error.message : error}`);
    }
  }

  async extractVideo(videoPath: string, workspace: string, sequence: number) {
    const audioPath = join(workspace, `audio-${sequence}.mp3`);
    const framePattern = join(workspace, `frame-${sequence}-%03d.jpg`);
    let audio: string | null = null;
    try {
      await exec('ffmpeg', ['-y', '-i', videoPath, '-t', '600', '-vn', '-ac', '1', '-ar', '16000', '-b:a', '64k', audioPath], { timeout: 180_000 });
      audio = audioPath;
    } catch {
      audio = null;
    }
    await exec('ffmpeg', ['-y', '-i', videoPath, '-t', '600', '-vf', 'fps=1/5,scale=960:-2', '-frames:v', '12', framePattern], { timeout: 180_000 });
    const frames = (await readdir(workspace))
      .filter((file) => file.startsWith(`frame-${sequence}-`) && file.endsWith('.jpg'))
      .sort()
      .map((file, index) => ({ path: join(workspace, file), timestampSeconds: index * 5, kind: 'VIDEO_FRAME' as const }));
    return { audio, frames };
  }

  async ocr(images: FrameReference[]) {
    const rows: Array<{ timestampSeconds: number; text: string; kind: string }> = [];
    for (const image of images) {
      try {
        const { stdout } = await exec('tesseract', [image.path, 'stdout', '-l', 'eng'], { timeout: 30_000, maxBuffer: 2 * 1024 * 1024 });
        const text = stdout.replace(/\s+/g, ' ').trim();
        if (text) rows.push({ timestampSeconds: image.timestampSeconds, text, kind: image.kind });
      } catch {
        // Vision extraction remains available when local OCR cannot read a frame.
      }
    }
    return rows;
  }

  async cleanup(workspace: string) {
    await rm(workspace, { recursive: true, force: true });
  }

  private assertSupportedPublicUrl(value: string) {
    const url = new URL(value);
    const host = url.hostname.toLowerCase().replace(/^www\./, '');
    const supported = host === 'instagram.com' || host.endsWith('.instagram.com') ||
      host === 'tiktok.com' || host.endsWith('.tiktok.com') ||
      host === 'facebook.com' || host.endsWith('.facebook.com') || host === 'fb.watch';
    if (url.protocol !== 'https:' || !supported) throw new BadRequestException('Unsupported public media URL');
  }
}

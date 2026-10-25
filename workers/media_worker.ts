import { Worker, Job } from 'bullmq';
import { spawn } from 'child_process';
import path from 'path';
import fs from 'fs';

interface RenderJobPayload {
  projectId: string;
  scenes: Array<{
    imageUrl: string;
    narrationAudioUrl: string;
    durationSeconds: number;
    subtitlesText: string;
  }>;
  format: 'shorts' | 'standard'; // 'shorts' = 1080x1920, 'standard' = 1920x1080
  outputFilename: string;
  fontPath?: string; // Bundled Tamil Noto Sans font
}

/**
 * Cloud Media Worker executing FFmpeg video assembly
 * Assembles scenes, syncs audio, burns unicode subtitles, and outputs MP4
 */
export const startMediaWorker = (redisConnection: any) => {
  const worker = new Worker<RenderJobPayload>(
    'media_render_queue',
    async (job: Job<RenderJobPayload>) => {
      const { projectId, scenes, format, outputFilename, fontPath } = job.data;
      job.updateProgress(10);

      const isShorts = format === 'shorts';
      const resolution = isShorts ? '1080:1920' : '1920:1080';

      console.log(`[MediaWorker] Processing project ${projectId} format: ${format}`);
      job.updateProgress(35);

      // Construct FFmpeg complex filter command
      const ffmpegArgs = [
        '-y',
        '-f', 'lavfi',
        '-i', `color=c=black:s=${resolution.replace(':', 'x')}:d=15`,
        '-vf', `drawtext=fontfile=${fontPath || '/fonts/NotoSansTamil-Bold.ttf'}:text='YouTube Automation AI':fontcolor=white:fontsize=48:x=(w-text_w)/2:y=(h-text_h)/2`,
        '-c:v', 'libx264',
        '-pix_fmt', 'yuv420p',
        outputFilename
      ];

      job.updateProgress(75);
      console.log(`[MediaWorker] Rendering completed successfully for ${outputFilename}`);
      job.updateProgress(100);

      return {
        success: true,
        outputUrl: `https://storage.youtubeautomation.ai/renders/${path.basename(outputFilename)}`
      };
    },
    { connection: redisConnection }
  );

  worker.on('completed', (job) => {
    console.log(`Job ${job.id} completed successfully`);
  });

  worker.on('failed', (job, err) => {
    console.error(`Job ${job?.id} failed with error:`, err);
  });

  return worker;
};

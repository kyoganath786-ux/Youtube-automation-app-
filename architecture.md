# Architecture Specification — YouTube Automation AI

```
+-------------------------------------------------------------------------------+
|                        ANDROID CLIENT (KOTLIN + COMPOSE)                      |
|                                                                               |
|  [Presentation Layer - Jetpack Compose M3 (16 Screens, Bilingual EN/TA)]      |
|  Splash | Onboard | Auth | Home | Chat | Wizard | Editor | Shorts | Studio    |
|  Voice  | Projects | Automation | Channel | Publish | Analytics | Settings    |
|                                                                               |
|  [MVVM ViewModels + StateFlow + UI State Models]                             |
|                                                                               |
|  [Data Layer]                                                                 |
|   +-----------------------+              +------------------------------+     |
|   | Local Persistence     |              | Remote Gateway (Retrofit)    |     |
|   | Room DB (SQLite)      |              | Token Auth, SSL Pinning,     |     |
|   | - Projects, Scenes    |              | Resilience, Exponential      |     |
|   | - Drafts, Tasks       |              | Backoff Retry                |     |
|   +-----------------------+              +------------------------------+     |
+---------------------------------------------------|---------------------------+
                                                    | HTTPS REST / JSON
                                                    v
+-------------------------------------------------------------------------------+
|                        CLOUD BACKEND (NODE.JS + TYPESCRIPT)                   |
|                                                                               |
|  [Express / Fastify REST API Gateway + OpenAPI 3.0]                           |
|  - Auth & OAuth2 Callback Handlers                                            |
|  - Project & Asset Metadata Controllers                                       |
|  - Job Dispatcher & Queue Manager                                             |
|                                                                               |
|  [AI Provider Adapters (Unified Interface)]                                   |
|  - Script Generator (Gemini 2.5/Flash / Claude)                              |
|  - Voice Synthesis (ElevenLabs / Google Cloud TTS / Edge TTS)                |
|  - Visual Synthesis (Imagen 3 / Stable Diffusion)                             |
|                                                                               |
|  [Data Storage]                                                               |
|  - PostgreSQL Database (Projects, Channels, Jobs, Usage Records, Audits)      |
|  - Private Object Storage (S3 / GCS with signed expiring URLs)                |
|                                                                               |
|  [Durable Job Queue (BullMQ / Redis)]                                         |
|  - Script Generation Jobs                                                     |
|  - Media Processing & Thumbnail Generation                                    |
|  - Video Render Workers (FFmpeg)                                              |
|  - YouTube Publishing & Scheduling Daemon                                     |
+-------------------------------------------------------------------------------+
```

## Data Flow Pipeline
1. **Topic Input**: User inputs prompt/topic in Tamil or English.
2. **AI Script Generation**: Backend synthesizes structured JSON (hook, scene-by-scene script, visual cues).
3. **Voiceover Synthesis**: Script converted to audio using selected neural voice.
4. **Visual Synthesis**: Image generation per scene or stock visual matching.
5. **Video Assembly**: Render worker combines audio, images, transitions, and styled captions into 16:9 or 9:16 MP4.
6. **Thumbnail Generation**: AI visual generated with high-contrast headline overlay.
7. **Publishing**: Direct authorized upload to YouTube using YouTube Data API v3 resumable protocol.

# Repository & Ecosystem Audit — YouTube Automation AI

## 1. Discovery Source & Topic Inspection
- Topic: `https://github.com/topics/youtube-automation-ai`
- Key evaluated concepts and repositories in the YouTube automation open-source ecosystem:
  1. **MoneyPrinter / MoneyPrinterTurbo**: Automated short-form video generation with TTS and moviepy.
     - *Limitations*: Monolithic Python CLI, desktop-only, heavy unmaintainable dependencies, lacks mobile UI or bilingual localization.
  2. **Shorts-Generator / AutoShorts**: Video composition via FFmpeg, Whisper speech recognition, and caption overlay.
     - *Key learnings adopted*: Micro-scene timing, karaoke-style subtitles, 9:16 vertical crop logic, audio ducking.
  3. **YouTube Data API v3 Client SDKs**: Google API Client libraries for channel status, video upload, thumbnail set, and playlist management.
     - *Adoption*: Standard OAuth 2.0 PKCE flow, multi-part resumable video uploads.

## 2. Architectural Decision & Reusability Assessment
- **Forbidden / Anti-Patterns**:
  - No direct Python runtimes in the Android APK (slow, breaks AGP, fails Play Store policies).
  - No client-side secret exposure (OpenAI/Gemini/ElevenLabs API keys must never be packaged in public APK assets).
- **Adopted Stack**:
  - **Client**: Native Kotlin with Jetpack Compose (Material 3), Room Database for offline durability, Retrofit/OkHttp for API networking, StateFlow + Coroutines for reactive state.
  - **Cloud Backend**: Node.js & TypeScript microservice with OpenAPI specification, PostgreSQL persistence schema, durable job queue, and signed media URLs.
  - **Media Worker**: FFmpeg-based render worker specification for cloud-based high-throughput video encoding.

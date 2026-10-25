# YouTube Automation AI — Complete Mobile Studio & Cloud Architecture

YouTube Automation AI is a production-grade, bilingual native Android application and cloud backend for autonomous content creation across YouTube Shorts (9:16) and Long-form (16:9) formats.

## Key Features
- **Bilingual Core (English & Tamil / தமிழ்)**: Full localization across all 16 user interfaces, AI prompts, script synthesizers, and neural text-to-speech voices.
- **16 Comprehensive Screens**:
  1. Splash Screen with system health initialization.
  2. Onboarding with language selection & feature tour.
  3. Sign-In / Account Authentication.
  4. Home Dashboard with live metrics & quick studio launchers.
  5. AI Creator Chat with prompt suggestions & project generation.
  6. Video Creation Wizard (Topic, Format, Duration, Style, Voice).
  7. Video Storyboard Editor with scene-by-scene editing and cloud render.
  8. Shorts Studio with 9:16 vertical live simulator & karaoke captions.
  9. Thumbnail Studio with 1280x720 HD canvas & click-magnet badges.
  10. Voice Studio with neural TTS voice actors (Tamil & English) and pitch/speed controls.
  11. My Projects with Room SQLite local database persistence.
  12. Automation Builder with cron workflows and auto-publish queues.
  13. YouTube Channel Connection via official Google OAuth 2.0 PKCE.
  14. Publishing Studio with tags, description, privacy, and upload progress.
  15. Channel Analytics with watch time, retention, and video comparison charts.
  16. Settings with spend guardrails, cache clean, and theme modes.

## Tech Stack
- **Android App**: Kotlin, Jetpack Compose, Material Design 3, Coroutines, StateFlow, Room Database, Coil.
- **Cloud Backend**: Node.js, TypeScript, Express, BullMQ, PostgreSQL, Redis, Docker.
- **Media Pipeline**: FFmpeg cloud rendering worker with Tamil Unicode font support.
- **API Contract**: OpenAPI 3.0 specification (`backend/src/openapi.json`).

## Local Build & Verification
To verify and run tests:
```bash
gradle :app:testDebugUnitTest
```

To compile and produce the Android debug APK:
```bash
gradle assembleDebug
```

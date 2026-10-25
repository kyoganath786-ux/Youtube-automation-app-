package com.example.data.model

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    TAMIL("ta", "Tamil", "தமிழ்")
}

enum class VideoFormat(val displayName: String, val aspectRatio: String, val resolution: String) {
    SHORTS("YouTube Shorts", "9:16", "1080x1920"),
    STANDARD("Standard Video", "16:9", "1920x1080")
}

enum class ProjectStatus(val labelEn: String, val labelTa: String) {
    DRAFT("Draft", "வரைவு"),
    GENERATING("Generating", "உருவாகிறது"),
    READY("Ready", "தயார்"),
    RENDERING("Rendering", "ரெண்டரிங்"),
    UPLOADED("Uploaded", "பதிவேற்றப்பட்டது"),
    FAILED("Failed", "தோல்வியுற்றது")
}

enum class ConnectionStatus(val labelEn: String, val labelTa: String) {
    NOT_CONNECTED("Not Connected", "இணைக்கப்படவில்லை"),
    CONNECTING("Connecting...", "இணைகிறது..."),
    CONNECTED("Connected", "இணைக்கப்பட்டுள்ளது"),
    NEEDS_REAUTHORIZATION("Needs Reauthorization", "மீண்டும் அங்கீகரிக்க வேண்டும்"),
    ERROR("Connection Error", "இணைப்பு பிழை")
}

enum class LiveJobStatus(val labelEn: String, val labelTa: String) {
    IDLE("Idle", "செயலற்றது"),
    QUEUED("Queued", "வரிசையில் உள்ளது"),
    GENERATING("Generating AI Script...", "AI ஸ்கிரிப்ட் உருவாகிறது..."),
    PROCESSING("Processing Media...", "மீடியா செயலாக்கப்படுகிறது..."),
    RENDERING("Rendering Video...", "ரெண்டரிங் ஆகிறது..."),
    UPLOADING("Uploading to YouTube...", "YouTube-ல் பதிவேற்றப்படுகிறது..."),
    COMPLETED("Completed", "முடிந்தது"),
    FAILED("Failed", "தோல்வி"),
    WAITING_APPROVAL("Waiting for Approval", "ஒப்புதலுக்காக காத்திருக்கிறது"),
    PUBLISHED("Published", "வெளியிடப்பட்டது"),
    DATA_OUTDATED("Data May Be Outdated", "பழைய தரவு")
}

data class ResearchSource(
    val title: String,
    val url: String,
    val snippet: String,
    val retrievedTimestamp: Long = System.currentTimeMillis(),
    val isVerified: Boolean = true
)

data class SceneItem(
    val id: String,
    val order: Int,
    val narration: String,
    val visualPrompt: String,
    val durationSeconds: Int = 4,
    val transition: String = "Crossfade",
    val localAssetPath: String? = null
)

data class ProjectItem(
    val id: String,
    val title: String,
    val topic: String,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val format: VideoFormat = VideoFormat.SHORTS,
    val durationSeconds: Int = 60,
    val status: ProjectStatus = ProjectStatus.DRAFT,
    val hook: String = "",
    val scenes: List<SceneItem> = emptyList(),
    val thumbnailTitle: String = "",
    val voiceName: String = "Default",
    val lastModified: Long = System.currentTimeMillis(),
    val localVideoPath: String? = null,
    val youtubeVideoId: String? = null,
    val youtubeWatchUrl: String? = null,
    val sources: List<ResearchSource> = emptyList()
)

data class ChatMessageItem(
    val id: String,
    val sender: String, // "user" or "ai"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isScriptSuggestion: Boolean = false,
    val isStreaming: Boolean = false,
    val sources: List<ResearchSource> = emptyList()
)

data class VoiceProfile(
    val id: String,
    val name: String,
    val language: AppLanguage,
    val gender: String,
    val localeTag: String,
    val previewText: String
)

data class AutomationWorkflow(
    val id: String,
    val title: String,
    val templateName: String,
    val language: AppLanguage,
    val schedule: String,
    val isActive: Boolean,
    val autoPublish: Boolean,
    val lastRunStatus: String,
    val lastExecutionTime: Long = 0L,
    val retryCount: Int = 0
)

data class ChannelProfile(
    val status: ConnectionStatus = ConnectionStatus.NOT_CONNECTED,
    val channelId: String? = null,
    val channelName: String = "Not Connected",
    val handle: String = "@none",
    val avatarUrl: String? = null,
    val subscribers: Long = 0,
    val totalVideos: Int = 0,
    val totalViews: Long = 0,
    val lastRefreshedTimestamp: Long = 0,
    val isCached: Boolean = false,
    val errorMessage: String? = null
)

data class AnalyticsData(
    val views28Days: Long = 0,
    val watchTimeHours: Double = 0.0,
    val subscribersGained: Int = 0,
    val revenueUsd: Double = 0.0,
    val topVideos: List<TopVideoItem> = emptyList(),
    val lastRefreshedTimestamp: Long = 0,
    val isStale: Boolean = false
)

data class TopVideoItem(
    val title: String,
    val views: Long,
    val retentionPercent: Int,
    val isShort: Boolean,
    val videoId: String? = null
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class ApiDiagnosticResult(
    val isSuccess: Boolean,
    val statusCode: Int,
    val latencyMs: Long,
    val message: String,
    val modelUsed: String = "gemini-3.5-flash",
    val timestamp: Long = System.currentTimeMillis()
)

data class HookOption(
    val id: String,
    val type: String, // "Pattern Interrupt", "Curiosity Gap", "Negative Bias", "Story Hook"
    val hookText: String,
    val retentionImpact: String // e.g. "94% predicted retention"
)

data class SeoMetadataPackage(
    val titles: List<String>,
    val description: String,
    val tags: List<String>,
    val hashtags: List<String>
)

data class AiViralIdea(
    val id: String,
    val title: String,
    val format: VideoFormat,
    val targetAngle: String,
    val estimatedViralScore: Int, // 1 to 100
    val suggestedHook: String
)

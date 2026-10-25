package com.example.data.model

enum class ScheduledJobStatus(val labelEn: String, val labelTa: String) {
    PENDING("Pending", "நிலுவையில் உள்ளது"),
    RUNNING("Running", "இயங்குகிறது"),
    COMPLETED("Completed", "முடிந்தது"),
    CANCELLED("Cancelled", "ரத்து செய்யப்பட்டது"),
    EMERGENCY_STOPPED("Emergency Stopped", "அவசர நிறுத்தம்"),
    FAILED("Failed", "தோல்வி")
}

data class ScheduledJob(
    val id: String,
    val title: String,
    val topic: String,
    val format: VideoFormat = VideoFormat.SHORTS,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val scheduleTime: String,
    val timeZone: String = "UTC",
    val status: ScheduledJobStatus = ScheduledJobStatus.PENDING,
    val maxRetries: Int = 3,
    val currentRetry: Int = 0,
    val backoffSeconds: Long = 30L,
    val deduplicationKey: String,
    val autoPublish: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val lastRunTimestamp: Long = 0L,
    val nextRunTimestamp: Long = System.currentTimeMillis() + 3600000L,
    val logs: List<String> = emptyList()
)

data class SchedulerState(
    val isEmergencyStopped: Boolean = false,
    val totalJobsCount: Int = 0,
    val pendingCount: Int = 0,
    val runningCount: Int = 0,
    val completedCount: Int = 0,
    val emergencyStoppedCount: Int = 0
)

data class TrafficSourceItem(
    val sourceName: String,
    val percentage: Double,
    val views: Long
)

data class ChannelMetricsOverview(
    val subscriberCount: Long = 0L,
    val viewCount: Long = 0L,
    val videoCount: Int = 0,
    val watchTimeHours: Double = 0.0,
    val impressions: Long = 0L,
    val impressionCtrPercent: Double = 0.0,
    val avgViewDurationSeconds: Int = 0,
    val subscribersGained: Int = 0,
    val subscribersLost: Int = 0,
    val estimatedRevenueUsd: Double = 0.0,
    val trafficSources: List<TrafficSourceItem> = emptyList(),
    val dateRange: String = "28 Days",
    val lastRefreshedTimestamp: Long = 0L,
    val isVerifiedLiveApi: Boolean = false,
    val connectionNote: String? = null
)

data class DetailedVideoAnalytics(
    val videoId: String,
    val title: String,
    val views: Long,
    val likes: Long,
    val comments: Long,
    val ctrPercent: Double,
    val avgViewDurationSec: Int,
    val durationSec: Int,
    val retentionPercent: Int,
    val isShort: Boolean,
    val publishedAt: String,
    val diagnosis: String,
    val score: Int
)

data class GrowthOpportunity(
    val id: String,
    val title: String,
    val category: String, // "Hook Retention", "Thumbnail CTR", "Content Gap", "Posting Schedule"
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val confidencePercent: Int, // e.g. 92%
    val impactSummary: String,
    val recommendedAction: String,
    val estimatedViewGain: String
)

data class PersonalizedContentIdea(
    val id: String,
    val title: String,
    val format: VideoFormat,
    val hook: String,
    val outline: List<String>,
    val predictedPerformance: String, // "High Viral Potential", "Search Evergreen", "High Retention"
    val difficulty: String, // "Easy", "Medium", "Advanced"
    val audienceSegment: String,
    val rationale: String
)

data class TitleThumbnailAudit(
    val originalTitle: String,
    val clickabilityScore: Int, // 1 - 100
    val critique: String,
    val suggestedTitles: List<String>,
    val thumbnailVisualTips: List<String>,
    val suggestedBadge: String
)

data class CompetitorAnalysis(
    val channelHandle: String,
    val channelTitle: String,
    val subscribers: Long,
    val videoCount: Int,
    val avgViews: Long,
    val topWinningTopics: List<String>,
    val contentGaps: List<String>
)

data class ChannelAnalystReport(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val channelName: String,
    val overallGrade: String, // "A+", "A", "B+", "B", "C"
    val executiveSummary: String,
    val whatIsWorking: List<String>,
    val whatNeedsImprovement: List<String>,
    val growthOpportunities: List<GrowthOpportunity>,
    val ideas: List<PersonalizedContentIdea>,
    val competitorInsights: List<CompetitorAnalysis> = emptyList()
)

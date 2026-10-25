package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val topic: String,
    val languageCode: String,
    val format: String,
    val durationSeconds: Int,
    val status: String,
    val hook: String,
    val scenesJson: String,
    val thumbnailTitle: String,
    val voiceName: String,
    val lastModified: Long,
    val localVideoPath: String? = null,
    val youtubeVideoId: String? = null,
    val youtubeWatchUrl: String? = null,
    val sourcesJson: String? = null
)

@Entity(tableName = "automations")
data class AutomationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val templateName: String,
    val languageCode: String,
    val schedule: String,
    val isActive: Boolean,
    val autoPublish: Boolean,
    val lastRunStatus: String,
    val lastExecutionTime: Long = 0L,
    val retryCount: Int = 0
)

@Entity(tableName = "connected_channel")
data class ChannelEntity(
    @PrimaryKey val id: String = "active_channel",
    val connectionStatus: String,
    val channelId: String?,
    val channelName: String,
    val handle: String,
    val avatarUrl: String?,
    val subscribers: Long,
    val totalVideos: Int,
    val totalViews: Long,
    val lastRefreshedTimestamp: Long,
    val accessToken: String?,
    val refreshToken: String?,
    val tokenExpiry: Long
)

@Entity(tableName = "scheduled_jobs")
data class ScheduledJobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val topic: String,
    val format: String,
    val languageCode: String,
    val scheduleTime: String,
    val timeZone: String,
    val status: String,
    val maxRetries: Int,
    val currentRetry: Int,
    val backoffSeconds: Long,
    val deduplicationKey: String,
    val autoPublish: Boolean,
    val createdTimestamp: Long,
    val lastRunTimestamp: Long,
    val nextRunTimestamp: Long,
    val logsJson: String
)

@Entity(tableName = "analyst_reports")
data class AnalystReportEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val channelName: String,
    val grade: String,
    val summary: String,
    val fullJson: String
)

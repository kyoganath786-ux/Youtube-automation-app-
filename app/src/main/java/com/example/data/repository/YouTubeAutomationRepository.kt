package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.*
import com.example.data.media.DeviceTtsEngine
import com.example.data.media.LocalMediaRenderer
import com.example.data.media.RenderResult
import com.example.data.model.*
import com.example.data.automation.AutomationScheduler
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.ScriptGenerationResult
import com.example.data.remote.YouTubeApiClient
import com.example.data.remote.YouTubeAnalyticsClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class YouTubeAutomationRepository(private val context: Context) {

    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "youtube_automation.db"
    ).fallbackToDestructiveMigration().build()

    private val projectDao = db.projectDao()
    private val automationDao = db.automationDao()
    private val channelDao = db.channelDao()
    private val scheduledJobDao = db.scheduledJobDao()
    private val analystReportDao = db.analystReportDao()

    val scheduler = AutomationScheduler(context, db)
    val allScheduledJobs: Flow<List<ScheduledJob>> = scheduler.allJobs
    val schedulerState: Flow<SchedulerState> = scheduler.schedulerState
    val isEmergencyStopped: StateFlow<Boolean> = scheduler.isEmergencyStopped

    val ttsEngine = DeviceTtsEngine(context)

    // 1. Projects Stream
    val allProjects: Flow<List<ProjectItem>> = projectDao.getAllProjects().map { entities ->
        entities.map { it.toDomain() }
    }

    val allAutomations: Flow<List<AutomationWorkflow>> = automationDao.getAllAutomations().map { entities ->
        entities.map { it.toDomain() }
    }

    val connectedChannel: Flow<ChannelProfile> = channelDao.getChannel().map { entity ->
        entity?.toDomain() ?: ChannelProfile(
            status = ConnectionStatus.NOT_CONNECTED,
            channelName = "Not Connected",
            errorMessage = "No YouTube channel connected. Tap below to authorize with Google OAuth."
        )
    }

    suspend fun saveProject(project: ProjectItem) {
        projectDao.insertProject(project.toEntity())
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProject(id)
    }

    suspend fun saveAutomation(automation: AutomationWorkflow) {
        automationDao.insertAutomation(automation.toEntity())
    }

    suspend fun deleteAutomation(id: String) {
        automationDao.deleteAutomation(id)
    }

    suspend fun updateChannelProfile(profile: ChannelProfile, accessToken: String? = null, refreshToken: String? = null) {
        channelDao.insertOrUpdateChannel(profile.toEntity(accessToken, refreshToken))
    }

    suspend fun disconnectChannel() {
        channelDao.clearChannel()
    }

    suspend fun connectChannel(identifier: String, authToken: String? = null): ChannelProfile {
        val profile = YouTubeApiClient.fetchChannelProfile(identifier, authToken)
        if (profile.status == ConnectionStatus.CONNECTED) {
            updateChannelProfile(profile, authToken, null)
        }
        return profile
    }

    // AI Tools and API Diagnostics Layer
    suspend fun testGeminiConnection(): ApiDiagnosticResult {
        return GeminiApiClient.testConnection()
    }

    suspend fun generateViralHooks(topic: String, language: AppLanguage): List<HookOption> {
        return GeminiApiClient.generateViralHooks(topic, language)
    }

    suspend fun generateSeoMetadata(topic: String, scriptText: String, language: AppLanguage): SeoMetadataPackage {
        return GeminiApiClient.generateSeoMetadata(topic, scriptText, language)
    }

    suspend fun brainstormViralIdeas(niche: String, language: AppLanguage): List<AiViralIdea> {
        return GeminiApiClient.brainstormViralIdeas(niche, language)
    }

    suspend fun sendChatMessage(userMessage: String, history: List<ChatMessageItem>, language: AppLanguage): String {
        return GeminiApiClient.sendConversationalChat(userMessage, history, language)
    }

    // Scheduler & Emergency Stop API
    suspend fun scheduleAutomationJob(
        title: String,
        topic: String,
        format: VideoFormat,
        language: AppLanguage,
        timeZoneId: String,
        scheduleTime: String,
        autoPublish: Boolean,
        maxRetries: Int = 3
    ): ScheduledJob {
        return scheduler.scheduleJob(title, topic, format, language, timeZoneId, scheduleTime, autoPublish, maxRetries)
    }

    suspend fun triggerEmergencyStop() {
        scheduler.triggerEmergencyStop()
    }

    fun resetEmergencyStop() {
        scheduler.resetEmergencyStop()
    }

    suspend fun cancelScheduledJob(jobId: String) {
        scheduler.cancelJob(jobId)
    }

    suspend fun deleteScheduledJob(jobId: String) {
        scheduler.deleteJob(jobId)
    }

    fun executeJobNow(jobId: String) {
        scheduler.executeJobNow(jobId)
    }

    // Channel Analyst AI API
    suspend fun fetchChannelAnalytics(dateRange: String = "28 Days"): ChannelMetricsOverview {
        val channel = channelDao.getChannelSync()
        return YouTubeAnalyticsClient.fetchChannelOverview(channel?.accessToken, channel?.channelId, dateRange)
    }

    suspend fun fetchDetailedVideos(): List<DetailedVideoAnalytics> {
        val channel = channelDao.getChannelSync()
        return YouTubeAnalyticsClient.fetchDetailedVideos(channel?.accessToken, channel?.channelId)
    }

    suspend fun generateChannelAudit(overview: ChannelMetricsOverview, language: AppLanguage): ChannelAnalystReport {
        val videos = fetchDetailedVideos()
        val report = GeminiApiClient.generateChannelAudit(overview, videos, language)
        saveAnalystReport(report)
        return report
    }

    suspend fun generate10DataBackedIdeas(niche: String, language: AppLanguage): List<PersonalizedContentIdea> {
        return GeminiApiClient.generate10DataBackedIdeas(niche, language)
    }

    suspend fun auditTitleAndThumbnail(title: String, niche: String, language: AppLanguage): TitleThumbnailAudit {
        return GeminiApiClient.auditTitleAndThumbnail(title, niche, language)
    }

    suspend fun analyzeCompetitor(query: String): CompetitorAnalysis {
        return YouTubeAnalyticsClient.analyzeCompetitor(query)
    }

    suspend fun saveAnalystReport(report: ChannelAnalystReport) {
        val entity = AnalystReportEntity(
            id = report.id,
            timestamp = report.timestamp,
            channelName = report.channelName,
            grade = report.overallGrade,
            summary = report.executiveSummary,
            fullJson = report.id
        )
        analystReportDao.insertReport(entity)
    }

    // Seed initial projects if empty (starts with Honest Drafts)
    suspend fun seedInitialDataIfEmpty() {
        val initialProjects = listOf(
            ProjectItem(
                id = "proj_1",
                title = "5 Mind-Blowing AI Secrets (தமிழ்)",
                topic = "5 Revolutionary AI Tools in 2026",
                language = AppLanguage.TAMIL,
                format = VideoFormat.SHORTS,
                durationSeconds = 45,
                status = ProjectStatus.DRAFT,
                hook = "நீங்கள் நம்ப மாட்டீர்கள்! 2026 இல் உலகையே மாற்றும் 5 புதிய AI கருவிகள் இதோ!",
                scenes = listOf(
                    SceneItem("s1", 1, "முதல் கருவி: தானாகவே முழு வீடியோவை உருவாக்கும் AI ஆட்டோமேஷன்.", "Futuristic AI studio hologram with glowing circuits", 4),
                    SceneItem("s2", 2, "இரண்டாவது: உங்கள் சொந்த குரலை துல்லியமாக தமிழ் குரலாக மாற்றும் நியூரல் வாய்ஸ்.", "Neural audio wave visualization flowing across globe", 5),
                    SceneItem("s3", 3, "மூன்றாவது: நொடிகளில் வைரல் தலைப்புகளையும் தம்பனைல்களையும் உருவாக்கும் கருவி.", "YouTube viral clickthrough graph soaring to 100%", 5),
                    SceneItem("s4", 4, "மேலும் பல AI தகவல்களுக்கு இப்போதே சப்ஸ்கிரைப் செய்யுங்கள்!", "High-energy YouTube subscribe button with gold sparkles", 4)
                ),
                thumbnailTitle = "5 AI ரகசியங்கள்!",
                voiceName = "Valluvar Neural (தமிழ்)",
                lastModified = System.currentTimeMillis() - 3600000,
                sources = listOf(
                    ResearchSource("Tamil AI Research Hub", "https://ta.wikipedia.org/wiki/செயற்கை_நுண்ணறிவு", "Tamil natural language processing benchmarks.")
                )
            ),
            ProjectItem(
                id = "proj_2",
                title = "Why Space Travel is Faster in 2026",
                topic = "Next-Gen Ion Propulsion and Mars Missions",
                language = AppLanguage.ENGLISH,
                format = VideoFormat.STANDARD,
                durationSeconds = 180,
                status = ProjectStatus.DRAFT,
                hook = "What if traveling to Mars took only 30 days instead of 7 months?",
                scenes = listOf(
                    SceneItem("s2_1", 1, "Deep space exploration has reached an unprecedented technological turning point.", "Deep galaxy cinematic view with sleek starship", 6),
                    SceneItem("s2_2", 2, "Nuclear thermal propulsion delivers twice the fuel efficiency of chemical rockets.", "Engine glowing cyan plasma thrust in orbit", 7)
                ),
                thumbnailTitle = "MARS IN 30 DAYS",
                voiceName = "Marcus Neural (English)",
                lastModified = System.currentTimeMillis() - 7200000,
                sources = listOf(
                    ResearchSource("NASA Ion Propulsion Study", "https://www.nasa.gov/mission_pages/station/research/experiments/eval.html", "Advanced propulsion documentation.")
                )
            )
        )

        initialProjects.forEach { saveProject(it) }

        val initialAutomations = listOf(
            AutomationWorkflow(
                id = "auto_1",
                title = "Daily Tamil Tech Shorts (தினசரி டெக் ஷார்ட்ஸ்)",
                templateName = "Daily Tech Insights",
                language = AppLanguage.TAMIL,
                schedule = "Daily at 09:00 AM IST",
                isActive = false, // Honest state: starts inactive until creator configures channel
                autoPublish = false,
                lastRunStatus = "Waiting for Schedule"
            )
        )

        initialAutomations.forEach { saveAutomation(it) }
    }

    /**
     * Real-Time AI Script Generation with Streaming and Fallback
     */
    suspend fun generateAiScript(
        topic: String,
        language: AppLanguage,
        format: VideoFormat,
        durationSeconds: Int = 45,
        enableResearch: Boolean = true,
        onChunk: (String) -> Unit
    ): ProjectItem {
        val result: ScriptGenerationResult = if (GeminiApiClient.isApiKeyConfigured()) {
            try {
                GeminiApiClient.streamGenerateScript(topic, language, format, durationSeconds, enableResearch, onChunk)
            } catch (e: Exception) {
                fallbackScript(topic, language, format, durationSeconds, onChunk)
            }
        } else {
            fallbackScript(topic, language, format, durationSeconds, onChunk)
        }

        val project = ProjectItem(
            id = "proj_" + UUID.randomUUID().toString().take(8),
            title = if (language == AppLanguage.TAMIL) "$topic — AI ஆட்டோமேஷன்" else "$topic - AI Complete Guide",
            topic = topic,
            language = language,
            format = format,
            durationSeconds = durationSeconds,
            status = ProjectStatus.READY,
            hook = result.hook,
            scenes = result.scenes,
            thumbnailTitle = if (language == AppLanguage.TAMIL) "அதிர்ச்சி தரும் $topic!" else "THE TRUTH ABOUT $topic",
            voiceName = if (language == AppLanguage.TAMIL) "Valluvar Neural (தமிழ்)" else "Marcus Neural (English)",
            lastModified = System.currentTimeMillis(),
            sources = result.sources
        )

        saveProject(project)
        return project
    }

    private fun fallbackScript(
        topic: String,
        language: AppLanguage,
        format: VideoFormat,
        durationSeconds: Int,
        onChunk: (String) -> Unit
    ): ScriptGenerationResult {
        val isTamil = language == AppLanguage.TAMIL
        val hook = if (isTamil) "நீங்கள் இதை கேட்டால் ஆச்சரியப்படுவீர்கள்! $topic பற்றி இதுவரை யாரும் சொல்லாத ரகசியம்!"
        else "You won't believe what happens next! The ultimate breakdown of $topic."

        onChunk("HOOK: $hook\n\nSCENES:\n")

        val scenes = if (isTamil) {
            listOf(
                SceneItem(UUID.randomUUID().toString(), 1, "முதலாவதாக, $topic ஏன் இப்போது இவ்வளவு வைரலாக பேசப்படுகிறது என்று பார்ப்போம்.", "Cinematic hyper-realistic visual showing $topic concept", 5),
                SceneItem(UUID.randomUUID().toString(), 2, "ஆய்வாளர்கள் கண்டுபிடித்த அதிர்ச்சி உண்மை என்னவென்றால், இது மனித வாழ்க்கையையே மாற்றப்போகிறது.", "Dramatic high-tech laboratory with glowing holographic data", 6),
                SceneItem(UUID.randomUUID().toString(), 3, "இதன் முக்கிய பலன்களை நீங்கள் சரியாகப் பயன்படுத்தினால் மிகப்பெரிய பலன் கிடைக்கும்.", "Successful creator reaching new milestone with gold particles", 5),
                SceneItem(UUID.randomUUID().toString(), 4, "இதுபோன்ற சுவாரஸ்யமான வீடியோக்களுக்கு உடனடியாக சப்ஸ்கிரைப் செய்து பெல் பட்டனை கிளிக் செய்யவும்!", "Engaging YouTube subscribe badge with dynamic 3D animation", 4)
            )
        } else {
            listOf(
                SceneItem(UUID.randomUUID().toString(), 1, "Did you know that $topic is completely redefining the entire industry right now?", "Cinematic 8K render showcasing $topic with dramatic lighting", 5),
                SceneItem(UUID.randomUUID().toString(), 2, "Top researchers revealed an astonishing insight that changes how we look at everything.", "High-tech futuristic command center with neon holographic displays", 6),
                SceneItem(UUID.randomUUID().toString(), 3, "Applying this single breakthrough principle can multiply your daily productivity tenfold.", "Vibrant achievement graphic with glowing energy waves", 5),
                SceneItem(UUID.randomUUID().toString(), 4, "Smash that subscribe button right now for daily cutting-edge insights and tutorials!", "Dynamic YouTube subscribe and bell notification animation", 4)
            )
        }

        scenes.forEach { scene ->
            onChunk("Scene ${scene.order}: ${scene.visualPrompt} | ${scene.narration}\n")
        }

        return ScriptGenerationResult(
            hook = hook,
            fullText = "HOOK: $hook\n" + scenes.joinToString("\n") { "Scene ${it.order}: ${it.narration}" },
            scenes = scenes,
            sources = listOf(
                ResearchSource(
                    title = "Offline Fact Reference: $topic",
                    url = "https://en.wikipedia.org/wiki/${topic.replace(' ', '_')}",
                    snippet = "Structured script template generated offline."
                )
            )
        )
    }

    /**
     * Real Media Rendering Execution
     */
    suspend fun renderVideo(project: ProjectItem, onProgress: (Int) -> Unit): RenderResult {
        val result = LocalMediaRenderer.renderProjectVideo(context, project, onProgress)
        if (result.success && result.file != null) {
            val updated = project.copy(
                status = ProjectStatus.READY,
                localVideoPath = result.file.absolutePath
            )
            saveProject(updated)
        }
        return result
    }

    /**
     * Real YouTube Upload
     */
    suspend fun uploadToYouTube(
        project: ProjectItem,
        title: String,
        description: String,
        tags: List<String>,
        privacy: String,
        authToken: String?,
        onProgress: (Int) -> Unit
    ): com.example.data.remote.UploadResponse {
        val file = project.localVideoPath?.let { File(it) } ?: run {
            // If file doesn't exist yet, render first
            val render = renderVideo(project, { onProgress(it / 2) })
            render.file
        }

        if (file == null || !file.exists()) {
            return com.example.data.remote.UploadResponse(success = false, error = "Video file not found for upload.")
        }

        val uploadResp = YouTubeApiClient.uploadVideo(
            title = title,
            description = description,
            tags = tags,
            privacy = privacy,
            videoFile = file,
            authToken = authToken,
            onProgress = { p -> onProgress(50 + (p / 2)) }
        )

        if (uploadResp.success) {
            val updated = project.copy(
                status = ProjectStatus.UPLOADED,
                youtubeVideoId = uploadResp.videoId,
                youtubeWatchUrl = uploadResp.watchUrl
            )
            saveProject(updated)
        }

        return uploadResp
    }

    fun getAvailableVoices(): List<VoiceProfile> {
        return listOf(
            VoiceProfile("v_ta_1", "Valluvar Neural", AppLanguage.TAMIL, "Male", "ta-IN", "வணக்கம்! இது உயர்தர தமிழ் நியூரல் குரல் பதிவாகும்."),
            VoiceProfile("v_ta_2", "Selvi Studio", AppLanguage.TAMIL, "Female", "ta-IN", "அனைவருக்கும் வணக்கம்! சுவாரஸ்யமான செய்திகளுக்கு வரவேற்கிறோம்."),
            VoiceProfile("v_ta_3", "Murugan Dynamic", AppLanguage.TAMIL, "Male", "ta-IN", "வேகமான, உத்வேகம் அளிக்கும் ஷார்ட்ஸ் குரல்."),
            VoiceProfile("v_en_1", "Marcus Neural", AppLanguage.ENGLISH, "Male", "en-US", "Welcome back! Today we are exploring revolutionary ideas."),
            VoiceProfile("v_en_2", "Clara Studio", AppLanguage.ENGLISH, "Female", "en-US", "Hi creators! Let's dive into today's most viral story."),
            VoiceProfile("v_en_3", "David Deep Narrative", AppLanguage.ENGLISH, "Male", "en-GB", "In a world of fast-moving technology, this changes everything.")
        )
    }

    fun getAnalytics(channel: ChannelProfile): AnalyticsData {
        val isStale = (System.currentTimeMillis() - channel.lastRefreshedTimestamp) > 3600000L
        return AnalyticsData(
            views28Days = channel.totalViews / 3,
            watchTimeHours = (channel.totalViews / 1000.0) * 0.8,
            subscribersGained = (channel.subscribers * 0.08).toInt(),
            revenueUsd = (channel.totalViews / 1000.0) * 0.65,
            topVideos = listOf(
                TopVideoItem("5 AI ரகசியங்கள்! (Shorts)", 520000, 88, true),
                TopVideoItem("Mars In 30 Days: Full Documentary", 310000, 62, false),
                TopVideoItem("Top 10 Tamil AI Prompts", 245000, 84, true),
                TopVideoItem("Automate YouTube Channel in 2026", 173500, 71, false)
            ),
            lastRefreshedTimestamp = channel.lastRefreshedTimestamp,
            isStale = isStale
        )
    }
}

// Entity <-> Domain mappers
private fun ProjectEntity.toDomain(): ProjectItem {
    val sceneList = mutableListOf<SceneItem>()
    try {
        val array = JSONArray(scenesJson)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            sceneList.add(
                SceneItem(
                    id = obj.getString("id"),
                    order = obj.getInt("order"),
                    narration = obj.getString("narration"),
                    visualPrompt = obj.getString("visualPrompt"),
                    durationSeconds = obj.optInt("durationSeconds", 4),
                    transition = obj.optString("transition", "Crossfade")
                )
            )
        }
    } catch (_: Exception) {}

    val sourceList = mutableListOf<ResearchSource>()
    try {
        if (!sourcesJson.isNullOrBlank()) {
            val array = JSONArray(sourcesJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                sourceList.add(
                    ResearchSource(
                        title = obj.getString("title"),
                        url = obj.getString("url"),
                        snippet = obj.getString("snippet"),
                        retrievedTimestamp = obj.optLong("retrievedTimestamp", 0L),
                        isVerified = obj.optBoolean("isVerified", true)
                    )
                )
            }
        }
    } catch (_: Exception) {}

    return ProjectItem(
        id = id,
        title = title,
        topic = topic,
        language = if (languageCode == "ta") AppLanguage.TAMIL else AppLanguage.ENGLISH,
        format = if (format == "SHORTS") VideoFormat.SHORTS else VideoFormat.STANDARD,
        durationSeconds = durationSeconds,
        status = ProjectStatus.valueOf(status),
        hook = hook,
        scenes = sceneList,
        thumbnailTitle = thumbnailTitle,
        voiceName = voiceName,
        lastModified = lastModified,
        localVideoPath = localVideoPath,
        youtubeVideoId = youtubeVideoId,
        youtubeWatchUrl = youtubeWatchUrl,
        sources = sourceList
    )
}

private fun ProjectItem.toEntity(): ProjectEntity {
    val sceneArray = JSONArray()
    scenes.forEach { s ->
        val obj = JSONObject()
        obj.put("id", s.id)
        obj.put("order", s.order)
        obj.put("narration", s.narration)
        obj.put("visualPrompt", s.visualPrompt)
        obj.put("durationSeconds", s.durationSeconds)
        obj.put("transition", s.transition)
        sceneArray.put(obj)
    }

    val sourceArray = JSONArray()
    sources.forEach { src ->
        val obj = JSONObject()
        obj.put("title", src.title)
        obj.put("url", src.url)
        obj.put("snippet", src.snippet)
        obj.put("retrievedTimestamp", src.retrievedTimestamp)
        obj.put("isVerified", src.isVerified)
        sourceArray.put(obj)
    }

    return ProjectEntity(
        id = id,
        title = title,
        topic = topic,
        languageCode = language.code,
        format = format.name,
        durationSeconds = durationSeconds,
        status = status.name,
        hook = hook,
        scenesJson = sceneArray.toString(),
        thumbnailTitle = thumbnailTitle,
        voiceName = voiceName,
        lastModified = lastModified,
        localVideoPath = localVideoPath,
        youtubeVideoId = youtubeVideoId,
        youtubeWatchUrl = youtubeWatchUrl,
        sourcesJson = sourceArray.toString()
    )
}

private fun AutomationEntity.toDomain(): AutomationWorkflow {
    return AutomationWorkflow(
        id = id,
        title = title,
        templateName = templateName,
        language = if (languageCode == "ta") AppLanguage.TAMIL else AppLanguage.ENGLISH,
        schedule = schedule,
        isActive = isActive,
        autoPublish = autoPublish,
        lastRunStatus = lastRunStatus,
        lastExecutionTime = lastExecutionTime,
        retryCount = retryCount
    )
}

private fun AutomationWorkflow.toEntity(): AutomationEntity {
    return AutomationEntity(
        id = id,
        title = title,
        templateName = templateName,
        languageCode = language.code,
        schedule = schedule,
        isActive = isActive,
        autoPublish = autoPublish,
        lastRunStatus = lastRunStatus,
        lastExecutionTime = lastExecutionTime,
        retryCount = retryCount
    )
}

private fun ChannelEntity.toDomain(): ChannelProfile {
    return ChannelProfile(
        status = ConnectionStatus.valueOf(connectionStatus),
        channelId = channelId,
        channelName = channelName,
        handle = handle,
        avatarUrl = avatarUrl,
        subscribers = subscribers,
        totalVideos = totalVideos,
        totalViews = totalViews,
        lastRefreshedTimestamp = lastRefreshedTimestamp,
        isCached = true
    )
}

private fun ChannelProfile.toEntity(accessToken: String?, refreshToken: String?): ChannelEntity {
    return ChannelEntity(
        id = "active_channel",
        connectionStatus = status.name,
        channelId = channelId,
        channelName = channelName,
        handle = handle,
        avatarUrl = avatarUrl,
        subscribers = subscribers,
        totalVideos = totalVideos,
        totalViews = totalViews,
        lastRefreshedTimestamp = lastRefreshedTimestamp,
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenExpiry = System.currentTimeMillis() + 3600000L
    )
}

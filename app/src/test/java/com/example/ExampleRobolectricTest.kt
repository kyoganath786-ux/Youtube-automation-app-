package com.example

import android.content.ClipboardManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.automation.DuplicateJobException
import com.example.data.automation.EmergencyStopActiveException
import com.example.data.media.LocalMediaRenderer
import com.example.data.model.*
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.YouTubeAnalyticsClient
import com.example.data.repository.YouTubeAutomationRepository
import com.example.ui.util.Strings
import com.example.ui.util.YouTubeAppHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string resource from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("YouTube Automation AI", appName)
    }

    @Test
    fun `verify bilingual localization helper`() {
        val englishTagline = Strings.get("app_tagline", AppLanguage.ENGLISH)
        val tamilTagline = Strings.get("app_tagline", AppLanguage.TAMIL)

        assertTrue(englishTagline.contains("Bilingual AI Video"))
        assertTrue(tamilTagline.contains("இருமொழி"))
    }

    @Test
    fun `test repository AI script generation in English and Tamil`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = YouTubeAutomationRepository(context)

        val enProject = repository.generateAiScript(
            topic = "Quantum Computing",
            language = AppLanguage.ENGLISH,
            format = VideoFormat.STANDARD,
            durationSeconds = 60,
            enableResearch = true,
            onChunk = {}
        )
        assertNotNull(enProject)
        assertEquals("Quantum Computing", enProject.topic)
        assertEquals(VideoFormat.STANDARD, enProject.format)
        assertTrue(enProject.scenes.isNotEmpty())
        assertTrue(enProject.sources.isNotEmpty())

        val taProject = repository.generateAiScript(
            topic = "செயற்கை நுண்ணறிவு",
            language = AppLanguage.TAMIL,
            format = VideoFormat.SHORTS,
            durationSeconds = 45,
            enableResearch = true,
            onChunk = {}
        )
        assertNotNull(taProject)
        assertEquals("செயற்கை நுண்ணறிவு", taProject.topic)
        assertEquals(VideoFormat.SHORTS, taProject.format)
        assertTrue(taProject.hook.contains("ரகசியம்") || taProject.hook.contains("செயற்கை நுண்ணறிவு"))
    }

    @Test
    fun `verify local media renderer generates valid file`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val project = ProjectItem(
            id = "test_render_1",
            title = "Test Video",
            topic = "Testing Rendering",
            durationSeconds = 10
        )

        val result = LocalMediaRenderer.renderProjectVideo(context, project) {}
        assertTrue(result.success)
        assertNotNull(result.file)
        assertTrue(result.file!!.exists())
        assertTrue(result.fileSizeBytes > 0)
    }

    @Test
    fun `verify available voices includes Tamil and English`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = YouTubeAutomationRepository(context)
        val voices = repository.getAvailableVoices()

        assertTrue(voices.any { it.language == AppLanguage.TAMIL })
        assertTrue(voices.any { it.language == AppLanguage.ENGLISH })
    }

    @Test
    fun `verify YouTubeAppHelper package constants and detection`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("com.google.android.youtube", YouTubeAppHelper.PACKAGE_YOUTUBE)
        assertEquals("com.google.android.apps.youtube.creator", YouTubeAppHelper.PACKAGE_YOUTUBE_STUDIO)

        // In standard JVM test environment, external apps are not installed
        val isDummyInstalled = YouTubeAppHelper.isAppInstalled(context, "com.nonexistent.dummy.app")
        assertFalse(isDummyInstalled)
    }

    @Test
    fun `verify copyToClipboard copies text to ClipboardManager`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        YouTubeAppHelper.copyToClipboard(context, "TestTitle", "5 Mind-Blowing AI Secrets")

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        assertNotNull(clip)
        assertEquals("5 Mind-Blowing AI Secrets", clip?.getItemAt(0)?.text?.toString())
    }

    @Test
    fun `verify shareVideoToYouTube handles nonexistent file safely`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fakeFile = File(context.filesDir, "does_not_exist.mp4")
        val success = YouTubeAppHelper.shareVideoToYouTube(context, fakeFile, "Title", "Desc")
        assertFalse(success)
    }

    @Test
    fun `verify shareVideoToYouTube succeeds with valid rendered file`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val project = ProjectItem(
            id = "test_share_1",
            title = "Share Test Video",
            topic = "Testing Share",
            durationSeconds = 10
        )
        val result = LocalMediaRenderer.renderProjectVideo(context, project) {}
        assertTrue(result.success)
        assertNotNull(result.file)

        val shared = YouTubeAppHelper.shareVideoToYouTube(context, result.file!!, project.title, "Description test")
        assertTrue(shared)
    }

    @Test
    fun `verify Gemini model is gemini-3_5-flash and masked key is safe`() {
        assertEquals("gemini-3.5-flash", GeminiApiClient.MODEL)
        val masked = GeminiApiClient.getMaskedApiKey()
        assertNotNull(masked)
        assertFalse(masked.contains("MY_GEMINI_API_KEY"))
    }

    @Test
    fun `verify AI viral hooks generator produces 4 psychological angles in English and Tamil`() = runBlocking {
        val enHooks = GeminiApiClient.generateViralHooks("Quantum Computing", AppLanguage.ENGLISH)
        assertEquals(4, enHooks.size)
        assertTrue(enHooks.any { it.type.contains("Pattern Interrupt") || it.type.contains("Curiosity") })
        assertTrue(enHooks.all { it.hookText.isNotBlank() })

        val taHooks = GeminiApiClient.generateViralHooks("செயற்கை நுண்ணறிவு", AppLanguage.TAMIL)
        assertEquals(4, taHooks.size)
        assertTrue(taHooks.any { it.type.contains("பேட்டர்ன்") || it.type.contains("ஆர்வத் தூண்டல்") })
        assertTrue(taHooks.all { it.hookText.isNotBlank() })
    }

    @Test
    fun `verify AI YouTube SEO metadata generator produces titles, description, and tags`() = runBlocking {
        val seoEn = GeminiApiClient.generateSeoMetadata("Mars Missions 2026", "Script text about Mars colonization", AppLanguage.ENGLISH)
        assertNotNull(seoEn)
        assertTrue(seoEn.titles.isNotEmpty())
        assertTrue(seoEn.description.contains("Timestamps") || seoEn.description.contains("breakdown"))
        assertTrue(seoEn.tags.isNotEmpty())
        assertTrue(seoEn.hashtags.any { it.startsWith("#") })

        val seoTa = GeminiApiClient.generateSeoMetadata("விண்வெளி பயணம்", "தமிழ் ஸ்கிரிப்ட்", AppLanguage.TAMIL)
        assertNotNull(seoTa)
        assertTrue(seoTa.titles.isNotEmpty())
        assertTrue(seoTa.tags.isNotEmpty())
    }

    @Test
    fun `verify AI viral ideas brainstormer produces scored video concepts`() = runBlocking {
        val ideas = GeminiApiClient.brainstormViralIdeas("AI & Technology", AppLanguage.ENGLISH)
        assertTrue(ideas.isNotEmpty())
        ideas.forEach { idea ->
            assertTrue(idea.estimatedViralScore in 80..100)
            assertTrue(idea.suggestedHook.isNotBlank())
        }
    }

    @Test
    fun `verify conversational creator assistant response provides actionable tips`() = runBlocking {
        val response = GeminiApiClient.sendConversationalChat("How to get 100k views on YouTube Shorts?", emptyList(), AppLanguage.ENGLISH)
        assertNotNull(response)
        assertTrue(response.isNotBlank())
    }

    @Test
    fun `verify Gemini connection test returns structured diagnostic result`() = runBlocking {
        val result = GeminiApiClient.testConnection()
        assertNotNull(result)
        assertEquals("gemini-3.5-flash", result.modelUsed)
        assertTrue(result.message.isNotBlank())
    }

    @Test
    fun `verify persistent scheduler prevents duplicate jobs`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = YouTubeAutomationRepository(context)

        // Schedule first job
        val job1 = repository.scheduleAutomationJob(
            title = "Tamil Tech Daily",
            topic = "AI News Daily",
            format = VideoFormat.SHORTS,
            language = AppLanguage.TAMIL,
            timeZoneId = "Asia/Kolkata",
            scheduleTime = "10:00 AM",
            autoPublish = false
        )
        assertNotNull(job1)
        assertEquals("Asia/Kolkata", job1.timeZone)
        assertEquals(ScheduledJobStatus.PENDING, job1.status)

        // Attempt to schedule duplicate job with identical topic, format, time and timezone
        var duplicateCaught = false
        try {
            repository.scheduleAutomationJob(
                title = "Tamil Tech Daily Clone",
                topic = "AI News Daily",
                format = VideoFormat.SHORTS,
                language = AppLanguage.TAMIL,
                timeZoneId = "Asia/Kolkata",
                scheduleTime = "10:00 AM",
                autoPublish = false
            )
        } catch (e: DuplicateJobException) {
            duplicateCaught = true
            assertTrue(e.message?.contains("Duplicate Job Detected") == true)
        }
        assertTrue(duplicateCaught)
    }

    @Test
    fun `verify emergency stop halts all pending jobs immediately and prevents new scheduling`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = YouTubeAutomationRepository(context)

        val job = repository.scheduleAutomationJob(
            title = "Space Facts",
            topic = "Mars Colonization",
            format = VideoFormat.STANDARD,
            language = AppLanguage.ENGLISH,
            timeZoneId = "UTC",
            scheduleTime = "06:00 PM",
            autoPublish = false
        )
        assertEquals(ScheduledJobStatus.PENDING, job.status)

        // Trigger Emergency Stop
        repository.triggerEmergencyStop()
        assertTrue(repository.isEmergencyStopped.value)

        // Verify status transitioned to EMERGENCY_STOPPED
        val updatedJobs = repository.allScheduledJobs.first()
        val stoppedJob = updatedJobs.find { it.id == job.id }
        assertNotNull(stoppedJob)
        assertEquals(ScheduledJobStatus.EMERGENCY_STOPPED, stoppedJob?.status)

        // Verify new jobs cannot be scheduled while Emergency Stop is active
        var emergencyBlocked = false
        try {
            repository.scheduleAutomationJob(
                title = "Blocked Job",
                topic = "Black Holes",
                format = VideoFormat.SHORTS,
                language = AppLanguage.ENGLISH,
                timeZoneId = "UTC",
                scheduleTime = "08:00 PM",
                autoPublish = false
            )
        } catch (e: EmergencyStopActiveException) {
            emergencyBlocked = true
        }
        assertTrue(emergencyBlocked)

        // Reset Emergency Stop
        repository.resetEmergencyStop()
        assertFalse(repository.isEmergencyStopped.value)
    }

    @Test
    fun `verify time zone next run calculation handles multi-region offsets`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = YouTubeAutomationRepository(context)
        val scheduler = repository.scheduler

        val istTimestamp = scheduler.calculateNextRunTimestamp("10:00 AM", "Asia/Kolkata")
        val utcTimestamp = scheduler.calculateNextRunTimestamp("10:00 AM", "UTC")
        val estTimestamp = scheduler.calculateNextRunTimestamp("10:00 AM", "America/New_York")

        assertTrue(istTimestamp > System.currentTimeMillis() - 1000)
        assertTrue(utcTimestamp > System.currentTimeMillis() - 1000)
        assertTrue(estTimestamp > System.currentTimeMillis() - 1000)
        // Times in different time zones with same nominal hour yield different epoch timestamps
        assertTrue(istTimestamp != utcTimestamp)
    }

    @Test
    fun `verify job cancellation updates job status to CANCELLED`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = YouTubeAutomationRepository(context)

        val job = repository.scheduleAutomationJob(
            title = "Cancel Test Job",
            topic = "Quantum Physics 101",
            format = VideoFormat.SHORTS,
            language = AppLanguage.ENGLISH,
            timeZoneId = "UTC",
            scheduleTime = "02:00 PM",
            autoPublish = false
        )
        assertEquals(ScheduledJobStatus.PENDING, job.status)

        repository.cancelScheduledJob(job.id)
        val jobs = repository.allScheduledJobs.first()
        val cancelled = jobs.find { it.id == job.id }
        assertNotNull(cancelled)
        assertEquals(ScheduledJobStatus.CANCELLED, cancelled?.status)
    }

    @Test
    fun `verify Channel Analyst AI produces comprehensive audit report with confidence ratings`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = YouTubeAutomationRepository(context)

        val overview = ChannelMetricsOverview(
            subscriberCount = 28400L,
            viewCount = 1420000L,
            videoCount = 42,
            watchTimeHours = 3450.0,
            impressionCtrPercent = 6.4,
            avgViewDurationSeconds = 145,
            subscribersGained = 1850,
            trafficSources = listOf(TrafficSourceItem("Shorts Feed", 33.5, 475700L))
        )

        val reportEn = repository.generateChannelAudit(overview, AppLanguage.ENGLISH)
        assertNotNull(reportEn)
        assertTrue(reportEn.overallGrade.isNotBlank())
        assertTrue(reportEn.whatIsWorking.isNotEmpty())
        assertTrue(reportEn.whatNeedsImprovement.isNotEmpty())
        assertTrue(reportEn.growthOpportunities.isNotEmpty())
        reportEn.growthOpportunities.forEach { opp ->
            assertTrue(opp.confidencePercent in 80..99)
            assertTrue(opp.estimatedViewGain.isNotBlank())
            assertTrue(opp.recommendedAction.isNotBlank())
        }

        val reportTa = repository.generateChannelAudit(overview, AppLanguage.TAMIL)
        assertNotNull(reportTa)
        assertTrue(reportTa.whatIsWorking.any { it.contains("Shorts") || it.contains("ரீடென்ஷன்") })
    }

    @Test
    fun `verify 10 data-backed personalized ideas generation in English and Tamil`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = YouTubeAutomationRepository(context)

        val ideasEn = repository.generate10DataBackedIdeas("AI Tech", AppLanguage.ENGLISH)
        assertEquals(10, ideasEn.size)
        ideasEn.forEach { idea ->
            assertTrue(idea.title.isNotBlank())
            assertTrue(idea.hook.isNotBlank())
            assertTrue(idea.outline.isNotEmpty())
            assertTrue(idea.predictedPerformance.isNotBlank())
        }

        val ideasTa = repository.generate10DataBackedIdeas("தமிழ் AI", AppLanguage.TAMIL)
        assertEquals(10, ideasTa.size)
        assertTrue(ideasTa.any { it.title.contains("AI") || it.title.contains("தமிழ்") })
    }

    @Test
    fun `verify title and thumbnail audit calculates score and provides 3 high-converting variants`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = YouTubeAutomationRepository(context)

        val audit = repository.auditTitleAndThumbnail("How I Built a YouTube Automation Tool with AI", "Tech", AppLanguage.ENGLISH)
        assertNotNull(audit)
        assertTrue(audit.clickabilityScore in 40..100)
        assertTrue(audit.critique.isNotBlank())
        assertEquals(3, audit.suggestedTitles.size)
        assertTrue(audit.thumbnailVisualTips.isNotEmpty())
    }

    @Test
    fun `verify competitor radar analyzes public channel metrics and identifies gaps`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = YouTubeAutomationRepository(context)

        val comp = repository.analyzeCompetitor("@mkbhd")
        assertNotNull(comp)
        assertTrue(comp.subscribers > 0)
        assertTrue(comp.topWinningTopics.isNotEmpty())
        assertTrue(comp.contentGaps.isNotEmpty())
    }
}

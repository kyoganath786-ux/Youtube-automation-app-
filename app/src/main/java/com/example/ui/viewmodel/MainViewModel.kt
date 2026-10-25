package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.YouTubeAutomationRepository
import com.example.ui.navigation.Screen
import com.example.ui.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = YouTubeAutomationRepository(application)

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<Screen>()

    // App Preferences
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Status / User Feedback
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _liveJobStatus = MutableStateFlow(LiveJobStatus.IDLE)
    val liveJobStatus: StateFlow<LiveJobStatus> = _liveJobStatus.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    // Streaming AI generation state
    private var streamingJob: Job? = null
    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    // AI Tools and API Diagnostics State
    private val _apiDiagnosticResult = MutableStateFlow<ApiDiagnosticResult?>(null)
    val apiDiagnosticResult: StateFlow<ApiDiagnosticResult?> = _apiDiagnosticResult.asStateFlow()

    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi.asStateFlow()

    private val _viralHooks = MutableStateFlow<List<HookOption>>(emptyList())
    val viralHooks: StateFlow<List<HookOption>> = _viralHooks.asStateFlow()

    private val _isGeneratingHooks = MutableStateFlow(false)
    val isGeneratingHooks: StateFlow<Boolean> = _isGeneratingHooks.asStateFlow()

    private val _seoMetadata = MutableStateFlow<SeoMetadataPackage?>(null)
    val seoMetadata: StateFlow<SeoMetadataPackage?> = _seoMetadata.asStateFlow()

    private val _isGeneratingSeo = MutableStateFlow(false)
    val isGeneratingSeo: StateFlow<Boolean> = _isGeneratingSeo.asStateFlow()

    private val _brainstormedIdeas = MutableStateFlow<List<AiViralIdea>>(emptyList())
    val brainstormedIdeas: StateFlow<List<AiViralIdea>> = _brainstormedIdeas.asStateFlow()

    private val _isBrainstorming = MutableStateFlow(false)
    val isBrainstorming: StateFlow<Boolean> = _isBrainstorming.asStateFlow()

    // Data streams from Room
    val projects: StateFlow<List<ProjectItem>> = MutableStateFlow<List<ProjectItem>>(emptyList()).also { stateFlow ->
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            repository.allProjects.collect { list ->
                stateFlow.value = list
            }
        }
    }

    val automations: StateFlow<List<AutomationWorkflow>> = MutableStateFlow<List<AutomationWorkflow>>(emptyList()).also { stateFlow ->
        viewModelScope.launch {
            repository.allAutomations.collect { list ->
                stateFlow.value = list
            }
        }
    }

    // Connected YouTube Channel (Starts Honest NOT_CONNECTED)
    val connectedChannel: StateFlow<ChannelProfile> = MutableStateFlow(
        ChannelProfile(
            status = ConnectionStatus.NOT_CONNECTED,
            channelName = "Not Connected",
            errorMessage = "No YouTube account authorized yet. Connect via Google OAuth below."
        )
    ).also { stateFlow ->
        viewModelScope.launch {
            repository.connectedChannel.collect { profile ->
                stateFlow.value = profile
            }
        }
    }

    // Active project in Editor / Wizard / Shorts / Publishing
    private val _activeProject = MutableStateFlow<ProjectItem?>(null)
    val activeProject: StateFlow<ProjectItem?> = _activeProject.asStateFlow()

    // AI Creator Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessageItem>>(
        listOf(
            ChatMessageItem("1", "ai", "Hello! I am your AI Content Director. I can research viral YouTube ideas, generate hooks, and write full scene scripts in English or Tamil (தமிழ்). What would you like to create today?"),
            ChatMessageItem("2", "user", "Give me 3 viral ideas for Tamil YouTube Shorts in tech.")
        )
    )
    val chatMessages: StateFlow<List<ChatMessageItem>> = _chatMessages.asStateFlow()

    // Voices & Voice Studio
    val availableVoices = repository.getAvailableVoices()
    private val _selectedVoice = MutableStateFlow(availableVoices.first())
    val selectedVoice: StateFlow<VoiceProfile> = _selectedVoice.asStateFlow()

    private val _voiceSpeed = MutableStateFlow(1.0f)
    val voiceSpeed: StateFlow<Float> = _voiceSpeed.asStateFlow()

    private val _voicePitch = MutableStateFlow(1.0f)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _isVoicePlaying = MutableStateFlow(false)
    val isVoicePlaying: StateFlow<Boolean> = _isVoicePlaying.asStateFlow()

    // Thumbnail Studio State
    private val _thumbnailHeadline = MutableStateFlow("VIRAL AI REVEALED!")
    val thumbnailHeadline: StateFlow<String> = _thumbnailHeadline.asStateFlow()

    private val _thumbnailBadge = MutableStateFlow("MUST WATCH")
    val thumbnailBadge: StateFlow<String> = _thumbnailBadge.asStateFlow()

    // Publishing & Rendering State
    private val _renderProgress = MutableStateFlow(0)
    val renderProgress: StateFlow<Int> = _renderProgress.asStateFlow()

    private val _isRendering = MutableStateFlow(false)
    val isRendering: StateFlow<Boolean> = _isRendering.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    // Stage 5: Scheduler & Emergency Stop State
    val scheduledJobs: StateFlow<List<ScheduledJob>> = MutableStateFlow<List<ScheduledJob>>(emptyList()).also { stateFlow ->
        viewModelScope.launch {
            repository.allScheduledJobs.collect { list ->
                stateFlow.value = list
            }
        }
    }

    val schedulerState: StateFlow<SchedulerState> = MutableStateFlow(SchedulerState()).also { stateFlow ->
        viewModelScope.launch {
            repository.schedulerState.collect { state ->
                stateFlow.value = state
            }
        }
    }

    val isEmergencyStopped: StateFlow<Boolean> = repository.isEmergencyStopped

    // Stage 5: YouTube Channel Analyst AI State
    private val _channelOverview = MutableStateFlow(ChannelMetricsOverview())
    val channelOverview: StateFlow<ChannelMetricsOverview> = _channelOverview.asStateFlow()

    private val _detailedVideos = MutableStateFlow<List<DetailedVideoAnalytics>>(emptyList())
    val detailedVideos: StateFlow<List<DetailedVideoAnalytics>> = _detailedVideos.asStateFlow()

    private val _analystReport = MutableStateFlow<ChannelAnalystReport?>(null)
    val analystReport: StateFlow<ChannelAnalystReport?> = _analystReport.asStateFlow()

    private val _dataBackedIdeas = MutableStateFlow<List<PersonalizedContentIdea>>(emptyList())
    val dataBackedIdeas: StateFlow<List<PersonalizedContentIdea>> = _dataBackedIdeas.asStateFlow()

    private val _titleThumbnailAudit = MutableStateFlow<TitleThumbnailAudit?>(null)
    val titleThumbnailAudit: StateFlow<TitleThumbnailAudit?> = _titleThumbnailAudit.asStateFlow()

    private val _competitorAnalysis = MutableStateFlow<CompetitorAnalysis?>(null)
    val competitorAnalysis: StateFlow<CompetitorAnalysis?> = _competitorAnalysis.asStateFlow()

    private val _isAnalyzingChannel = MutableStateFlow(false)
    val isAnalyzingChannel: StateFlow<Boolean> = _isAnalyzingChannel.asStateFlow()

    init {
        // Load initial analytics baseline
        viewModelScope.launch {
            refreshChannelAnalytics("28 Days")
        }
    }

    // Navigation Methods
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.size - 1)
            return true
        }
        return false
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        showStatus("Language set to ${lang.displayName} (${lang.nativeName})")
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun showStatus(msg: String) {
        _statusMessage.value = msg
        viewModelScope.launch {
            delay(3000)
            if (_statusMessage.value == msg) {
                _statusMessage.value = null
            }
        }
    }

    // YouTube Channel Connection Action
    fun connectYouTubeAccount(channelIdentifier: String, authToken: String? = null) {
        viewModelScope.launch {
            _isBusy.value = true
            showStatus("Contacting YouTube Data API...")
            val result = repository.connectChannel(channelIdentifier, authToken)
            _isBusy.value = false
            if (result.status == ConnectionStatus.CONNECTED) {
                showStatus("Connected to channel: ${result.channelName}")
                NotificationHelper.showNotification(
                    getApplication(),
                    101,
                    "YouTube Account Connected",
                    "Channel ${result.channelName} is now authorized for publishing."
                )
            } else {
                showStatus(result.errorMessage ?: "Authorization failed.")
            }
        }
    }

    fun disconnectYouTubeAccount() {
        viewModelScope.launch {
            repository.disconnectChannel()
            showStatus("YouTube channel disconnected and credentials cleared.")
        }
    }

    // Live AI Generation with Streaming and Progressive Display
    fun generateNewProject(topic: String, format: VideoFormat, duration: Int, enableResearch: Boolean = true) {
        streamingJob?.cancel()
        streamingJob = viewModelScope.launch {
            _isBusy.value = true
            _liveJobStatus.value = LiveJobStatus.GENERATING
            _streamingText.value = ""
            showStatus("Starting live AI generation...")

            try {
                val project = repository.generateAiScript(
                    topic = topic,
                    language = _currentLanguage.value,
                    format = format,
                    durationSeconds = duration,
                    enableResearch = enableResearch,
                    onChunk = { chunk ->
                        _streamingText.value = _streamingText.value + chunk
                    }
                )

                _activeProject.value = project
                _liveJobStatus.value = LiveJobStatus.IDLE
                _isBusy.value = false
                showStatus("AI Script generation completed!")

                NotificationHelper.showNotification(
                    getApplication(),
                    102,
                    "AI Script Ready",
                    "Generated ${project.scenes.size} scenes for '${project.title}'"
                )

                navigateTo(Screen.EDITOR)
            } catch (e: Exception) {
                _liveJobStatus.value = LiveJobStatus.FAILED
                _isBusy.value = false
                showStatus("Generation error: ${e.message}")
            }
        }
    }

    fun stopGeneration() {
        streamingJob?.cancel()
        _isBusy.value = false
        _liveJobStatus.value = LiveJobStatus.IDLE
        showStatus("AI generation stopped.")
    }

    fun setActiveProject(project: ProjectItem) {
        _activeProject.value = project
    }

    fun updateActiveProject(updated: ProjectItem) {
        _activeProject.value = updated
        viewModelScope.launch {
            repository.saveProject(updated)
            showStatus("Project saved to local database")
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_activeProject.value?.id == id) {
                _activeProject.value = null
            }
            showStatus("Project deleted")
        }
    }

    fun duplicateProject(project: ProjectItem) {
        viewModelScope.launch {
            val dup = project.copy(
                id = "proj_" + UUID.randomUUID().toString().take(8),
                title = "${project.title} (Copy)",
                lastModified = System.currentTimeMillis()
            )
            repository.saveProject(dup)
            showStatus("Project duplicated")
        }
    }

    // Live AI Chat (Conversational Content Director)
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessageItem(UUID.randomUUID().toString(), "user", text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isBusy.value = true
            val aiResponse = repository.sendChatMessage(text, _chatMessages.value, _currentLanguage.value)
            val aiMsg = ChatMessageItem(
                id = UUID.randomUUID().toString(),
                sender = "ai",
                content = aiResponse,
                isScriptSuggestion = true
            )
            _chatMessages.value = _chatMessages.value + aiMsg
            _isBusy.value = false
        }
    }

    // AI API Verification & Diagnostics
    fun testGeminiApiConnection() {
        viewModelScope.launch {
            _isTestingApi.value = true
            showStatus("Testing live connection to Gemini 3.5 Flash...")
            val result = repository.testGeminiConnection()
            _apiDiagnosticResult.value = result
            _isTestingApi.value = false
            showStatus(if (result.isSuccess) "Gemini API verified (${result.latencyMs}ms)" else "Gemini API check: ${result.message.take(45)}")
        }
    }

    // AI Viral Hook Optimizer
    fun generateViralHooksForTopic(topic: String) {
        viewModelScope.launch {
            _isGeneratingHooks.value = true
            showStatus("Generating viral hooks with AI...")
            val hooks = repository.generateViralHooks(topic, _currentLanguage.value)
            _viralHooks.value = hooks
            _isGeneratingHooks.value = false
            showStatus("Generated ${hooks.size} viral hooks!")
        }
    }

    fun applyHookToActiveProject(hookText: String) {
        val proj = _activeProject.value ?: return
        val updated = proj.copy(hook = hookText, lastModified = System.currentTimeMillis())
        updateActiveProject(updated)
        showStatus("Hook updated in project!")
    }

    // AI SEO & Metadata Engine
    fun generateSeoForActiveProject() {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            _isGeneratingSeo.value = true
            showStatus("Generating High-CTR YouTube SEO...")
            val fullScript = proj.scenes.joinToString("\n") { it.narration }
            val seo = repository.generateSeoMetadata(proj.topic, fullScript, proj.language)
            _seoMetadata.value = seo
            _isGeneratingSeo.value = false
            showStatus("Generated ${seo.titles.size} titles and SEO description!")
        }
    }

    fun applySeoToActiveProject(title: String, desc: String) {
        val proj = _activeProject.value ?: return
        val updated = proj.copy(
            title = title,
            thumbnailTitle = title.take(25),
            lastModified = System.currentTimeMillis()
        )
        updateActiveProject(updated)
        showStatus("Title and thumbnail headline updated!")
    }

    // AI Viral Ideas Brainstorming
    fun brainstormIdeas(niche: String) {
        viewModelScope.launch {
            _isBrainstorming.value = true
            showStatus("Brainstorming viral video ideas...")
            val ideas = repository.brainstormViralIdeas(niche, _currentLanguage.value)
            _brainstormedIdeas.value = ideas
            _isBrainstorming.value = false
            showStatus("Generated ${ideas.size} video concepts!")
        }
    }

    // Voice Studio Controls with Native Device TTS
    fun selectVoice(voice: VoiceProfile) {
        _selectedVoice.value = voice
    }

    fun setVoiceSpeed(speed: Float) {
        _voiceSpeed.value = speed
    }

    fun setVoicePitch(pitch: Float) {
        _voicePitch.value = pitch
    }

    fun testNarrationPlayback(sampleText: String? = null) {
        val voice = _selectedVoice.value
        val textToSpeak = sampleText ?: voice.previewText
        _isVoicePlaying.value = true
        repository.ttsEngine.speak(textToSpeak, voice.language, _voiceSpeed.value, _voicePitch.value)
        viewModelScope.launch {
            delay(2500)
            _isVoicePlaying.value = false
        }
    }

    fun stopNarration() {
        repository.ttsEngine.stop()
        _isVoicePlaying.value = false
    }

    // Thumbnail Controls
    fun setThumbnailHeadline(text: String) {
        _thumbnailHeadline.value = text
    }

    fun setThumbnailBadge(badge: String) {
        _thumbnailBadge.value = badge
    }

    // Real Video Rendering
    fun renderProject(project: ProjectItem, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isRendering.value = true
            _renderProgress.value = 0
            _liveJobStatus.value = LiveJobStatus.RENDERING
            showStatus("Initiating video render...")

            val result = repository.renderVideo(project) { p ->
                _renderProgress.value = p
            }

            _isRendering.value = false
            _liveJobStatus.value = LiveJobStatus.IDLE

            if (result.success && result.file != null) {
                showStatus("Video rendered successfully! File: ${result.file.name}")
                NotificationHelper.showNotification(
                    getApplication(),
                    103,
                    "Video Render Completed",
                    "Rendered ${result.durationSeconds}s (${result.resolution}) MP4 file."
                )
                onComplete()
            } else {
                showStatus("Render error: ${result.error}")
            }
        }
    }

    // Real YouTube Upload
    fun publishToYouTube(title: String, desc: String, privacy: String, onFinished: () -> Unit) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            _isUploading.value = true
            _uploadProgress.value = 0
            _liveJobStatus.value = LiveJobStatus.UPLOADING
            showStatus("Preparing YouTube Resumable Upload...")

            val uploadResult = repository.uploadToYouTube(
                project = proj,
                title = title,
                description = desc,
                tags = listOf("AI", "YouTubeAutomation", "Shorts", "Tech"),
                privacy = privacy,
                authToken = "oauth_token_placeholder", // Passed to API client
                onProgress = { p -> _uploadProgress.value = p }
            )

            _isUploading.value = false
            _liveJobStatus.value = LiveJobStatus.IDLE

            if (uploadResult.success) {
                showStatus("Published! Video ID: ${uploadResult.videoId}")
                NotificationHelper.showNotification(
                    getApplication(),
                    104,
                    "YouTube Upload Successful",
                    "Video is live on YouTube at ${uploadResult.watchUrl}"
                )
            } else {
                showStatus("Upload note: ${uploadResult.error}")
            }
            onFinished()
        }
    }

    fun toggleAutomation(id: String) {
        viewModelScope.launch {
            val currentList = automations.value
            val target = currentList.find { it.id == id } ?: return@launch
            val updated = target.copy(isActive = !target.isActive)
            repository.saveAutomation(updated)
            showStatus(if (updated.isActive) "Automation workflow resumed" else "Automation workflow paused")
        }
    }

    fun createAutomation(title: String, template: String, schedule: String, autoPub: Boolean) {
        viewModelScope.launch {
            val workflow = AutomationWorkflow(
                id = "auto_" + UUID.randomUUID().toString().take(8),
                title = title,
                templateName = template,
                language = _currentLanguage.value,
                schedule = schedule,
                isActive = true,
                autoPublish = autoPub,
                lastRunStatus = "Scheduled"
            )
            repository.saveAutomation(workflow)
            showStatus("New automation workflow registered")
        }
    }

    // ==========================================
    // STAGE 5: SCHEDULER & EMERGENCY STOP METHODS
    // ==========================================

    fun scheduleAutomationJob(
        title: String,
        topic: String,
        format: VideoFormat,
        timeZoneId: String,
        scheduleTime: String,
        autoPublish: Boolean,
        maxRetries: Int = 3
    ) {
        viewModelScope.launch {
            try {
                val job = repository.scheduleAutomationJob(
                    title = title,
                    topic = topic,
                    format = format,
                    language = _currentLanguage.value,
                    timeZoneId = timeZoneId,
                    scheduleTime = scheduleTime,
                    autoPublish = autoPublish,
                    maxRetries = maxRetries
                )
                showStatus("Job '${job.title}' scheduled ($timeZoneId, $scheduleTime)")
            } catch (e: Exception) {
                showStatus(e.message ?: "Failed to schedule job")
            }
        }
    }

    fun triggerEmergencyStop() {
        viewModelScope.launch {
            repository.triggerEmergencyStop()
            showStatus("EMERGENCY STOP ACTIVATED: All automation halted!")
        }
    }

    fun resetEmergencyStop() {
        repository.resetEmergencyStop()
        showStatus("Emergency Stop cleared. Automation is active.")
    }

    fun cancelScheduledJob(jobId: String) {
        viewModelScope.launch {
            repository.cancelScheduledJob(jobId)
            showStatus("Job cancelled")
        }
    }

    fun deleteScheduledJob(jobId: String) {
        viewModelScope.launch {
            repository.deleteScheduledJob(jobId)
            showStatus("Job removed")
        }
    }

    fun executeScheduledJobNow(jobId: String) {
        if (isEmergencyStopped.value) {
            showStatus("Emergency Stop is currently active. Clear emergency stop first.")
            return
        }
        repository.executeJobNow(jobId)
        showStatus("Job pipeline started in background")
    }

    // ==========================================
    // STAGE 5: CHANNEL ANALYST AI METHODS
    // ==========================================

    fun refreshChannelAnalytics(dateRange: String = "28 Days") {
        viewModelScope.launch {
            _channelOverview.value = repository.fetchChannelAnalytics(dateRange)
            _detailedVideos.value = repository.fetchDetailedVideos()
        }
    }

    fun runAiChannelAudit() {
        viewModelScope.launch {
            _isAnalyzingChannel.value = true
            showStatus("Gemini 3.5 Flash is analyzing your channel metrics...")
            try {
                val report = repository.generateChannelAudit(_channelOverview.value, _currentLanguage.value)
                _analystReport.value = report
                _dataBackedIdeas.value = report.ideas
                showStatus("Channel Audit complete: Overall Grade ${report.overallGrade}")
            } catch (e: Exception) {
                showStatus("Audit failed: ${e.message}")
            } finally {
                _isAnalyzingChannel.value = false
            }
        }
    }

    fun generate10DataBackedIdeas(niche: String = "") {
        viewModelScope.launch {
            _isAnalyzingChannel.value = true
            try {
                val targetNiche = if (niche.isNotBlank()) niche else "YouTube Growth & AI"
                val ideas = repository.generate10DataBackedIdeas(targetNiche, _currentLanguage.value)
                _dataBackedIdeas.value = ideas
                showStatus("Generated 10 data-backed viral concepts")
            } catch (e: Exception) {
                showStatus("Ideas generation error: ${e.message}")
            } finally {
                _isAnalyzingChannel.value = false
            }
        }
    }

    fun auditTitleAndThumbnail(title: String, niche: String = "") {
        viewModelScope.launch {
            if (title.isBlank()) {
                showStatus("Please enter a video title to analyze")
                return@launch
            }
            _isAnalyzingChannel.value = true
            try {
                val audit = repository.auditTitleAndThumbnail(title, niche, _currentLanguage.value)
                _titleThumbnailAudit.value = audit
                showStatus("Title & Thumbnail analyzed: Score ${audit.clickabilityScore}/100")
            } catch (e: Exception) {
                showStatus("Audit error: ${e.message}")
            } finally {
                _isAnalyzingChannel.value = false
            }
        }
    }

    fun searchCompetitor(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                showStatus("Please enter a channel handle or name")
                return@launch
            }
            _isAnalyzingChannel.value = true
            try {
                val result = repository.analyzeCompetitor(query)
                _competitorAnalysis.value = result
                showStatus("Competitor analysis completed for ${result.channelHandle}")
            } catch (e: Exception) {
                showStatus("Competitor search error: ${e.message}")
            } finally {
                _isAnalyzingChannel.value = false
            }
        }
    }

    fun useIdeaInEditor(idea: PersonalizedContentIdea) {
        viewModelScope.launch {
            val project = ProjectItem(
                id = "proj_" + UUID.randomUUID().toString().take(8),
                title = idea.title,
                topic = idea.title,
                language = _currentLanguage.value,
                format = idea.format,
                durationSeconds = if (idea.format == VideoFormat.SHORTS) 45 else 120,
                status = ProjectStatus.READY,
                hook = idea.hook,
                scenes = idea.outline.mapIndexed { idx, bullet ->
                    SceneItem(
                        id = "scene_$idx",
                        order = idx + 1,
                        narration = bullet,
                        visualPrompt = "Dynamic high-impact visual representing $bullet"
                    )
                }
            )
            repository.saveProject(project)
            _activeProject.value = project
            navigateTo(Screen.EDITOR)
            showStatus("Idea loaded into Editor: ${idea.title}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.ttsEngine.shutdown()
    }
}

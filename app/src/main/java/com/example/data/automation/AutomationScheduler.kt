package com.example.data.automation

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.ScheduledJobEntity
import com.example.data.media.LocalMediaRenderer
import com.example.data.model.*
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.YouTubeApiClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DuplicateJobException(message: String) : Exception(message)
class EmergencyStopActiveException(message: String) : Exception(message)

class AutomationScheduler(
    private val context: Context,
    private val database: AppDatabase
) {
    private val TAG = "AutomationScheduler"
    private val jobDao = database.scheduledJobDao()
    private val projectDao = database.projectDao()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val runningJobTokens = ConcurrentHashMap<String, Job>()

    // Global Emergency Stop state
    private val _isEmergencyStopped = MutableStateFlow(false)
    val isEmergencyStopped: StateFlow<Boolean> = _isEmergencyStopped.asStateFlow()

    // Persistent Jobs Stream
    val allJobs: Flow<List<ScheduledJob>> = jobDao.getAllJobs().map { list ->
        list.map { it.toDomain() }
    }

    val schedulerState: Flow<SchedulerState> = combine(allJobs, isEmergencyStopped) { jobs, emergencyStopped ->
        SchedulerState(
            isEmergencyStopped = emergencyStopped,
            totalJobsCount = jobs.size,
            pendingCount = jobs.count { it.status == ScheduledJobStatus.PENDING },
            runningCount = jobs.count { it.status == ScheduledJobStatus.RUNNING },
            completedCount = jobs.count { it.status == ScheduledJobStatus.COMPLETED },
            emergencyStoppedCount = jobs.count { it.status == ScheduledJobStatus.EMERGENCY_STOPPED }
        )
    }

    /**
     * Schedules a new automation job with time zone calculation and duplicate prevention.
     */
    suspend fun scheduleJob(
        title: String,
        topic: String,
        format: VideoFormat,
        language: AppLanguage,
        timeZoneId: String,
        scheduleTime: String,
        autoPublish: Boolean,
        maxRetries: Int = 3
    ): ScheduledJob = withContext(Dispatchers.IO) {
        if (_isEmergencyStopped.value) {
            throw EmergencyStopActiveException("Emergency Stop is currently active. Reset Emergency Stop before scheduling new jobs.")
        }

        // Generate normalized deduplication key
        val cleanTopic = topic.trim().lowercase()
        val deduplicationKey = "${cleanTopic}_${format.name}_${scheduleTime.trim()}_$timeZoneId"

        // Duplicate-job prevention check
        val existingActive = jobDao.findActiveJobByDeduplicationKey(deduplicationKey)
        if (existingActive != null) {
            throw DuplicateJobException(
                "Duplicate Job Detected! An active job with topic '$topic' in time zone '$timeZoneId' ($scheduleTime) is already queued."
            )
        }

        // Calculate next run timestamp with time zone awareness
        val nextRun = calculateNextRunTimestamp(scheduleTime, timeZoneId)

        val id = "job_${UUID.randomUUID().toString().take(8)}"
        val initialLog = "[${getFormattedTimestamp()}] Job scheduled for time zone $timeZoneId (Target: $scheduleTime)"

        val entity = ScheduledJobEntity(
            id = id,
            title = title.ifBlank { "Automated $topic" },
            topic = topic,
            format = format.name,
            languageCode = language.code,
            scheduleTime = scheduleTime,
            timeZone = timeZoneId,
            status = ScheduledJobStatus.PENDING.name,
            maxRetries = maxRetries,
            currentRetry = 0,
            backoffSeconds = 30L,
            deduplicationKey = deduplicationKey,
            autoPublish = autoPublish,
            createdTimestamp = System.currentTimeMillis(),
            lastRunTimestamp = 0L,
            nextRunTimestamp = nextRun,
            logsJson = JSONArray(listOf(initialLog)).toString()
        )

        jobDao.insertJob(entity)
        Log.i(TAG, "Scheduled job $id successfully.")
        entity.toDomain()
    }

    /**
     * Triggers Emergency Stop: immediately halts all active running tasks and marks
     * pending/running jobs as EMERGENCY_STOPPED.
     */
    suspend fun triggerEmergencyStop() = withContext(Dispatchers.IO) {
        _isEmergencyStopped.value = true
        // Cancel all actively executing coroutines
        runningJobTokens.forEach { (jobId, runningCoro) ->
            runningCoro.cancel()
            Log.w(TAG, "Emergency Stop: Aborted execution coroutine for job $jobId")
        }
        runningJobTokens.clear()

        // Update database status
        jobDao.emergencyStopAllJobs()
        Log.w(TAG, "Emergency Stop triggered: All pending and running automation jobs halted.")
    }

    /**
     * Resumes operations after Emergency Stop has been cleared by creator.
     */
    fun resetEmergencyStop() {
        _isEmergencyStopped.value = false
        Log.i(TAG, "Emergency Stop cleared by user. Automation scheduler is now active.")
    }

    /**
     * Cancels an individual job
     */
    suspend fun cancelJob(jobId: String) = withContext(Dispatchers.IO) {
        runningJobTokens[jobId]?.cancel()
        runningJobTokens.remove(jobId)

        val job = jobDao.getJobById(jobId)
        if (job != null) {
            val updatedLogs = job.getLogsList().toMutableList().apply {
                add("[${getFormattedTimestamp()}] CANCELLED by creator.")
            }
            jobDao.updateJob(
                job.copy(
                    status = ScheduledJobStatus.CANCELLED.name,
                    logsJson = JSONArray(updatedLogs).toString()
                )
            )
        }
    }

    /**
     * Deletes a job
     */
    suspend fun deleteJob(jobId: String) = withContext(Dispatchers.IO) {
        runningJobTokens[jobId]?.cancel()
        runningJobTokens.remove(jobId)
        jobDao.deleteJob(jobId)
    }

    /**
     * Executes a job pipeline (Script -> TTS Narration -> Canvas Video -> Optional YouTube upload)
     * with retry policy and exponential backoff.
     */
    fun executeJobNow(jobId: String) {
        val executionCoro = scope.launch {
            if (_isEmergencyStopped.value) {
                Log.w(TAG, "Cannot execute job $jobId: Emergency Stop is active.")
                return@launch
            }

            var currentJob = jobDao.getJobById(jobId) ?: return@launch
            if (currentJob.status == ScheduledJobStatus.CANCELLED.name ||
                currentJob.status == ScheduledJobStatus.EMERGENCY_STOPPED.name) {
                return@launch
            }

            // Mark as RUNNING
            val logs = currentJob.getLogsList().toMutableList()
            logs.add("[${getFormattedTimestamp()}] Execution started (Attempt ${currentJob.currentRetry + 1}/${currentJob.maxRetries}).")
            currentJob = currentJob.copy(
                status = ScheduledJobStatus.RUNNING.name,
                lastRunTimestamp = System.currentTimeMillis(),
                logsJson = JSONArray(logs).toString()
            )
            jobDao.updateJob(currentJob)

            try {
                // Pipeline Step 1: AI Script Generation
                logs.add("[${getFormattedTimestamp()}] Generating video scenes with Gemini 3.5 Flash...")
                jobDao.updateJob(currentJob.copy(logsJson = JSONArray(logs).toString()))

                val lang = if (currentJob.languageCode == "ta") AppLanguage.TAMIL else AppLanguage.ENGLISH
                val format = if (currentJob.format == "STANDARD") VideoFormat.STANDARD else VideoFormat.SHORTS

                val scriptResult = GeminiApiClient.generateFullScript(
                    topic = currentJob.topic,
                    language = lang,
                    format = format,
                    durationSeconds = if (format == VideoFormat.SHORTS) 45 else 120,
                    enableResearch = true
                )

                // Pipeline Step 2: Render Video
                logs.add("[${getFormattedTimestamp()}] Assembling audio-visual scenes into MP4 container...")
                jobDao.updateJob(currentJob.copy(logsJson = JSONArray(logs).toString()))

                val projectItem = ProjectItem(
                    id = "proj_${UUID.randomUUID().toString().take(8)}",
                    title = scriptResult.title,
                    topic = currentJob.topic,
                    language = lang,
                    format = format,
                    durationSeconds = 45,
                    hook = scriptResult.hook,
                    scenes = scriptResult.scenes,
                    sources = scriptResult.sources
                )

                val renderResult = LocalMediaRenderer.renderProjectVideo(context, projectItem) {}

                if (!renderResult.success || renderResult.file == null) {
                    throw IllegalStateException("Video rendering pipeline failure: ${renderResult.error}")
                }

                logs.add("[${getFormattedTimestamp()}] MP4 generated: ${renderResult.file!!.name} (${renderResult.fileSizeBytes / 1024} KB)")

                // Pipeline Step 3: Auto-Publish if enabled
                if (currentJob.autoPublish) {
                    logs.add("[${getFormattedTimestamp()}] Auto-Publish: Staging video for YouTube...")
                    // If channel token is available, could call YouTubeApiClient.uploadVideo
                    // Otherwise mark ready for mobile-assisted publishing
                    logs.add("[${getFormattedTimestamp()}] Ready in YouTube Publisher queue.")
                }

                // Success!
                logs.add("[${getFormattedTimestamp()}] PIPELINE COMPLETED SUCCESSFULLY.")
                jobDao.updateJob(
                    currentJob.copy(
                        status = ScheduledJobStatus.COMPLETED.name,
                        logsJson = JSONArray(logs).toString()
                    )
                )

            } catch (e: CancellationException) {
                logs.add("[${getFormattedTimestamp()}] Job was interrupted by Emergency Stop or Cancel.")
                jobDao.updateJob(
                    currentJob.copy(
                        status = if (_isEmergencyStopped.value) ScheduledJobStatus.EMERGENCY_STOPPED.name else ScheduledJobStatus.CANCELLED.name,
                        logsJson = JSONArray(logs).toString()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Job $jobId execution encountered error: ${e.message}", e)
                val newRetry = currentJob.currentRetry + 1
                if (newRetry < currentJob.maxRetries) {
                    val backoff = currentJob.backoffSeconds * (1L shl (newRetry - 1))
                    val nextAttempt = System.currentTimeMillis() + (backoff * 1000L)
                    logs.add("[${getFormattedTimestamp()}] Error: ${e.message}. Retrying in ${backoff}s (Retry $newRetry/${currentJob.maxRetries}).")
                    jobDao.updateJob(
                        currentJob.copy(
                            status = ScheduledJobStatus.PENDING.name,
                            currentRetry = newRetry,
                            nextRunTimestamp = nextAttempt,
                            logsJson = JSONArray(logs).toString()
                        )
                    )
                } else {
                    logs.add("[${getFormattedTimestamp()}] FAILED permanently: ${e.message} (Max retries exceeded).")
                    jobDao.updateJob(
                        currentJob.copy(
                            status = ScheduledJobStatus.FAILED.name,
                            currentRetry = newRetry,
                            logsJson = JSONArray(logs).toString()
                        )
                    )
                }
            } finally {
                runningJobTokens.remove(jobId)
            }
        }
        runningJobTokens[jobId] = executionCoro
    }

    /**
     * Calculates future run timestamp taking time zone ID into account
     */
    fun calculateNextRunTimestamp(scheduleTime: String, timeZoneId: String): Long {
        val tz = TimeZone.getTimeZone(timeZoneId)
        val cal = Calendar.getInstance(tz)

        // Attempt to parse standard hours if available (e.g. "10:00 AM" or "Daily at 10:00")
        val lower = scheduleTime.lowercase()
        var hour = 10
        var minute = 0

        val regex = Regex("(\\d{1,2}):(\\d{2})\\s*(am|pm)?")
        val match = regex.find(lower)
        if (match != null) {
            val (hStr, mStr, amPm) = match.destructured
            hour = hStr.toIntOrNull() ?: 10
            minute = mStr.toIntOrNull() ?: 0
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
        }

        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // If time is in the past for today, advance to tomorrow
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return cal.timeInMillis
    }

    private fun getFormattedTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    }

    private fun ScheduledJobEntity.toDomain(): ScheduledJob {
        val statusEnum = try {
            ScheduledJobStatus.valueOf(status)
        } catch (e: Exception) {
            ScheduledJobStatus.PENDING
        }
        val formatEnum = if (format == "STANDARD") VideoFormat.STANDARD else VideoFormat.SHORTS
        val langEnum = if (languageCode == "ta") AppLanguage.TAMIL else AppLanguage.ENGLISH

        return ScheduledJob(
            id = id,
            title = title,
            topic = topic,
            format = formatEnum,
            language = langEnum,
            scheduleTime = scheduleTime,
            timeZone = timeZone,
            status = statusEnum,
            maxRetries = maxRetries,
            currentRetry = currentRetry,
            backoffSeconds = backoffSeconds,
            deduplicationKey = deduplicationKey,
            autoPublish = autoPublish,
            createdTimestamp = createdTimestamp,
            lastRunTimestamp = lastRunTimestamp,
            nextRunTimestamp = nextRunTimestamp,
            logs = getLogsList()
        )
    }

    private fun ScheduledJobEntity.getLogsList(): List<String> {
        return try {
            val arr = JSONArray(logsJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}

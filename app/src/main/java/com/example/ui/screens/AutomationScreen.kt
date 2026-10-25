package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.util.Strings
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AutomationScreen(
    viewModel: MainViewModel,
    language: AppLanguage
) {
    val isTa = language == AppLanguage.TAMIL
    val jobs by viewModel.scheduledJobs.collectAsState()
    val schedulerState by viewModel.schedulerState.collectAsState()
    val isEmergencyStopped by viewModel.isEmergencyStopped.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedLogsJob by remember { mutableStateOf<ScheduledJob?>(null) }

    // Dialog state
    var titleInput by remember { mutableStateOf("") }
    var topicInput by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(VideoFormat.SHORTS) }
    var scheduleTimeInput by remember { mutableStateOf("10:00 AM") }
    var selectedTimeZone by remember { mutableStateOf("Asia/Kolkata") }
    var maxRetries by remember { mutableStateOf(3) }
    var autoPublish by remember { mutableStateOf(false) }

    val availableTimeZones = listOf(
        "Asia/Kolkata" to "IST (India Standard Time)",
        "America/New_York" to "EST (US Eastern Time)",
        "America/Los_Angeles" to "PST (US Pacific Time)",
        "Europe/London" to "GMT (London / UK)",
        "UTC" to "UTC (Universal Coordinated Time)",
        "Asia/Tokyo" to "JST (Japan Standard Time)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // EMERGENCY STOP MASTER BANNER
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isEmergencyStopped) CrimsonPrimary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isEmergencyStopped) Color.White else CrimsonPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEmergencyStopped) Icons.Default.Warning else Icons.Default.ReportProblem,
                                contentDescription = null,
                                tint = if (isEmergencyStopped) CrimsonPrimary else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isEmergencyStopped) (if (isTa) "அவசர நிறுத்தம் இயக்கப்பட்டது!" else "EMERGENCY STOP ACTIVE")
                                else (if (isTa) "தானியங்கி பாதுகாப்பு கட்டுப்பாடு" else "Automation Kill Switch"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEmergencyStopped) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = if (isEmergencyStopped) "All pipeline background jobs are halted immediately."
                                else "Halt all pending, rendering, and uploading jobs in one tap.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = if (isEmergencyStopped) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isEmergencyStopped) {
                    Button(
                        onClick = { viewModel.resetEmergencyStop() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_emergency_stop_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CrimsonPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTa) "தானியங்கியை மீண்டும் இயக்கு (Resume)" else "Clear Stop & Resume Automation",
                            color = CrimsonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.triggerEmergencyStop() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trigger_emergency_stop_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTa) "அவசர நிறுத்தம் செய் (EMERGENCY STOP)" else "ACTIVATE EMERGENCY STOP",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Scheduler KPI Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatePill(
                title = if (isTa) "நிலுவை" else "Pending",
                count = schedulerState.pendingCount,
                color = AmberAccent,
                modifier = Modifier.weight(1f)
            )
            StatePill(
                title = if (isTa) "இயங்குகிறது" else "Running",
                count = schedulerState.runningCount,
                color = CyberCyan,
                modifier = Modifier.weight(1f)
            )
            StatePill(
                title = if (isTa) "முடிந்தது" else "Done",
                count = schedulerState.completedCount,
                color = StatusSuccess,
                modifier = Modifier.weight(1f)
            )
            StatePill(
                title = if (isTa) "நிறுத்தம்" else "Stopped",
                count = schedulerState.emergencyStoppedCount,
                color = CrimsonPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        // Create New Scheduled Job Button
        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auto_create_job_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isTa) "புதிய அட்டவணைப் பணியை உருவாக்கு" else "Schedule New Content Job",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = if (isTa) "அட்டவணைப்படுத்தப்பட்ட பணிகள் (${jobs.size})" else "Persistent Scheduled Jobs (${jobs.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        // Jobs List
        if (jobs.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isTa) "அட்டவணைப்படுத்தப்பட்ட பணிகள் எதுவும் இல்லை. புதிய பணியை சேர்க்கவும்."
                        else "No automated scheduled jobs. Tap above to schedule automated YouTube production.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(jobs, key = { it.id }) { job ->
                    JobCard(
                        job = job,
                        isTa = isTa,
                        onRunNow = { viewModel.executeScheduledJobNow(job.id) },
                        onCancel = { viewModel.cancelScheduledJob(job.id) },
                        onDelete = { viewModel.deleteScheduledJob(job.id) },
                        onViewLogs = { selectedLogsJob = job }
                    )
                }
            }
        }
    }

    // Schedule Job Dialog with Time Zone & Retry Policy
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(if (isTa) "புதிய தானியங்கி பணியை அட்டவணையிடு" else "Schedule Automated Video Job")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text(if (isTa) "பணியின் தலைப்பு" else "Job Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_job_title_input")
                    )

                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text(if (isTa) "வீடியோ தலைப்பு / பொருள்" else "Video Topic") },
                        placeholder = { Text("e.g. AI News Today") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_job_topic_input")
                    )

                    // Format Radio Options
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedFormat == VideoFormat.SHORTS,
                            onClick = { selectedFormat = VideoFormat.SHORTS }
                        )
                        Text("Shorts (9:16)", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = selectedFormat == VideoFormat.STANDARD,
                            onClick = { selectedFormat = VideoFormat.STANDARD }
                        )
                        Text("Standard (16:9)", fontSize = 12.sp)
                    }

                    // Schedule Time & Time Zone
                    OutlinedTextField(
                        value = scheduleTimeInput,
                        onValueChange = { scheduleTimeInput = it },
                        label = { Text("Execution Time (e.g. 10:00 AM)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Time Zone Selector Chips
                    Text(
                        text = "Target Time Zone: $selectedTimeZone",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                    LazyColumn(
                        modifier = Modifier.height(100.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(availableTimeZones) { (tzId, tzLabel) ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedTimeZone == tzId) CyberCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTimeZone = tzId }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tzLabel, fontSize = 11.sp)
                                    if (selectedTimeZone == tzId) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Auto-Publish Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Auto-Publish to YouTube", fontSize = 12.sp)
                        Switch(
                            checked = autoPublish,
                            onCheckedChange = { autoPublish = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val topic = if (topicInput.isNotBlank()) topicInput else "2026 AI Innovations"
                        val title = if (titleInput.isNotBlank()) titleInput else "Daily $topic"
                        viewModel.scheduleAutomationJob(
                            title = title,
                            topic = topic,
                            format = selectedFormat,
                            timeZoneId = selectedTimeZone,
                            scheduleTime = scheduleTimeInput,
                            autoPublish = autoPublish,
                            maxRetries = maxRetries
                        )
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text(if (isTa) "அட்டவணையிடு" else "Schedule Job")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(Strings.get("cancel", language))
                }
            }
        )
    }

    // Execution Logs Viewer Dialog
    if (selectedLogsJob != null) {
        val job = selectedLogsJob!!
        AlertDialog(
            onDismissRequest = { selectedLogsJob = null },
            title = {
                Text("Job Execution Audit Logs", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    Text(
                        text = "${job.title} • Status: ${job.status.name}",
                        fontSize = 11.sp,
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (job.logs.isEmpty()) {
                            item {
                                Text("No logs recorded yet.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            items(job.logs) { logEntry ->
                                Text(
                                    text = logEntry,
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedLogsJob = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun JobCard(
    job: ScheduledJob,
    isTa: Boolean,
    onRunNow: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onViewLogs: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(job.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = "Topic: ${job.topic} • ${job.timeZone} (${job.scheduleTime})",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                // Status Chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (job.status) {
                        ScheduledJobStatus.PENDING -> AmberAccent.copy(alpha = 0.2f)
                        ScheduledJobStatus.RUNNING -> CyberCyan.copy(alpha = 0.2f)
                        ScheduledJobStatus.COMPLETED -> StatusSuccess.copy(alpha = 0.2f)
                        ScheduledJobStatus.EMERGENCY_STOPPED -> CrimsonPrimary.copy(alpha = 0.2f)
                        ScheduledJobStatus.CANCELLED -> MaterialTheme.colorScheme.surface
                        ScheduledJobStatus.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = if (isTa) job.status.labelTa else job.status.labelEn,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (job.status) {
                            ScheduledJobStatus.PENDING -> AmberAccent
                            ScheduledJobStatus.RUNNING -> CyberCyan
                            ScheduledJobStatus.COMPLETED -> StatusSuccess
                            ScheduledJobStatus.EMERGENCY_STOPPED -> CrimsonPrimary
                            ScheduledJobStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                            ScheduledJobStatus.FAILED -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (job.status == ScheduledJobStatus.PENDING || job.status == ScheduledJobStatus.RUNNING) {
                        OutlinedButton(
                            onClick = onCancel,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Cancel", fontSize = 11.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onViewLogs,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logs", fontSize = 11.sp)
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }

                if (job.status != ScheduledJobStatus.RUNNING) {
                    Button(
                        onClick = onRunNow,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatePill(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

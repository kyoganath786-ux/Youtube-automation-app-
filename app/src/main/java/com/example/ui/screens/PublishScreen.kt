package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ConnectionStatus
import com.example.data.model.VideoFormat
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.util.Strings
import com.example.ui.util.YouTubeAppHelper
import com.example.ui.viewmodel.MainViewModel
import java.io.File

@Composable
fun PublishScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onPublished: () -> Unit
) {
    val context = LocalContext.current
    val isTa = language == AppLanguage.TAMIL
    val activeProject by viewModel.activeProject.collectAsState()
    val channel by viewModel.connectedChannel.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val isRendering by viewModel.isRendering.collectAsState()
    val renderProgress by viewModel.renderProgress.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()

    var title by remember {
        mutableStateOf(activeProject?.title ?: if (isTa) "2026 இல் 5 முக்கிய AI ரகசியங்கள் (Shorts)" else "5 Mind-Blowing AI Secrets (Shorts)")
    }
    var description by remember {
        mutableStateOf(
            if (isTa) "இந்த வீடியோவில் 2026 ஆம் ஆண்டின் மிகவும் பயனுள்ள AI கருவிகள் பற்றி விவரிக்கப்பட்டுள்ளது. சப்ஸ்கிரைப் செய்யுங்கள்!\n#AI #Tamil #Shorts #Tech"
            else "Discover the most incredible breakthrough AI tools in 2026. Hit subscribe for daily automation updates!\n#YouTubeAutomation #AI #Tech #Shorts"
        )
    }
    var privacy by remember { mutableStateOf("Public") }
    var isMadeForKids by remember { mutableStateOf(false) }
    var isScheduled by remember { mutableStateOf(false) }
    var scheduledTime by remember { mutableStateOf("Tomorrow at 6:00 PM") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showCloudApiSection by remember { mutableStateOf(false) }

    // User-assisted publishing checklist state
    var checkRendered by remember { mutableStateOf(activeProject?.localVideoPath != null) }
    var checkShared by remember { mutableStateOf(false) }
    var checkCopiedMeta by remember { mutableStateOf(false) }
    var checkSetAudience by remember { mutableStateOf(false) }
    var checkStudioDone by remember { mutableStateOf(false) }

    val privacyOptions = listOf("Public", "Unlisted", "Private")
    val isConnected = channel.status == ConnectionStatus.CONNECTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Screen Header
        Text(
            text = if (isTa) "வெளியீட்டு மையம்" else "Publishing Hub",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = if (isTa) "YouTube செயலியில் 1-தட்டலில் பகிரவும் அல்லது Cloud API மூலம் பதிவேற்றவும்"
            else "Share directly to YouTube app (No API key needed) or use Cloud API",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        // Project Summary Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CrimsonPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (activeProject?.format == VideoFormat.SHORTS) Icons.Default.PhoneAndroid else Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeProject?.title ?: "Ready Video",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Text(
                        text = "Duration: ${activeProject?.durationSeconds ?: 45}s • Format: ${activeProject?.format?.aspectRatio ?: "9:16"} • ${activeProject?.status?.name ?: "DRAFT"}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // ==================== PRIMARY METHOD: MOBILE SHARE TO YOUTUBE APP ====================
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberAccent.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = AmberAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTa) "முறை 1: YouTube செயலிக்கு பகிரவும் (API சாவி தேவையில்லை)"
                        else "Mode 1: Share to YouTube App (No API Key)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isTa) "ரெண்டர் செய்யப்பட்ட வீடியோவை நேரடியாக அதிகாரப்பூர்வ YouTube செயலியின் பதிவேற்ற திரைக்கு அனுப்புகிறது."
                    else "Sends rendered MP4 directly to the official YouTube app via Android System Share Sheet. Zero API key or Google Cloud setup needed!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Render or Share Action
                Button(
                    onClick = {
                        val proj = activeProject
                        if (proj != null) {
                            val localFile = proj.localVideoPath?.let { File(it) }
                            if (localFile != null && localFile.exists() && localFile.length() > 0) {
                                val shared = YouTubeAppHelper.shareVideoToYouTube(context, localFile, title, description)
                                if (shared) {
                                    checkShared = true
                                }
                            } else {
                                // Render first then share
                                viewModel.renderProject(proj) {
                                    checkRendered = true
                                    val renderedFile = File(context.filesDir, "rendered_videos").listFiles()?.maxByOrNull { it.lastModified() }
                                    if (renderedFile != null && renderedFile.exists()) {
                                        YouTubeAppHelper.shareVideoToYouTube(context, renderedFile, title, description)
                                        checkShared = true
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isRendering && !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_share_video_youtube"),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isRendering) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(if (isTa) "ரெண்டர் செய்யப்படுகிறது ($renderProgress%)..." else "Rendering Video ($renderProgress%)...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.SendToMobile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTa) "YouTube செயலியில் பகிருங்கள் & பதிவேற்றுங்கள்" else "Share Video to YouTube App",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Copy Actions for Title & Description
                Text(
                    text = if (isTa) "மெட்டாடேட்டா நகல் கருவிகள்" else "Quick Metadata Copy to Clipboard",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            YouTubeAppHelper.copyToClipboard(context, "Title", title)
                            checkCopiedMeta = true
                        },
                        modifier = Modifier.weight(1f).testTag("btn_copy_title"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTa) "தலைப்பு நகல்" else "Copy Title", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            YouTubeAppHelper.copyToClipboard(context, "Description", "$title\n\n$description")
                            checkCopiedMeta = true
                        },
                        modifier = Modifier.weight(1f).testTag("btn_copy_desc"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTa) "விளக்கம் நகல்" else "Copy Desc", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Open YouTube Studio directly to finalize
                OutlinedButton(
                    onClick = {
                        YouTubeAppHelper.openYouTubeStudioApp(context)
                        checkStudioDone = true
                    },
                    modifier = Modifier.fillMaxWidth().testTag("btn_open_studio_publish"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.SettingsApplications, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTa) "YouTube Studio திறந்து இறுதி அமைப்புகளை சரிபார்க்கவும்" else "Open YouTube Studio to Finalize",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step-by-Step Publishing Checklist
                Text(
                    text = if (isTa) "வெளியீட்டு சரிபார்ப்புப் பட்டியல் (Checklist)" else "Assisted Publishing Checklist",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                ChecklistItem(
                    checked = checkRendered || activeProject?.localVideoPath != null,
                    onCheckedChange = { checkRendered = it },
                    title = if (isTa) "1. MP4 வீடியோ ரெண்டர் செய்யப்பட்டது" else "1. Video rendered to disk"
                )
                ChecklistItem(
                    checked = checkShared,
                    onCheckedChange = { checkShared = it },
                    title = if (isTa) "2. YouTube செயலிக்கு அனுப்பப்பட்டது" else "2. Shared into official YouTube app"
                )
                ChecklistItem(
                    checked = checkCopiedMeta,
                    onCheckedChange = { checkCopiedMeta = it },
                    title = if (isTa) "3. தலைப்பு & விளக்கம் ஒட்டப்பட்டது" else "3. Title & description pasted"
                )
                ChecklistItem(
                    checked = checkSetAudience,
                    onCheckedChange = { checkSetAudience = it },
                    title = if (isTa) "4. பார்வை & பார்வையாளர்கள் தேர்வு செய்யப்பட்டது" else "4. Visibility and audience set"
                )
                ChecklistItem(
                    checked = checkStudioDone,
                    onCheckedChange = { checkStudioDone = it },
                    title = if (isTa) "5. YouTube Studio-வில் சரிபார்க்கப்பட்டது" else "5. Verified in YouTube Studio app"
                )
            }
        }

        // Title Input Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("publish_title", language),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth().testTag("publish_title_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Description Input Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("publish_desc", language),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth().testTag("publish_desc_input"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3
                )
            }
        }

        // Privacy Selector Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("privacy_label", language),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    privacyOptions.forEach { opt ->
                        FilterChip(
                            selected = privacy == opt,
                            onClick = { privacy = opt },
                            label = { Text(opt, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Audience & Schedule Toggles Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTa) "குழந்தைகளுக்கானதா? (Made for Kids)" else "Made for Kids",
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = isMadeForKids,
                        onCheckedChange = { isMadeForKids = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.get("schedule_publish", language),
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = isScheduled,
                        onCheckedChange = { isScheduled = it }
                    )
                }

                if (isScheduled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = scheduledTime,
                        onValueChange = { scheduledTime = it },
                        label = { Text(if (isTa) "வெளியீட்டு நேரம்" else "Publish Schedule") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // ==================== MODE 2: DIRECT CLOUD API UPLOAD (OPTIONAL) ====================
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCloudApiSection = !showCloudApiSection },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CrimsonPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isTa) "முறை 2: Direct Cloud API Upload (விரும்பினால்)" else "Mode 2: Direct Cloud API Upload",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isConnected) "Channel: ${channel.channelName}" else "Requires authorized account",
                                fontSize = 11.sp,
                                color = if (isConnected) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = if (showCloudApiSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                if (showCloudApiSection) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isTa) "இந்த முறை YouTube Data API v3 மூலம் பின்னணியில் தானாக பதிவேற்றும். இதற்கு Channel Studio-வில் கணக்கை இணைத்திருக்க வேண்டும்."
                        else "Direct background upload via YouTube Data API v3 without opening the YouTube app. Requires connected channel in Channel Studio.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isUploading) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isTa) "YouTube-ல் பதிவேற்றப்படுகிறது ($uploadProgress%)..." else "Uploading to YouTube ($uploadProgress%)...",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (uploadProgress < 100) "Streaming Bytes" else "Processing Live",
                                    style = MaterialTheme.typography.labelSmall.copy(color = CyberCyan)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { uploadProgress / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = CrimsonPrimary
                            )
                        }
                    } else {
                        Button(
                            onClick = { showConfirmDialog = true },
                            enabled = isConnected && !isUploading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("publish_submit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isConnected) (if (isTa) "Cloud API மூலம் பதிவேற்று" else "Upload via Cloud API")
                                else (if (isTa) "API கணக்கு இணைக்கப்படவில்லை" else "Account Not Connected for API"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Confirmation Safeguard Dialog for Mode 2
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(if (isTa) "API வெளியீட்டை உறுதிப்படுத்தவும்" else "Confirm YouTube API Publishing")
            },
            text = {
                Text(
                    text = if (isTa) "இந்த வீடியோ உங்கள் அங்கீகரிக்கப்பட்ட YouTube சேனலில் ($privacy) பதிவேற்றப்படும். தொடரலாமா?"
                    else "This video will be uploaded to your authorized channel via YouTube Data API with visibility set to '$privacy'. Proceed?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.publishToYouTube(title, description, privacy) {
                            onPublished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text(if (isTa) "வெளியிடு" else "Publish Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(Strings.get("cancel", language))
                }
            }
        )
    }
}

@Composable
private fun ChecklistItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

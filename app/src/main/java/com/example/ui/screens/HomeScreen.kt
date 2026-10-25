package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.util.Strings
import com.example.ui.util.YouTubeAppHelper
import com.example.ui.viewmodel.MainViewModel
import java.io.File

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val isTa = language == AppLanguage.TAMIL
    val projects by viewModel.projects.collectAsState()
    val automations by viewModel.automations.collectAsState()
    val channel by viewModel.connectedChannel.collectAsState()
    val isConnected = channel.status == ConnectionStatus.CONNECTED

    val isYtInstalled = remember { YouTubeAppHelper.isYouTubeInstalled(context) }
    val isStudioInstalled = remember { YouTubeAppHelper.isYouTubeStudioInstalled(context) }
    val activeProject by viewModel.activeProject.collectAsState()
    val brainstormedIdeas by viewModel.brainstormedIdeas.collectAsState()
    val isBrainstorming by viewModel.isBrainstorming.collectAsState()
    var showBrainstormDialog by remember { mutableStateOf(false) }
    val isGeminiConfigured = remember { com.example.data.remote.GeminiApiClient.isApiKeyConfigured() }

    var showChecklistDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Gemini AI Tools & API Key Resource Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTa) "Gemini 3.5 Flash AI கருவிகள்" else "Gemini 3.5 Flash AI Tools",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isGeminiConfigured) StatusSuccess.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isGeminiConfigured) "KEY ACTIVE" else "OFFLINE READY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGeminiConfigured) StatusSuccess else AmberAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                if (brainstormedIdeas.isEmpty()) {
                                    viewModel.brainstormIdeas("Tech & AI")
                                }
                                showBrainstormDialog = true
                            },
                            modifier = Modifier.weight(1f).testTag("btn_brainstorm_ideas"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            if (isBrainstorming) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTa) "வைரல் யோசனைகள்" else "Viral Ideas", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        FilledTonalButton(
                            onClick = { onNavigate(Screen.CHAT) },
                            modifier = Modifier.weight(1f).testTag("btn_ai_director_chat"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTa) "AI இயக்குனர்" else "AI Director", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { onNavigate(Screen.SETTINGS) },
                            modifier = Modifier.weight(0.9f).testTag("btn_api_keys_settings"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTa) "API சாவி" else "API Keys", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Mode 1 vs Mode 2 Operating Mode Banner
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isConnected) CrimsonPrimary.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isConnected) CrimsonPrimary else AmberAccent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.Verified else Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = if (isConnected) CrimsonPrimary else AmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isConnected) (if (isTa) "அங்கீகரிக்கப்பட்ட API பயன்முறை" else "Authorized API Mode")
                                else (if (isTa) "மொபைல் உதவி முறை — நேரடி கணக்கு இணைப்பு இல்லை" else "Mobile assistance mode — no direct account connection"),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isConnected) CrimsonPrimary else AmberAccent
                            )
                            Text(
                                text = if (isConnected) "Direct YouTube API access active"
                                else "Uses installed YouTube apps, Share Sheet, and clipboard — No API key needed!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    TextButton(
                        onClick = { onNavigate(Screen.CHANNEL) },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (isTa) "விவரங்கள்" else "Modes",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Official YouTube & Studio Launchers Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isTa) "அதிகாரப்பூர்வ YouTube செயலிகள்" else "Installed YouTube Apps & Launchers",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Open YouTube App Button
                        OutlinedButton(
                            onClick = { YouTubeAppHelper.openYouTubeApp(context) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_open_youtube_app"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (isTa) "YouTube திற" else "Open YouTube",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isYtInstalled) "App Installed" else "Web Fallback",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Open YouTube Studio App Button
                        OutlinedButton(
                            onClick = { YouTubeAppHelper.openYouTubeStudioApp(context) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_open_youtube_studio_app"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsVoice,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (isTa) "Studio திற" else "YouTube Studio",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isStudioInstalled) "App Installed" else "Web Studio",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Deep Link Analytics Action
                    FilledTonalButton(
                        onClick = { YouTubeAppHelper.openChannelAnalytics(context) },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTa) "Studio-வில் சேனல் பகுப்பாய்வைத் திறக்க" else "Open Channel Analytics in YouTube Studio",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Quick Studio Tools Grid
        item {
            Column {
                Text(
                    text = Strings.get("quick_studios", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudioActionCard(
                        title = Strings.get("create_video", language),
                        subtitle = if (isTa) "16:9 முழு வீடியோ" else "16:9 Longform AI",
                        icon = Icons.Default.VideoCall,
                        tint = CrimsonPrimary,
                        onClick = { onNavigate(Screen.WIZARD) },
                        modifier = Modifier.weight(1f)
                    )

                    StudioActionCard(
                        title = Strings.get("create_shorts", language),
                        subtitle = if (isTa) "9:16 வைரல் ஷார்ட்ஸ்" else "9:16 Vertical Reels",
                        icon = Icons.Default.FlashOn,
                        tint = AmberAccent,
                        onClick = { onNavigate(Screen.SHORTS) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudioActionCard(
                        title = if (isTa) "தம்பனைல்" else "Thumbnail",
                        subtitle = if (isTa) "AI அட்டைகள்" else "AI Cover Art",
                        icon = Icons.Default.Image,
                        tint = CyberCyan,
                        onClick = { onNavigate(Screen.THUMBNAILS) },
                        modifier = Modifier.weight(1f)
                    )

                    StudioActionCard(
                        title = if (isTa) "குரல் ஸ்டுடியோ" else "Voice Studio",
                        subtitle = if (isTa) "தமிழ் / English TTS" else "Neural Narrator",
                        icon = Icons.Default.Mic,
                        tint = Color(0xFFA855F7),
                        onClick = { onNavigate(Screen.VOICE) },
                        modifier = Modifier.weight(1f)
                    )

                    StudioActionCard(
                        title = if (isTa) "AI அரட்டை" else "AI Chat",
                        subtitle = if (isTa) "ஸ்கிரிப்ட் யோசனை" else "Script Ideation",
                        icon = Icons.Default.SmartToy,
                        tint = Color(0xFF10B981),
                        onClick = { onNavigate(Screen.CHAT) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Mobile Publishing Checklist Launcher Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Checklist, contentDescription = null, tint = StatusSuccess)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isTa) "YouTube வெளியீட்டு சரிபார்ப்பு பட்டியல்" else "YouTube Publishing Checklist",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isTa) "Studio-வில் எளிதாக பதிவேற்ற வழிகாட்டி" else "Step-by-step assistance for official Studio upload",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { showChecklistDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = if (isTa) "திற" else "Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    activeProject?.let { proj ->
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Active: ${proj.title}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Clipboard & Share actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { YouTubeAppHelper.copyToClipboard(context, "Title", proj.title) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Title", fontSize = 10.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val desc = "${proj.hook}\n\n#YouTubeAutomation #AI #Tech"
                                    YouTubeAppHelper.copyToClipboard(context, "Description", desc)
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Desc", fontSize = 10.sp)
                            }

                            Button(
                                onClick = {
                                    proj.localVideoPath?.let { path ->
                                        YouTubeAppHelper.shareVideoToYouTube(context, File(path), proj.title, proj.hook)
                                    } ?: run {
                                        viewModel.showStatus("Please render video before sharing to YouTube.")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Video", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Recent Projects Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("recent_projects", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = { onNavigate(Screen.PROJECTS) }) {
                    Text(text = if (isTa) "அனைத்தும்" else "View All", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (projects.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MovieFilter, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTa) "திட்டங்கள் எதுவும் இல்லை. புதிய வீடியோவை உருவாக்குங்கள்!" else "No projects yet. Start your first creation!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    items(projects.take(4)) { project ->
                        ProjectHomeCard(
                            project = project,
                            language = language,
                            onClick = {
                                viewModel.setActiveProject(project)
                                onNavigate(Screen.EDITOR)
                            }
                        )
                    }
                }
            }
        }
    }

    // User-Assisted YouTube Studio Checklist Dialog
    if (showChecklistDialog) {
        PublishingChecklistDialog(
            isTa = isTa,
            onDismiss = { showChecklistDialog = false },
            onOpenYouTube = { YouTubeAppHelper.openYouTubeApp(context) },
            onOpenStudio = { YouTubeAppHelper.openYouTubeStudioApp(context) }
        )
    }

    // AI Viral Ideas Brainstorm Dialog
    if (showBrainstormDialog) {
        AlertDialog(
            onDismissRequest = { showBrainstormDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AmberAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTa) "AI வைரல் யோசனைகள்" else "AI Viral Ideas (Gemini 3.5)")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isBrainstorming) {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = CrimsonPrimary)
                        }
                    } else {
                        brainstormedIdeas.forEach { idea ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    viewModel.generateNewProject(
                                        topic = idea.title,
                                        format = idea.format,
                                        duration = if (idea.format == VideoFormat.SHORTS) 45 else 180
                                    )
                                    showBrainstormDialog = false
                                    onNavigate(Screen.EDITOR)
                                }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = CrimsonPrimary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = idea.format.displayName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CrimsonPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = "Viral Score: ${idea.estimatedViralScore}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusSuccess
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = idea.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = "Hook: ${idea.suggestedHook}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.brainstormIdeas("Trending AI & Tech") },
                    enabled = !isBrainstorming,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text(if (isTa) "புதிய யோசனைகள்" else "Regenerate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBrainstormDialog = false }) {
                    Text(Strings.get("cancel", language))
                }
            }
        )
    }
}

@Composable
fun PublishingChecklistDialog(
    isTa: Boolean,
    onDismiss: () -> Unit,
    onOpenYouTube: () -> Unit,
    onOpenStudio: () -> Unit
) {
    var step1 by remember { mutableStateOf(false) }
    var step2 by remember { mutableStateOf(false) }
    var step3 by remember { mutableStateOf(false) }
    var step4 by remember { mutableStateOf(false) }
    var step5 by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isTa) "YouTube வெளியீட்டு சரிபார்ப்பு பட்டியல்" else "YouTube Studio Publishing Checklist")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isTa) "அதிகாரப்பூர்வ YouTube செயலியில் பதிவேற்ற படிகள் (API விசை தேவையில்லை):"
                    else "Steps to publish using official YouTube apps (No API key required):",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                ChecklistItem("1. Share rendered video to YouTube App", step1) { step1 = it }
                ChecklistItem("2. Paste generated Title from Clipboard", step2) { step2 = it }
                ChecklistItem("3. Paste Description & Hashtags", step3) { step3 = it }
                ChecklistItem("4. Set Visibility (Public / Scheduled) in Studio", step4) { step4 = it }
                ChecklistItem("5. Confirm upload in YouTube Studio", step5) { step5 = it }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onOpenYouTube, modifier = Modifier.weight(1f)) {
                        Text("Open YouTube", fontSize = 11.sp)
                    }
                    Button(onClick = onOpenStudio, colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary), modifier = Modifier.weight(1f)) {
                        Text("Open Studio", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isTa) "முடிந்தது" else "Done")
            }
        }
    )
}

@Composable
private fun ChecklistItem(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!isChecked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, fontSize = 12.sp)
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
        }
    }
}

@Composable
private fun StudioActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ProjectHomeCard(
    project: ProjectItem,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val isTa = language == AppLanguage.TAMIL
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                if (project.format == VideoFormat.SHORTS) Color(0xFF5B0E1E) else Color(0xFF102847),
                                Color(0xFF1B1D2A)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (project.format == VideoFormat.SHORTS) Icons.Default.PhoneAndroid else Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = project.format.aspectRatio,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "${project.durationSeconds}s",
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (project.status) {
                            ProjectStatus.READY -> StatusSuccess.copy(alpha = 0.2f)
                            ProjectStatus.RENDERING -> AmberAccent.copy(alpha = 0.2f)
                            ProjectStatus.UPLOADED -> CyberCyan.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Text(
                            text = if (isTa) project.status.labelTa else project.status.labelEn,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (project.status) {
                                ProjectStatus.READY -> StatusSuccess
                                ProjectStatus.RENDERING -> AmberAccent
                                ProjectStatus.UPLOADED -> CyberCyan
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = project.language.displayName,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

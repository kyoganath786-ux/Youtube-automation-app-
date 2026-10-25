package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.util.Strings
import com.example.ui.util.YouTubeAppHelper
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ChannelScreen(
    viewModel: MainViewModel,
    language: AppLanguage
) {
    val context = LocalContext.current
    val isTa = language == AppLanguage.TAMIL
    val channel by viewModel.connectedChannel.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Mode 1 (Mobile Assist), 1: Mode 2 (Cloud API)
    var channelInput by remember { mutableStateOf("@YouTube") }

    val isYtInstalled = remember { YouTubeAppHelper.isYouTubeInstalled(context) }
    val isStudioInstalled = remember { YouTubeAppHelper.isYouTubeStudioInstalled(context) }

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
            text = if (isTa) "சேனல் & யூடியூப் ஒருங்கிணைப்பு" else "Channel & YouTube Integration",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = if (isTa) "மொபைல் செயலி உதவி அல்லது நேரடி API முறையைத் தேர்ந்தெடுக்கவும்"
            else "Select Mobile App Assistance (Zero API Key) or Direct Cloud API Mode",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        // Mode Segmented Tab Switcher
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTa) "முறை 1: மொபைல் உதவி" else "Mode 1: Mobile Assist",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTa) "முறை 2: Cloud API" else "Mode 2: Cloud API",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // ==================== MODE 1: MOBILE APP ASSISTANCE (NO API KEY) ====================

            // Highlight Badge Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AmberAccent.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberAccent.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmberAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTa) "API சாவி தேவையில்லை (No API Key Required)" else "No YouTube API Key Required",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = AmberAccent)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isTa) "இந்த முறையில் உங்கள் சாதனத்தில் நிறுவப்பட்டுள்ள YouTube மற்றும் YouTube Studio செயலிகளைப் பயன்படுத்தி நேரடியாக வீடியோக்களைப் பகிரலாம் மற்றும் நிர்வகிக்கலாம்."
                        else "This mobile-first mode works seamlessly with official YouTube and YouTube Studio Android apps using Android Intents and the System Share Sheet. No Google Cloud project setup needed!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Installed Official Apps Detection Section
            Text(
                text = if (isTa) "நிறுவப்பட்டுள்ள யூடியூப் செயலிகள்" else "Installed Official Apps",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            // YouTube App Detection Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CrimsonPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "YouTube App",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isYtInstalled) StatusSuccess else StatusWarning)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isYtInstalled) (if (isTa) "செயலி நிறுவப்பட்டுள்ளது" else "App Installed (Ready)")
                                    else (if (isTa) "இணைய உலாவி மூலம் (Web Fallback)" else "Not Installed (Web Fallback)"),
                                    fontSize = 11.sp,
                                    color = if (isYtInstalled) StatusSuccess else StatusWarning
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { YouTubeAppHelper.openYouTubeApp(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("btn_channel_open_yt")
                    ) {
                        Text(if (isTa) "திறக்க" else "Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // YouTube Studio Detection Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SettingsApplications, contentDescription = null, tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "YouTube Studio App",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isStudioInstalled) StatusSuccess else StatusWarning)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isStudioInstalled) (if (isTa) "Studio நிறுவப்பட்டுள்ளது" else "Studio Installed (Ready)")
                                    else (if (isTa) "இணைய ஸ்டுடியோ மூலம் (Web Fallback)" else "Not Installed (Web Fallback)"),
                                    fontSize = 11.sp,
                                    color = if (isStudioInstalled) StatusSuccess else StatusWarning
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { YouTubeAppHelper.openYouTubeStudioApp(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("btn_channel_open_studio")
                    ) {
                        Text(if (isTa) "ஸ்டுடியோ திற" else "Open Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Studio Deep Links
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isTa) "விரைவு இணைப்புகள்" else "Quick Studio Deep Links",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { YouTubeAppHelper.openChannelAnalytics(context) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTa) "சேனல் பகுப்பாய்வுகளைத் திற (Analytics)" else "Open Channel Analytics in Studio",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 4-Step Mobile Assistance Flow Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isTa) "மொபைல் உதவி எப்படி செயல்படுகிறது?" else "How Mobile Assistance Works",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    MobileStepRow(1, if (isTa) "AI ஸ்கிரிப்ட், வீடியோ மற்றும் தம்பனைலை உருவாக்கவும்" else "Generate AI script, video & thumbnail inside the app")
                    MobileStepRow(2, if (isTa) "வீடியோவை ஒரே தட்டலில் YouTube செயலிக்கு பகிரவும்" else "Share rendered MP4 video directly to YouTube with 1-tap Share Sheet")
                    MobileStepRow(3, if (isTa) "AI தலைப்பு, விளக்கம் மற்றும் குறிச்சொற்களை நகலெடுக்கவும்" else "Copy AI-crafted Title, Description & Tags to Android Clipboard")
                    MobileStepRow(4, if (isTa) "YouTube Studio செயலியில் இறுதி அமைப்புகளைச் சரிபார்த்து வெளியிடவும்" else "Finalize monetization, audience & schedule in official YouTube Studio app")
                }
            }

        } else {
            // ==================== MODE 2: CLOUD API INTEGRATION ====================

            // Channel Status Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(
                                when (channel.status) {
                                    ConnectionStatus.CONNECTED -> CrimsonPrimary
                                    ConnectionStatus.CONNECTING -> StatusWarning
                                    ConnectionStatus.ERROR -> StatusError
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (channel.status) {
                                ConnectionStatus.CONNECTED -> Icons.Default.SmartDisplay
                                ConnectionStatus.CONNECTING -> Icons.Default.Sync
                                ConnectionStatus.ERROR -> Icons.Default.ErrorOutline
                                else -> Icons.Default.LinkOff
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = channel.channelName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = channel.handle,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (channel.status) {
                            ConnectionStatus.CONNECTED -> StatusSuccess.copy(alpha = 0.15f)
                            ConnectionStatus.CONNECTING -> StatusWarning.copy(alpha = 0.15f)
                            ConnectionStatus.ERROR -> StatusError.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (channel.status) {
                                            ConnectionStatus.CONNECTED -> StatusSuccess
                                            ConnectionStatus.CONNECTING -> StatusWarning
                                            ConnectionStatus.ERROR -> StatusError
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (channel.status) {
                                    ConnectionStatus.CONNECTED -> if (isTa) "இணைக்கப்பட்டுள்ளது (YouTube API)" else "Connected (Verified API)"
                                    ConnectionStatus.CONNECTING -> if (isTa) "சரிபார்க்கப்படுகிறது..." else "Connecting..."
                                    ConnectionStatus.ERROR -> if (isTa) "இணைப்பு பிழை" else "Connection Error"
                                    else -> if (isTa) "இணைக்கப்படவில்லை" else "Not Connected (API Idle)"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (channel.status) {
                                    ConnectionStatus.CONNECTED -> StatusSuccess
                                    ConnectionStatus.CONNECTING -> StatusWarning
                                    ConnectionStatus.ERROR -> StatusError
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    channel.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            }

            // Channel Statistics Row (Only if verified and connected)
            if (channel.status == ConnectionStatus.CONNECTED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ChannelStatCard(
                        title = Strings.get("yt_subscribers", language),
                        value = "${channel.subscribers / 1000}K",
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f)
                    )

                    ChannelStatCard(
                        title = Strings.get("yt_videos", language),
                        value = "${channel.totalVideos}",
                        icon = Icons.Default.VideoLibrary,
                        modifier = Modifier.weight(1f)
                    )

                    ChannelStatCard(
                        title = Strings.get("yt_views", language),
                        value = "${String.format("%.1f", channel.totalViews / 1000000.0)}M",
                        icon = Icons.Default.Visibility,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Connection Action Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isTa) "YouTube API இணைப்பு (Cloud Backend)" else "Direct YouTube Data API v3 Link",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isTa) "பின்னணி ஆட்டோமேஷன் மற்றும் கிளவுட் பதிவேற்றங்களுக்கு மட்டுமே இந்த இணைப்பு தேவை."
                        else "Only required for automated headless backend publishing and scheduled cron jobs. Enter handle (@YouTube) to verify.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = channelInput,
                        onValueChange = { channelInput = it },
                        label = { Text(if (isTa) "சேனல் ஹேண்டில் / ID" else "Channel Handle / ID") },
                        modifier = Modifier.fillMaxWidth().testTag("channel_handle_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (channel.status == ConnectionStatus.CONNECTED) {
                        OutlinedButton(
                            onClick = { viewModel.disconnectYouTubeAccount() },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("channel_disconnect_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Strings.get("btn_disconnect", language), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.connectYouTubeAccount(channelInput.trim())
                            },
                            enabled = !isBusy && channelInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("channel_connect_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Link, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(Strings.get("btn_connect_yt", language), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Scopes Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isTa) "அங்கீகரிக்கப்பட்ட அனுமதிகள் (Authorized Scopes)" else "Authorized Google OAuth Scopes",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ScopeItem("https://www.googleapis.com/auth/youtube.upload", "Direct Video & Shorts Uploads")
                    ScopeItem("https://www.googleapis.com/auth/youtube.readonly", "Channel Telemetry & Metadata")
                    ScopeItem("https://www.googleapis.com/auth/yt-analytics.readonly", "Watch Time & Audience Analytics")
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun MobileStepRow(step: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(AmberAccent),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$step", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp))
    }
}

@Composable
private fun ChannelStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ScopeItem(scope: String, purpose: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = purpose, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            Text(text = scope, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

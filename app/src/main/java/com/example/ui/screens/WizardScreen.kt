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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.LiveJobStatus
import com.example.data.model.VideoFormat
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.util.Strings
import com.example.ui.viewmodel.MainViewModel

@Composable
fun WizardScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onNavigateToEditor: () -> Unit
) {
    val isTa = language == AppLanguage.TAMIL
    var topic by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(VideoFormat.SHORTS) }
    var durationSec by remember { mutableStateOf(45) }
    var selectedStyle by remember { mutableStateOf("Cinematic 4K") }
    var enableResearch by remember { mutableStateOf(true) }

    val isBusy by viewModel.isBusy.collectAsState()
    val liveStatus by viewModel.liveJobStatus.collectAsState()
    val streamingText by viewModel.streamingText.collectAsState()

    val styles = listOf("Cinematic 4K", "Modern Tech", "Dramatic Viral", "Documentary")
    val durations = listOf(30, 45, 60, 180)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Topic Input Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("topic_label", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    placeholder = { Text(Strings.get("topic_hint", language), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("wizard_topic_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }
        }

        // Live Fact-Checking & Web Grounding Toggle
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = CyberCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isTa) "நேரலை இணைய உண்மை சரிபார்ப்பு" else "Live Web Sourcing & Fact Check",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isTa) "உண்மையான ஆதார இணைப்புகளுடன் ஸ்கிரிப்டை உருவாக்கும்" else "Retrieves real sources with timestamps and citations",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                    }
                }

                Switch(
                    checked = enableResearch,
                    onCheckedChange = { enableResearch = it }
                )
            }
        }

        // Format Selector (Shorts 9:16 vs Landscape 16:9)
        Column {
            Text(
                text = Strings.get("format_label", language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormatOptionCard(
                    title = "YouTube Shorts",
                    aspect = "9:16 Vertical",
                    icon = Icons.Default.PhoneAndroid,
                    isSelected = selectedFormat == VideoFormat.SHORTS,
                    onClick = { selectedFormat = VideoFormat.SHORTS },
                    modifier = Modifier.weight(1f)
                )

                FormatOptionCard(
                    title = "Standard Video",
                    aspect = "16:9 Landscape",
                    icon = Icons.Default.Tv,
                    isSelected = selectedFormat == VideoFormat.STANDARD,
                    onClick = { selectedFormat = VideoFormat.STANDARD },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Duration Options
        Column {
            Text(
                text = Strings.get("duration_label", language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durations.forEach { sec ->
                    val isSelected = durationSec == sec
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, CrimsonPrimary) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { durationSec = sec }
                    ) {
                        Text(
                            text = if (sec < 60) "${sec}s" else "${sec / 60}m",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Streaming Progressive Preview Card (if active)
        if (isBusy || streamingText.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
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
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = CrimsonPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTa) liveStatus.labelTa else liveStatus.labelEn,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (isBusy) {
                            TextButton(onClick = { viewModel.stopGeneration() }) {
                                Text(if (isTa) "நிறுத்து" else "Stop", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (streamingText.isNotEmpty()) streamingText else (if (isTa) "AI சிந்திக்கிறது..." else "Generating initial response..."),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        ),
                        maxLines = 8
                    )
                }
            }
        }

        // Generate Action Button
        Button(
            onClick = {
                val effectiveTopic = if (topic.isNotBlank()) topic else if (isTa) "2026 இல் 5 முக்கிய AI ரகசியங்கள்" else "5 Mind-Blowing AI Secrets in 2026"
                viewModel.generateNewProject(effectiveTopic, selectedFormat, durationSec, enableResearch)
            },
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("wizard_generate_button"),
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isBusy) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(if (isTa) "ஸ்கிரிப்ட் உருவாகிறது..." else "Streaming Script...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.get("generate_ai", language),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun FormatOptionCard(
    title: String,
    aspect: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, CrimsonPrimary) else null,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = aspect,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

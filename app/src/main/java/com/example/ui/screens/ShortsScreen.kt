package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.VideoFormat
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ShortsScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onNavigateToPublish: () -> Unit
) {
    val isTa = language == AppLanguage.TAMIL
    var hookText by remember {
        mutableStateOf(
            if (isTa) "நீங்கள் இதை நம்ப மாட்டீர்கள்! 99% பேர் அறியாத YouTube ரகசியம்!"
            else "You won't believe what happens next! The 99% secret nobody shares!"
        )
    }
    var captionStyle by remember { mutableStateOf("Karaoke Glow") }
    var backgroundMusic by remember { mutableStateOf("Cyber Synthwave") }
    var musicVolume by remember { mutableStateOf(0.35f) }
    var isSubtitlesEnabled by remember { mutableStateOf(true) }

    val captionStyles = listOf("Karaoke Glow", "Yellow Bold", "Clean White")
    val musicTracks = listOf("Cyber Synthwave", "Lo-Fi Focus", "Cinematic Tension", "Upbeat Energy")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Vertical 9:16 Shorts Simulator Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF520719),
                            Color(0xFF1B0B26),
                            Color(0xFF090A12)
                        )
                    )
                )
                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CrimsonPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SHORTS 9:16 LIVE PREVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Simulated Viral Captions
                if (isSubtitlesEnabled) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent)
                    ) {
                        Text(
                            text = hookText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (captionStyle == "Karaoke Glow") AmberAccent else Color.White,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 22.sp
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Text(text = backgroundMusic, fontSize = 11.sp, color = CyberCyan)
                }
            }
        }

        // Hook & Script Editor
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTa) "வைரல் ஹூக் உரை (Viral Hook Text)" else "Viral Hook Headline",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hookText,
                    onValueChange = { hookText = it },
                    modifier = Modifier.fillMaxWidth().testTag("shorts_hook_input"),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )
            }
        }

        // Captions Style Controls
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
                        text = if (isTa) "இயங்கும் சப்-டைட்டில் (Captions)" else "Dynamic Animated Captions",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Switch(
                        checked = isSubtitlesEnabled,
                        onCheckedChange = { isSubtitlesEnabled = it }
                    )
                }

                if (isSubtitlesEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        captionStyles.forEach { style ->
                            FilterChip(
                                selected = captionStyle == style,
                                onClick = { captionStyle = style },
                                label = { Text(style, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Background Music Selector
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTa) "பின்னணி இசை (Background Track)" else "Background Music Track",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    musicTracks.take(2).forEach { track ->
                        FilterChip(
                            selected = backgroundMusic == track,
                            onClick = { backgroundMusic = track },
                            label = { Text(track, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isTa) "இசை அளவு (Volume): ${(musicVolume * 100).toInt()}%" else "Music Volume: ${(musicVolume * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
                Slider(
                    value = musicVolume,
                    onValueChange = { musicVolume = it },
                    valueRange = 0f..1f
                )
            }
        }

        // Export to YouTube Publishing Button
        Button(
            onClick = {
                viewModel.generateNewProject(
                    topic = hookText,
                    format = VideoFormat.SHORTS,
                    duration = 45
                )
                viewModel.showStatus(if (isTa) "ஷார்ட்ஸ் உருவாக்கப்பட்டது! பதிவேற்றத்திற்கு தயார்." else "Shorts rendered! Ready for upload.")
                onNavigateToPublish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("shorts_export_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isTa) "ஷார்ட்ஸை ஏற்றுமதி செய் (Export Shorts)" else "Export & Publish Shorts",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

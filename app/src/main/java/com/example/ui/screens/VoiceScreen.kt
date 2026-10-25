package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.VoiceProfile
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.util.Strings
import com.example.ui.viewmodel.MainViewModel

@Composable
fun VoiceScreen(
    viewModel: MainViewModel,
    language: AppLanguage
) {
    val isTa = language == AppLanguage.TAMIL
    val voices = viewModel.availableVoices
    val selectedVoice by viewModel.selectedVoice.collectAsState()
    val speed by viewModel.voiceSpeed.collectAsState()
    val pitch by viewModel.voicePitch.collectAsState()
    val isPlaying by viewModel.isVoicePlaying.collectAsState()

    var testScript by remember {
        mutableStateOf(
            if (isTa) "வணக்கம்! யூடியூப் ஆட்டோமேஷன் AI மூலம் உங்கள் வீடியோக்களுக்கு மிக நேர்த்தியான தமிழ் நியூரல் குரல் தயாராக உள்ளது."
            else "Welcome to YouTube Automation AI. Studio-grade neural narration is ready for your next viral video."
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Waveform Soundboard Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedVoice.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${selectedVoice.language.displayName} • ${selectedVoice.gender} Neural Voice",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CrimsonPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "24kHz HD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Animated Sound Wave Bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(50.dp)
                ) {
                    val heights = listOf(14.dp, 28.dp, 45.dp, 32.dp, 18.dp, 40.dp, 50.dp, 36.dp, 22.dp, 44.dp, 28.dp, 16.dp)
                    heights.forEachIndexed { i, h ->
                        val barColor = if (isPlaying) (if (i % 2 == 0) CrimsonPrimary else AmberAccent) else MaterialTheme.colorScheme.outline
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(if (isPlaying) h else 14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(barColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Play / Test Voice Button
                Button(
                    onClick = { viewModel.testNarrationPlayback() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("voice_test_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) AmberAccent else CrimsonPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) (if (isTa) "ஒலிக்கிறது..." else "Speaking Audio...") else Strings.get("test_voice", language),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Narration Script Input
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTa) "குரல் உரை (Narration Text)" else "Narration Text Script",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = testScript,
                    onValueChange = { testScript = it },
                    modifier = Modifier.fillMaxWidth().testTag("voice_script_input"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3
                )
            }
        }

        // Voice Model Selector Carousel
        Column {
            Text(
                text = if (isTa) "கிடைக்கும் நியூரல் குரல்கள்" else "Available Neural Voice Actors",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            voices.forEach { voice ->
                val isSelected = selectedVoice.id == voice.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, CrimsonPrimary) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectVoice(voice) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CrimsonPrimary else MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = voice.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${voice.language.displayName} • ${voice.gender}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = CrimsonPrimary)
                        }
                    }
                }
            }
        }

        // Pitch & Speed Sliders
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${Strings.get("speed_slider", language)}: ${String.format("%.1f", speed)}x",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = speed,
                    onValueChange = { viewModel.setVoiceSpeed(it) },
                    valueRange = 0.5f..2.0f
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${Strings.get("pitch_slider", language)}: ${String.format("%.1f", pitch)}x",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = pitch,
                    onValueChange = { viewModel.setVoicePitch(it) },
                    valueRange = 0.5f..1.5f
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

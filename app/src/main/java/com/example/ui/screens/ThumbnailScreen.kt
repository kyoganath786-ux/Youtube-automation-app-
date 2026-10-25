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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ThumbnailScreen(
    viewModel: MainViewModel,
    language: AppLanguage
) {
    val isTa = language == AppLanguage.TAMIL
    var headline by remember { mutableStateOf(if (isTa) "அதிர்ச்சி தரும் AI ரகசியங்கள்!" else "THE 99% AI BREAKTHROUGH") }
    var selectedBadge by remember { mutableStateOf(if (isTa) "முக்கிய செய்தி" else "VIRAL") }
    var backgroundTheme by remember { mutableStateOf("Crimson Cyber") }
    var customPrompt by remember { mutableStateOf("") }

    val badges = if (isTa) {
        listOf("முக்கிய செய்தி", "உண்மை இதுதான்", "புதிய AI", "100K பார்வைகள்")
    } else {
        listOf("VIRAL", "MUST WATCH", "EXPOSED", "SHOCKING", "NEW 2026")
    }

    val bgThemes = listOf("Crimson Cyber", "Midnight Space", "Golden Luxury", "Toxic Neon")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 16:9 Thumbnail Canvas Simulator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    when (backgroundTheme) {
                        "Crimson Cyber" -> Brush.radialGradient(listOf(Color(0xFFFF0033), Color(0xFF1E040B)))
                        "Midnight Space" -> Brush.radialGradient(listOf(Color(0xFF0284C7), Color(0xFF030712)))
                        "Golden Luxury" -> Brush.radialGradient(listOf(Color(0xFFF59E0B), Color(0xFF1F1202)))
                        else -> Brush.radialGradient(listOf(Color(0xFF10B981), Color(0xFF021B10)))
                    }
                )
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            // Badge Overlay
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = AmberAccent,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = selectedBadge,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Headline Big High-Contrast Text
            Text(
                text = headline.uppercase(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp)
            )

            // 1280x720 Spec Pill
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
            ) {
                Text(
                    text = "1280x720 HD",
                    fontSize = 9.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Headline Text Input Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTa) "முகப்புப்பட தலைப்பு உரை (Headline Text)" else "Thumbnail Headline Text",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = headline,
                    onValueChange = { headline = it },
                    modifier = Modifier.fillMaxWidth().testTag("thumb_headline_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Badges Selection
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTa) "கவனம் ஈர்க்கும் பேட்ஜ் (Click Magnet Badge)" else "Click Magnet Badges",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    badges.take(3).forEach { badge ->
                        FilterChip(
                            selected = selectedBadge == badge,
                            onClick = { selectedBadge = badge },
                            label = { Text(badge, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }

        // Background Preset Themes
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTa) "பின்னணி வண்ண பாணி" else "Background Palette Theme",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bgThemes.take(3).forEach { theme ->
                        FilterChip(
                            selected = backgroundTheme == theme,
                            onClick = { backgroundTheme = theme },
                            label = { Text(theme, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // Export Thumbnail Button
        Button(
            onClick = {
                viewModel.showStatus(if (isTa) "1280x720 முகப்புப்படம் சேமிக்கப்பட்டது!" else "1280x720 HD Thumbnail exported!")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("thumb_export_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isTa) "தம்பனைலை பதிவிறக்கு (Export Thumbnail)" else "Export 1280x720 Thumbnail",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

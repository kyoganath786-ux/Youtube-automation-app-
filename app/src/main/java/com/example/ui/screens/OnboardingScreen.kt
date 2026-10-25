package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onContinue: () -> Unit
) {
    var selectedLang by remember { mutableStateOf(language) }
    val isTa = selectedLang == AppLanguage.TAMIL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = if (isTa) "ஸ்டுடியோ அறிமுகம்" else "CREATOR STUDIO SETUP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Text(
                text = if (isTa) "YouTube Automation AI-க்கு வரவேற்கிறோம்!" else "Welcome to YouTube Automation AI!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )

            Text(
                text = if (isTa) "வீடியோக்கள், ஷார்ட்ஸ், தம்பனைல் மற்றும் குரல் பதிவுகளை தானாகவே உருவாக்குங்கள்."
                else "Automate high-converting YouTube Long-form videos and Shorts in minutes with cutting-edge AI.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
            )

            // Language Selection Selector Cards
            Text(
                text = if (isTa) "முதன்மை மொழியைத் தேர்ந்தெடுக்கவும்" else "Choose Preferred Language",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LanguageSelectionCard(
                    title = "English",
                    subtitle = "Global Content",
                    isSelected = selectedLang == AppLanguage.ENGLISH,
                    onClick = {
                        selectedLang = AppLanguage.ENGLISH
                        viewModel.setLanguage(AppLanguage.ENGLISH)
                    },
                    modifier = Modifier.weight(1f)
                )

                LanguageSelectionCard(
                    title = "தமிழ்",
                    subtitle = "Tamil Localized",
                    isSelected = selectedLang == AppLanguage.TAMIL,
                    onClick = {
                        selectedLang = AppLanguage.TAMIL
                        viewModel.setLanguage(AppLanguage.TAMIL)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Key Feature Bullets
            FeatureItem(
                icon = Icons.Default.AutoAwesome,
                title = if (isTa) "AI ஸ்கிரிப்ட் & காட்சிகள்" else "AI Script & Storyboard Engine",
                desc = if (isTa) "வைரல் ஹூக், காட்சிகள் மற்றும் குறிச்சொற்களை தமிழ் மற்றும் ஆங்கிலத்தில் உருவாக்குங்கள்."
                else "Generate high-retention hooks and full narration scripts in English & Tamil."
            )

            FeatureItem(
                icon = Icons.Default.GraphicEq,
                title = if (isTa) "இயற்கையான நியூரல் குரல் (TTS)" else "Studio-Grade Neural Narration",
                desc = if (isTa) "வள்ளுவர், செல்வி உட்பட பல தமிழ் மற்றும் சர்வதேச குரல் மாதிரிகள்."
                else "Multi-voice neural synthesis with pitch and speed control."
            )

            FeatureItem(
                icon = Icons.Default.CloudSync,
                title = if (isTa) "பாதுகாப்பான மேகக்கணி ஆட்டோமேஷன்" else "Cloud Render & Secure Scheduling",
                desc = if (isTa) "உங்கள் சாதன பேட்டரியை வீணாக்காமல் மேகக்கணியில் வீடியோ ரெண்டர் செய்யப்படும்."
                else "Server-side video assembly, no heavy battery drain, zero key leaks."
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_continue_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isTa) "தொடங்குங்கள் (Get Started)" else "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LanguageSelectionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, CrimsonPrimary) else null,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                tint = if (isSelected) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AmberAccent,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                ),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

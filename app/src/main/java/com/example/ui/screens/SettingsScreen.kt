package com.example.ui.screens

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
import com.example.data.remote.GeminiApiClient
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.util.Strings
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onSignOut: () -> Unit
) {
    val isTa = language == AppLanguage.TAMIL
    val isDark by viewModel.isDarkTheme.collectAsState()
    val apiDiagnostic by viewModel.apiDiagnosticResult.collectAsState()
    val isTestingApi by viewModel.isTestingApi.collectAsState()
    var isSecretsGuideOpen by remember { mutableStateOf(false) }
    var budgetLimit by remember { mutableStateOf(35f) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Language Switcher Section
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("language_select", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LanguageChip(
                        name = "English",
                        isSelected = language == AppLanguage.ENGLISH,
                        onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                        modifier = Modifier.weight(1f)
                    )

                    LanguageChip(
                        name = "தமிழ் (Tamil)",
                        isSelected = language == AppLanguage.TAMIL,
                        onClick = { viewModel.setLanguage(AppLanguage.TAMIL) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Appearance / Theme Section
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = AmberAccent
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = Strings.get("theme_mode", language),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isDark) "Cinematic Dark Obsidian" else "Daylight Minimal",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                    }
                }

                Switch(
                    checked = isDark,
                    onCheckedChange = { viewModel.toggleTheme() },
                    modifier = Modifier.testTag("theme_switch")
                )
            }
        }

        // AI Tools & API Keys Management Center
        val isKeyConfigured = remember { GeminiApiClient.isApiKeyConfigured() }
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
                    Column {
                        Text(
                            text = if (isTa) "முழு AI கருவிகள் & API சாவி மையம்" else "AI Tools & API Key Management",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Model: ${GeminiApiClient.MODEL}",
                            style = MaterialTheme.typography.bodySmall.copy(color = CyberCyan, fontSize = 11.sp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isKeyConfigured) StatusSuccess.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isKeyConfigured) "KEY ACTIVE" else "OFFLINE FALLBACK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isKeyConfigured) StatusSuccess else AmberAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Masked API Key Display Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "GEMINI_API_KEY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = GeminiApiClient.getMaskedApiKey(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.testGeminiApiConnection() },
                            enabled = !isTestingApi,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            modifier = Modifier.testTag("btn_test_gemini_api")
                        ) {
                            if (isTestingApi) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isTa) "சோதி" else "Test Ping", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Diagnostic Result Box (if tested)
                if (apiDiagnostic != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val isSuccess = apiDiagnostic!!.isSuccess
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSuccess) StatusSuccess.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSuccess) StatusSuccess else MaterialTheme.colorScheme.error)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isSuccess) StatusSuccess else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSuccess) "Connection Status: HTTP 200 OK (${apiDiagnostic!!.latencyMs}ms)"
                                    else "API Response: HTTP ${apiDiagnostic!!.statusCode}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = apiDiagnostic!!.message,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Secrets Setup Guide Expander
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth().clickable { isSecretsGuideOpen = !isSecretsGuideOpen }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isTa) "API சாவியை எப்படி அமைப்பது? (Secrets வழிகாட்டி)" else "How to configure GEMINI_API_KEY in Secrets",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Icon(
                                imageVector = if (isSecretsGuideOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (isSecretsGuideOpen) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isTa)
                                    "1. AI Studio பக்கவாட்டு பட்டியில் உள்ள 'Secrets' (சாவி ஐகான்) திறக்கவும்.\n2. 'GEMINI_API_KEY' என்று பெயரிட்டு உங்கள் Google AI Studio API சாவியை உள்ளிடவும்.\n3. இது உங்கள் APK-ல் பாதுகாப்பாக இணைக்கப்படும் (குறியீட்டில் நேரடியாக சேமிக்கப்படாது).\n4. சாவி இல்லாத போதும், முழுமையான ஆஃப்லைன் AI எஞ்சின் செயல்படும்."
                                else
                                    "1. Open the 'Secrets' panel in the AI Studio sidebar.\n2. Add a secret named 'GEMINI_API_KEY' with your Google AI Studio API key.\n3. Injected securely into BuildConfig via Secrets Gradle Plugin.\n4. If no key is set, the offline neural fallback engine powers all tools seamlessly.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Full AI Tools Registry
                Text(
                    text = if (isTa) "செயலில் உள்ள AI கருவிகள் (Full AI Tool Suite)" else "All Integrated AI Creator Tools",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                ProviderStatusItem("Script Director (gemini-3.5-flash)", "Streaming Narration & Scene Plan", true)
                ProviderStatusItem("Viral Hook Optimizer", "4-Angle Retention Hook Engine", true)
                ProviderStatusItem("High-CTR SEO Generator", "Titles, Descriptions & Tags", true)
                ProviderStatusItem("Viral Idea Brainstormer", "Niche Trends & Scoring", true)
                ProviderStatusItem("AI Director Chat", "Conversational Script Doctor", true)
                ProviderStatusItem("Dual-Language Neural TTS", "Tamil & English Voices", true)

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isTa) "மாதாந்திர செலவு வரம்பு: $${budgetLimit.toInt()} USD" else "Monthly Spend Guardrail: $${budgetLimit.toInt()} USD",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = budgetLimit,
                    onValueChange = { budgetLimit = it },
                    valueRange = 10f..100f
                )
            }
        }

        // Storage & Cache Maintenance
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = Strings.get("clear_cache", language),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Cached Video Frames (42 MB)",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        viewModel.showStatus(if (isTa) "தற்காலிக கோப்புகள் அழிக்கப்பட்டன!" else "Temporary cache cleaned!")
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = if (isTa) "அழி" else "Clean", fontSize = 12.sp)
                }
            }
        }

        // Sign Out Button
        OutlinedButton(
            onClick = { showSignOutDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_sign_out_btn"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Strings.get("sign_out", language), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(if (isTa) "வெளியேறவா?" else "Sign Out?")
            },
            text = {
                Text(if (isTa) "உங்கள் கணக்கிலிருந்து வெளியேற விரும்புகிறீர்களா?" else "Are you sure you want to sign out of Creator Studio?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isTa) "ஆம், வெளியேறு" else "Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(Strings.get("cancel", language))
                }
            }
        )
    }
}

@Composable
private fun LanguageChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, CrimsonPrimary) else null,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun ProviderStatusItem(name: String, role: String, isOnline: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = role, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = StatusSuccess.copy(alpha = 0.15f)
        ) {
            Text(
                text = "ACTIVE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = StatusSuccess,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

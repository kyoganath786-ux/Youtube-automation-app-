package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.util.Strings
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun EditorScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onNavigateToPublish: () -> Unit
) {
    val isTa = language == AppLanguage.TAMIL
    val activeProject by viewModel.activeProject.collectAsState()
    val isRendering by viewModel.isRendering.collectAsState()
    val renderProgress by viewModel.renderProgress.collectAsState()

    if (activeProject == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (isTa) "திட்டம் தேர்ந்தெடுக்கப்படவில்லை" else "No project selected")
        }
        return
    }

    val project = activeProject!!
    val context = LocalContext.current
    val viralHooks by viewModel.viralHooks.collectAsState()
    val isGeneratingHooks by viewModel.isGeneratingHooks.collectAsState()
    val seoMetadata by viewModel.seoMetadata.collectAsState()
    val isGeneratingSeo by viewModel.isGeneratingSeo.collectAsState()
    var showHookDialog by remember { mutableStateOf(false) }
    var showSeoDialog by remember { mutableStateOf(false) }

    var isPlaying by remember { mutableStateOf(false) }
    var currentSceneIndex by remember { mutableStateOf(0) }

    LaunchedEffect(isPlaying) {
        if (isPlaying && project.scenes.isNotEmpty()) {
            while (isPlaying) {
                delay(2000)
                currentSceneIndex = (currentSceneIndex + 1) % project.scenes.size
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Video Preview Player Canvas
        item {
            val isShorts = project.format == VideoFormat.SHORTS
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isShorts) 280.dp else 200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF2B0A14),
                                    Color(0xFF0F111A)
                                )
                            )
                        )
                ) {
                    val activeScene = project.scenes.getOrNull(currentSceneIndex)
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CrimsonPrimary.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "Scene ${currentSceneIndex + 1}/${project.scenes.size} • ${project.format.aspectRatio}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = activeScene?.narration ?: project.hook,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Prompt: ${activeScene?.visualPrompt ?: "Cinematic visual"}",
                            fontSize = 11.sp,
                            color = AmberLight,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 2
                        )
                    }

                    // Playback Overlay Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                isPlaying = !isPlaying
                                if (isPlaying) {
                                    val textToRead = activeScene?.narration ?: project.hook
                                    viewModel.testNarrationPlayback(textToRead)
                                } else {
                                    viewModel.stopNarration()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "${project.durationSeconds}s Total • ${project.voiceName}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        IconButton(
                            onClick = {
                                currentSceneIndex = (currentSceneIndex + 1) % (project.scenes.size.coerceAtLeast(1))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next Scene", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Real Render Progress Bar (if rendering)
        if (isRendering) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isTa) "உண்மையான MP4 வீடியோ ரெண்டர் ஆகிறது ($renderProgress%)..." else "Rendering Real MP4 Video ($renderProgress%)...",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = project.format.resolution,
                                style = MaterialTheme.typography.labelSmall.copy(color = CyberCyan)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { renderProgress / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = CrimsonPrimary
                        )
                    }
                }
            }
        }

        // AI Creator Studio Quick Tools Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        if (viralHooks.isEmpty()) {
                            viewModel.generateViralHooksForTopic(project.topic)
                        }
                        showHookDialog = true
                    },
                    modifier = Modifier.weight(1f).testTag("btn_ai_viral_hooks"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isGeneratingHooks) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isTa) "AI வைரல் ஹூக்ஸ்" else "AI Viral Hooks", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = {
                        if (seoMetadata == null) {
                            viewModel.generateSeoForActiveProject()
                        }
                        showSeoDialog = true
                    },
                    modifier = Modifier.weight(1f).testTag("btn_ai_seo_metadata"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isGeneratingSeo) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isTa) "AI SEO தலைப்புகள்" else "AI SEO & Titles", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Action Buttons Row: Add Scene, Render Video
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val newScene = SceneItem(
                            id = "s_" + System.currentTimeMillis(),
                            order = project.scenes.size + 1,
                            narration = if (isTa) "கூடுதல் புதிய காட்சி மற்றும் தகவல்..." else "Additional scene insights...",
                            visualPrompt = "Dynamic high-tech visual frame",
                            durationSeconds = 4
                        )
                        viewModel.updateActiveProject(project.copy(scenes = project.scenes + newScene))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isTa) "காட்சி சேர்" else "Add Scene", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        viewModel.renderProject(project) {
                            onNavigateToPublish()
                        }
                    },
                    enabled = !isRendering,
                    modifier = Modifier.weight(1f).testTag("editor_render_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.MovieCreation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isTa) "ரெண்டர் செய்" else "Render Video", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Verified Research Sources Section (if present)
        if (project.sources.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTa) "சரிபார்க்கப்பட்ட ஆதாரங்கள் (${project.sources.size})" else "Verified Fact-Checking Sources (${project.sources.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        project.sources.forEach { src ->
                            Text(
                                text = "• ${src.title}: ${src.snippet}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }

        // Scene Storyboard List Header
        item {
            Text(
                text = if (isTa) "காட்சிகள் வரிசை (Storyboard Scenes)" else "Scene Storyboard & Narration",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Editable Scene Cards
        itemsIndexed(project.scenes) { index, scene ->
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scene ${index + 1} (${scene.durationSeconds}s)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    val updatedScenes = project.scenes.filter { it.id != scene.id }
                                    viewModel.updateActiveProject(project.copy(scenes = updatedScenes))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Scene", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = scene.narration,
                        onValueChange = { newText ->
                            val updatedScenes = project.scenes.toMutableList()
                            updatedScenes[index] = scene.copy(narration = newText)
                            viewModel.updateActiveProject(project.copy(scenes = updatedScenes))
                        },
                        label = { Text(if (isTa) "குரல் உரை (Narration Text)" else "Narration Text") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = scene.visualPrompt,
                        onValueChange = { newPrompt ->
                            val updatedScenes = project.scenes.toMutableList()
                            updatedScenes[index] = scene.copy(visualPrompt = newPrompt)
                            viewModel.updateActiveProject(project.copy(scenes = updatedScenes))
                        },
                        label = { Text(if (isTa) "காட்சி AI வரியில் குறிப்பு (Visual Prompt)" else "Visual Generation Prompt") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }
        }
    }

    // AI Viral Hooks Selection Dialog
    if (showHookDialog) {
        AlertDialog(
            onDismissRequest = { showHookDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = AmberAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTa) "AI வைரல் ஹூக் தேர்வு" else "AI Viral Hook Optimizer")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isGeneratingHooks) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = CrimsonPrimary)
                        }
                    } else {
                        viralHooks.forEach { hook ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    viewModel.applyHookToActiveProject(hook.hookText)
                                    showHookDialog = false
                                }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = hook.type, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AmberAccent)
                                        Text(text = hook.retentionImpact, fontSize = 11.sp, color = StatusSuccess)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = hook.hookText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.generateViralHooksForTopic(project.topic) },
                    enabled = !isGeneratingHooks,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text(if (isTa) "மறு உருவாக்கம்" else "Regenerate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHookDialog = false }) {
                    Text(Strings.get("cancel", language))
                }
            }
        )
    }

    // AI SEO Metadata Dialog
    if (showSeoDialog) {
        AlertDialog(
            onDismissRequest = { showSeoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTa) "AI YouTube SEO தொகுப்பு" else "AI YouTube SEO Suite")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isGeneratingSeo) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = CrimsonPrimary)
                        }
                    } else if (seoMetadata != null) {
                        Text(
                            text = if (isTa) "உயர்தர CTR தலைப்புகள் (தட்டவும்):" else "High-CTR Titles (Tap to apply):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        seoMetadata!!.titles.forEach { title ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    viewModel.applySeoToActiveProject(title, seoMetadata!!.description)
                                    showSeoDialog = false
                                }
                            ) {
                                Text(
                                    text = "• $title",
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SEO Tags: ${seoMetadata!!.tags.take(5).joinToString(", ")}...",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Tags", seoMetadata!!.tags.joinToString(", ")))
                                    viewModel.showStatus("Tags copied!")
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Tags", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.generateSeoForActiveProject() },
                    enabled = !isGeneratingSeo,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text(if (isTa) "மறு உருவாக்கம்" else "Regenerate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeoDialog = false }) {
                    Text(Strings.get("cancel", language))
                }
            }
        )
    }
}

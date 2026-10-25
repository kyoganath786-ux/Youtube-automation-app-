package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ProjectItem
import com.example.data.model.ProjectStatus
import com.example.data.model.VideoFormat
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.util.Strings
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProjectsScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onOpenProject: () -> Unit,
    onCreateNew: () -> Unit
) {
    val isTa = language == AppLanguage.TAMIL
    val projects by viewModel.projects.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "Shorts", "Standard", "Ready", "Draft")

    val filteredProjects = projects.filter { proj ->
        val matchesSearch = proj.title.contains(searchQuery, ignoreCase = true) ||
                proj.topic.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Shorts" -> proj.format == VideoFormat.SHORTS
            "Standard" -> proj.format == VideoFormat.STANDARD
            "Ready" -> proj.status == ProjectStatus.READY
            "Draft" -> proj.status == ProjectStatus.DRAFT
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNew,
                containerColor = CrimsonPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 60.dp).testTag("projects_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Project")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isTa) "திட்டங்களைத் தேடுங்கள்..." else "Search projects by title or topic...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("projects_search_bar"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { opt ->
                    FilterChip(
                        selected = selectedFilter == opt,
                        onClick = { selectedFilter = opt },
                        label = { Text(opt, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Projects List
            if (filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isTa) "பொருத்தமான திட்டங்கள் எதுவும் இல்லை" else "No matching projects found",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(filteredProjects, key = { it.id }) { project ->
                        ProjectItemRow(
                            project = project,
                            language = language,
                            onOpen = {
                                viewModel.setActiveProject(project)
                                onOpenProject()
                            },
                            onDuplicate = { viewModel.duplicateProject(project) },
                            onDelete = { viewModel.deleteProject(project.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectItemRow(
    project: ProjectItem,
    language: AppLanguage,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val isTa = language == AppLanguage.TAMIL
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpen() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (project.format == VideoFormat.SHORTS) CrimsonPrimary.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (project.format == VideoFormat.SHORTS) Icons.Default.PhoneAndroid else Icons.Default.Tv,
                            contentDescription = null,
                            tint = if (project.format == VideoFormat.SHORTS) CrimsonPrimary else CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${project.format.displayName} • ${project.durationSeconds}s • ${project.language.displayName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (project.status) {
                        ProjectStatus.READY -> StatusSuccess.copy(alpha = 0.15f)
                        ProjectStatus.RENDERING -> AmberAccent.copy(alpha = 0.15f)
                        ProjectStatus.UPLOADED -> CyberCyan.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ) {
                    Text(
                        text = if (isTa) project.status.labelTa else project.status.labelEn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (project.status) {
                            ProjectStatus.READY -> StatusSuccess
                            ProjectStatus.RENDERING -> AmberAccent
                            ProjectStatus.UPLOADED -> CyberCyan
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDuplicate) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = Strings.get("duplicate", language), fontSize = 12.sp)
                }

                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = Strings.get("delete", language), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = onOpen,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = Strings.get("edit", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

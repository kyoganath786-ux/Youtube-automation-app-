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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.util.YouTubeAppHelper
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    language: AppLanguage
) {
    val context = LocalContext.current
    val isTa = language == AppLanguage.TAMIL

    val channelOverview by viewModel.channelOverview.collectAsState()
    val detailedVideos by viewModel.detailedVideos.collectAsState()
    val analystReport by viewModel.analystReport.collectAsState()
    val ideas by viewModel.dataBackedIdeas.collectAsState()
    val titleAudit by viewModel.titleThumbnailAudit.collectAsState()
    val competitor by viewModel.competitorAnalysis.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingChannel.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = if (isTa) listOf(
        "மேலோட்டம்",
        "AI தணிக்கை",
        "10 வைரல் யோசனைகள்",
        "தலைப்பு & தம்ப்னைல்",
        "போட்டியாளர் ஆய்வு"
    ) else listOf(
        "Overview",
        "AI Audit",
        "10 Viral Ideas",
        "Title & Thumbnail",
        "Competitor Radar"
    )

    var selectedDateRange by remember { mutableStateOf("28 Days") }
    val dateRanges = listOf("7 Days", "28 Days", "90 Days", "365 Days")

    var customTitleInput by remember { mutableStateOf("How to Automate YouTube Content Creation with AI") }
    var competitorSearchInput by remember { mutableStateOf("@mkbhd") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Banner
        Surface(
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTa) "YouTube சேனல் ஆய்வாளர் AI" else "YouTube Channel Analyst AI",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = if (isTa) "Gemini 3.5 Flash & YouTube API ஒருங்கிணைந்த தணிக்கை"
                            else "Stage 5: Live Metrics, Growth Audit & Algorithmic Strategy",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                    }

                    // Deep-link to Official YouTube Studio Analytics
                    IconButton(
                        onClick = { YouTubeAppHelper.openYouTubeStudioAnalytics(context) },
                        modifier = Modifier.testTag("launch_yt_studio_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Studio",
                            tint = CyberCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Sub-Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = CrimsonPrimary
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.testTag("analytics_tab_$index")
                        )
                    }
                }
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> OverviewTab(
                    overview = channelOverview,
                    videos = detailedVideos,
                    dateRanges = dateRanges,
                    selectedRange = selectedDateRange,
                    onRangeSelected = {
                        selectedDateRange = it
                        viewModel.refreshChannelAnalytics(it)
                    },
                    isTa = isTa,
                    onOpenStudio = { YouTubeAppHelper.openYouTubeStudioAnalytics(context) }
                )
                1 -> AuditTab(
                    report = analystReport,
                    isAnalyzing = isAnalyzing,
                    onRunAudit = { viewModel.runAiChannelAudit() },
                    isTa = isTa
                )
                2 -> IdeasTab(
                    ideas = ideas,
                    isAnalyzing = isAnalyzing,
                    onGenerate = { viewModel.generate10DataBackedIdeas() },
                    onUseIdea = { viewModel.useIdeaInEditor(it) },
                    isTa = isTa
                )
                3 -> TitleOptimizerTab(
                    titleInput = customTitleInput,
                    onTitleChange = { customTitleInput = it },
                    auditResult = titleAudit,
                    isAnalyzing = isAnalyzing,
                    onAudit = { viewModel.auditTitleAndThumbnail(customTitleInput) },
                    isTa = isTa
                )
                4 -> CompetitorTab(
                    searchQuery = competitorSearchInput,
                    onQueryChange = { competitorSearchInput = it },
                    competitor = competitor,
                    isAnalyzing = isAnalyzing,
                    onSearch = { viewModel.searchCompetitor(competitorSearchInput) },
                    isTa = isTa
                )
            }
        }
    }
}

@Composable
private fun OverviewTab(
    overview: ChannelMetricsOverview,
    videos: List<DetailedVideoAnalytics>,
    dateRanges: List<String>,
    selectedRange: String,
    onRangeSelected: (String) -> Unit,
    isTa: Boolean,
    onOpenStudio: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Range Selector Chips & Status Badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dateRanges) { range ->
                        FilterChip(
                            selected = selectedRange == range,
                            onClick = { onRangeSelected(range) },
                            label = { Text(range, fontSize = 11.sp) }
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (overview.isVerifiedLiveApi) StatusSuccess.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (overview.isVerifiedLiveApi) "LIVE OAUTH SYNC" else "MOBILE ASSISTANCE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (overview.isVerifiedLiveApi) StatusSuccess else AmberAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Channel KPI Metric Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = if (isTa) "பார்வைகள் (Views)" else "Total Views",
                        value = "${String.format(Locale.US, "%.1f", overview.viewCount / 1000000.0)}M",
                        trend = "+28.4%",
                        color = CrimsonPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = if (isTa) "பார்வை நேரம்" else "Watch Time (Hours)",
                        value = "${overview.watchTimeHours.toInt()}h",
                        trend = "+19.2%",
                        color = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = if (isTa) "சப்ஸ்கிரைபர்கள்" else "Subscribers Gained",
                        value = "+${overview.subscribersGained}",
                        trend = "Healthy",
                        color = AmberAccent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = if (isTa) "கிளிக் விகிதம் (CTR)" else "Impression CTR",
                        value = "${overview.impressionCtrPercent}%",
                        trend = "+1.4%",
                        color = StatusSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Official YouTube Studio Launch Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenStudio() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CrimsonPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isTa) "YouTube Studio செயலியில் திறக்கவும்" else "Open in Official YouTube Studio",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isTa) "நேரடி வருவாய், கமெண்ட்கள் மற்றும் பார்வையாளர் வரைபடங்கள்"
                                else "Inspect deep subscriber retention curves and live RPM in official app",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Traffic Sources Breakdown
        item {
            Text(
                text = if (isTa) "பார்வையாளர்கள் வரும் வழிகள் (Traffic Sources)" else "Traffic Sources Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(overview.trafficSources) { src ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(src.sourceName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("${src.percentage}% (${src.views / 1000}K views)", fontSize = 12.sp, color = CyberCyan)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (src.percentage / 100.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (src.sourceName.contains("Shorts")) CrimsonPrimary else CyberCyan,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        // Video Retention & Health Inspector
        item {
            Text(
                text = if (isTa) "சமீபத்திய வீடியோக்களின் செயல்திறன்" else "Recent Video Performance & Diagnosis",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(videos) { vid ->
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = vid.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Text(
                                text = "${vid.views / 1000}K views • ${vid.likes / 1000}K likes • CTR ${vid.ctrPercent}%",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (vid.score >= 85) StatusSuccess.copy(alpha = 0.2f) else AmberAccent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Score ${vid.score}/100",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (vid.score >= 85) StatusSuccess else AmberAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 ${vid.diagnosis}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun AuditTab(
    report: ChannelAnalystReport?,
    isAnalyzing: Boolean,
    onRunAudit: () -> Unit,
    isTa: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Button(
                onClick = onRunAudit,
                enabled = !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_run_audit_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTa) "Gemini 3.5 Flash ஆய்வு செய்கிறது..." else "Gemini 3.5 Flash Analyzing...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTa) "முழு சேனல் தணிக்கையை தொடங்கு (Gemini 3.5)" else "Run Deep Channel Audit (Gemini 3.5)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (report == null) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTa) "மேலே உள்ள பொத்தானை அழுத்தி முழுமையான AI தணிக்கை அறிக்கையை உருவாக்கவும்."
                            else "Tap above to generate a comprehensive algorithmic audit with confidence scores and growth opportunities.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            }
        } else {
            // Overall Grade Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(CrimsonPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = report.overallGrade,
                                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (isTa) "சேனல் ஒட்டுமொத்த தரம்" else "Overall Channel Health Grade",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = report.executiveSummary,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }

            // What's Working & What Needs Improvement
            item {
                Text(text = if (isTa) "வெற்றிகரமான அம்சங்கள்" else "What is Working Well", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            items(report.whatIsWorking) { point ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(point, fontSize = 12.sp)
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = if (isTa) "மேம்படுத்த வேண்டியவை" else "What Needs Immediate Improvement", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            items(report.whatNeedsImprovement) { point ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(point, fontSize = 12.sp)
                }
            }

            // Growth Opportunities with Confidence
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = if (isTa) "வளர்ச்சி வாய்ப்புகள் (Confidence %)" else "Top Growth Opportunities", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            items(report.growthOpportunities) { opp ->
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
                            Text(opp.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CrimsonPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${opp.confidencePercent}% Confidence",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(opp.impactSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Action: ${opp.recommendedAction}\nPotential Gain: ${opp.estimatedViewGain}",
                                fontSize = 11.sp,
                                color = CyberCyan,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdeasTab(
    ideas: List<PersonalizedContentIdea>,
    isAnalyzing: Boolean,
    onGenerate: () -> Unit,
    onUseIdea: (PersonalizedContentIdea) -> Unit,
    isTa: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Button(
                onClick = onGenerate,
                enabled = !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_generate_ideas_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTa) "10 புதிய வைரல் தலைப்புகளை உருவாக்கு" else "Generate 10 Personalized Viral Ideas",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        items(ideas) { idea ->
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
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (idea.format == VideoFormat.SHORTS) CrimsonPrimary.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = idea.format.displayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (idea.format == VideoFormat.SHORTS) CrimsonPrimary else CyberCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "${idea.predictedPerformance} • ${idea.difficulty}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = idea.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hook: \"${idea.hook}\"",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        idea.outline.forEachIndexed { idx, point ->
                            Text("• Scene ${idx + 1}: $point", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onUseIdea(idea) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("use_idea_${idea.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.MovieCreation, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTa) "இந்த யோசனையை எடிட்டரில் தொடங்கு" else "Create This Video in Editor",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleOptimizerTab(
    titleInput: String,
    onTitleChange: (String) -> Unit,
    auditResult: TitleThumbnailAudit?,
    isAnalyzing: Boolean,
    onAudit: () -> Unit,
    isTa: Boolean
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                label = { Text(if (isTa) "வீடியோ தலைப்பை உள்ளிடவும்" else "Enter Video Title to Audit") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audit_title_input"),
                trailingIcon = {
                    IconButton(onClick = onAudit, enabled = !isAnalyzing) {
                        Icon(Icons.Default.Check, contentDescription = "Audit", tint = CrimsonPrimary)
                    }
                }
            )
        }

        item {
            Button(
                onClick = onAudit,
                enabled = !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audit_title_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isTa) "CTR மதிப்பெண் மற்றும் தம்ப்னைல் ஆய்வு செய்" else "Analyze Clickability Score (AI)")
            }
        }

        if (auditResult != null) {
            item {
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
                                text = if (isTa) "கிளிக் திறன் மதிப்பெண் (Score)" else "Clickability Score",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (auditResult.clickabilityScore >= 75) StatusSuccess.copy(alpha = 0.2f) else AmberAccent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${auditResult.clickabilityScore} / 100",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (auditResult.clickabilityScore >= 75) StatusSuccess else AmberAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(auditResult.critique, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Text(
                    text = if (isTa) "பரிந்துரைக்கப்படும் உயர் CTR தலைப்புகள்" else "High-Converting Title Alternatives",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(auditResult.suggestedTitles) { suggested ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(suggested, modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(
                            onClick = { YouTubeAppHelper.copyToClipboard(context, "Title", suggested) }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            item {
                Text(
                    text = if (isTa) "தம்ப்னைல் வடிவமைப்பு விதிகள்" else "Thumbnail Visual Hierarchy Rules",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(auditResult.thumbnailVisualTips) { tip ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Brush, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tip, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CompetitorTab(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    competitor: CompetitorAnalysis?,
    isAnalyzing: Boolean,
    onSearch: () -> Unit,
    isTa: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                label = { Text(if (isTa) "போட்டியாளர் சேனல் பெயர் (@handle)" else "Competitor Channel Handle (@handle)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("competitor_search_input"),
                trailingIcon = {
                    IconButton(onClick = onSearch, enabled = !isAnalyzing) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberCyan)
                    }
                }
            )
        }

        item {
            Button(
                onClick = onSearch,
                enabled = !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("competitor_search_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isTa) "போட்டியாளர் இடைவெளிகளை கண்டறி (Audit)" else "Inspect Competitor & Find Content Gaps",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (competitor != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AmberAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(competitor.channelTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("${competitor.channelHandle} • ${competitor.subscribers / 1000}K Subscribers • ${competitor.videoCount} Videos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isTa) "அவர்கள் வெற்றிபெறும் தலைப்புகள்" else "Their Top Winning Topics",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        competitor.topWinningTopics.forEach { topic ->
                            Text("• $topic", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTa) "அவர்கள் தவறவிடும் வாய்ப்புகள் (Content Gaps)" else "Identified Channel Gaps You Can Dominate",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CrimsonPrimary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        competitor.contentGaps.forEach { gap ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(gap, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    trend: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = color))
            Spacer(modifier = Modifier.height(4.dp))
            Text(trend, style = MaterialTheme.typography.labelSmall.copy(color = StatusSuccess, fontWeight = FontWeight.Bold, fontSize = 10.sp))
        }
    }
}

package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.example.data.SampleMediaData
import com.example.model.*
import com.example.ui.components.ExportDialog
import com.example.ui.components.TimelineView
import com.example.ui.components.VideoPlayerCard
import com.example.ui.theme.*
import com.example.viewmodel.EditorTab
import com.example.viewmodel.ShortyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: ShortyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Hardware/Gesture back navigation handler
    BackHandler {
        onNavigateBack()
    }

    val project by viewModel.currentProject.collectAsState()
    val playbackPosition by viewModel.playbackPositionSec.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationStage by viewModel.generationStage.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val exportPipelineStage by viewModel.exportPipelineStage.collectAsState()
    val exportSuccess by viewModel.exportSuccess.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var promptInputText by remember { mutableStateOf("") }

    val verticalScroll = rememberScrollState()

    if (project == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No active edit project", color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onNavigateBack) {
                    Text("Select Raw Footage")
                }
            }
        }
        return
    }

    val currentProject = project!!

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentProject.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${currentProject.style.name} • ${currentProject.aspectRatio.label}",
                                fontSize = 11.sp,
                                color = NeonCyanLight
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "FREE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("export_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                    ) {
                        Icon(Icons.Default.Download, "Export", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export MP4", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(verticalScroll)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Primary Video Player
            VideoPlayerCard(
                project = currentProject,
                playbackPositionSec = playbackPosition,
                isPlaying = isPlaying,
                playbackSpeed = playbackSpeed,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                onRatioChange = { viewModel.updateAspectRatio(it) }
            )

            // 2. Editor Studio Tab Navigation Bar
            ScrollableTabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = StudioSurfaceElevated,
                contentColor = NeonCyan,
                edgePadding = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                EditorTab.entries.forEach { tab ->
                    val isSelected = activeTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.setActiveTab(tab) },
                        text = {
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NeonCyan else TextSecondary
                            )
                        }
                    )
                }
            }

            // 3. Tab Content View
            when (activeTab) {
                EditorTab.STUDIO -> {
                    // Studio Quick Overview & AI Prompt Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, "AI", tint = ElectricVioletLight, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("AI Prompt Assistant (Free Revisions)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                }
                                Text("Shorty Chat", fontSize = 11.sp, color = NeonCyan)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Request changes to your cut! Type anything like 'make cuts faster' or 'change captions to yellow'.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // One-tap quick revisions chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SampleMediaData.PresetPrompts.forEach { prompt ->
                                    SuggestionChip(
                                        onClick = { viewModel.submitAiPromptRevision(prompt) },
                                        label = { Text(prompt, fontSize = 10.sp) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = StudioSurface,
                                            labelColor = TextPrimary
                                        ),
                                        border = BorderStroke(1.dp, StudioSurfaceBorder)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = promptInputText,
                                    onValueChange = { promptInputText = it },
                                    placeholder = { Text("E.g. Cut pauses, speed up to 15s...", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElectricViolet,
                                        unfocusedBorderColor = StudioSurfaceBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (promptInputText.isNotBlank()) {
                                            viewModel.submitAiPromptRevision(promptInputText)
                                            promptInputText = ""
                                        }
                                    },
                                    modifier = Modifier.height(52.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                                ) {
                                    Icon(Icons.Default.Send, "Send", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Mini Multi-track Timeline overview
                    TimelineView(
                        project = currentProject,
                        playbackPositionSec = playbackPosition,
                        onSeek = { viewModel.seekTo(it) }
                    )
                }

                EditorTab.TIMELINE -> {
                    // Full detailed timeline view
                    TimelineView(
                        project = currentProject,
                        playbackPositionSec = playbackPosition,
                        onSeek = { viewModel.seekTo(it) }
                    )

                    // Cuts breakdown list
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Shot Breakdown (${currentProject.cuts.size} clips)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            currentProject.cuts.forEachIndexed { idx, cut ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.seekTo(cut.timelineStartSec) }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (cut.isHook) CyberPink.copy(alpha = 0.2f) else StudioSurfaceElevated
                                        ) {
                                            Text(
                                                text = "#${idx + 1}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (cut.isHook) CyberPink else NeonCyan,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = cut.label,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${(cut.durationSec * 10).toInt() / 10f}s • Zoom ${cut.zoomFactor}x",
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "Jump",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (idx < currentProject.cuts.size - 1) {
                                    HorizontalDivider(color = StudioSurfaceBorder, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                EditorTab.AI_PROMPT -> {
                    // Revisions History and Chat Log
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "AI Revision History",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            currentProject.revisionHistory.forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Default.Check, "Log", tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = log,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                EditorTab.STYLE -> {
                    // Caption Settings, Font Styles, Presets & Color LUT Selection
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Master Captions Toggle Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    if (currentProject.enableCaptions) listOf(AmberVibrant, ElectricViolet)
                                    else listOf(StudioSurfaceBorder, StudioSurfaceBorder)
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Subtitles,
                                        contentDescription = "Captions",
                                        tint = if (currentProject.enableCaptions) AmberVibrant else TextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Automatic Video Captions",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = if (currentProject.enableCaptions) "Enabled • Animated subtitles baked into export" else "Disabled • Video exported clean without subtitles",
                                            fontSize = 11.sp,
                                            color = if (currentProject.enableCaptions) AmberVibrant else TextSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = currentProject.enableCaptions,
                                    onCheckedChange = { viewModel.toggleCaptionsEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AmberVibrant
                                    ),
                                    modifier = Modifier.testTag("style_tab_captions_switch")
                                )
                            }
                        }

                        if (currentProject.enableCaptions) {
                            Text(
                                text = "Caption Font Style",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            CaptionFontStyle.entries.forEach { fontStyle ->
                                val isSelected = currentProject.captionFontStyle == fontStyle
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.updateCaptionFontStyle(fontStyle) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) StudioSurfaceElevated else StudioSurface
                                    ),
                                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.horizontalGradient(listOf(AmberVibrant, ElectricViolet))
                                    ) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = fontStyle.displayName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) AmberVibrant else TextPrimary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color.Black.copy(alpha = 0.5f)
                                                ) {
                                                    Text(
                                                        text = fontStyle.sampleText,
                                                        fontSize = 10.sp,
                                                        color = AmberVibrant,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = fontStyle.description,
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }

                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.updateCaptionFontStyle(fontStyle) },
                                            colors = RadioButtonDefaults.colors(selectedColor = AmberVibrant)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Animated Captions Color & Theme",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            CaptionPreset.entries.forEach { preset ->
                                val isSelected = currentProject.style.captionPreset == preset
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.updateCaptionPreset(preset) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) StudioSurfaceElevated else StudioSurface
                                    ),
                                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet))
                                    ) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = preset.displayName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) NeonCyanLight else TextPrimary
                                            )
                                            Text(
                                                text = "${preset.fontStyle} • Word-by-word active highlight",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }

                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.updateCaptionPreset(preset) },
                                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Color Grading LUTs",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        ColorGradePreset.entries.forEach { grade ->
                            val isSelected = currentProject.colorGrade == grade
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateColorGrade(grade) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) StudioSurfaceElevated else StudioSurface
                                ),
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(ElectricViolet, CyberPink))
                                ) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = grade.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) ElectricVioletLight else TextPrimary
                                        )
                                        Text(
                                            text = grade.description,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.updateColorGrade(grade) },
                                        colors = RadioButtonDefaults.colors(selectedColor = ElectricViolet)
                                    )
                                }
                            }
                        }
                    }
                }

                EditorTab.EFFECTS -> {
                    // FX, Ready-To-Use Free Creator Tools, Transitions & Animated Stickers
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_effects_tab"),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header Banner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.6f), ElectricViolet.copy(alpha = 0.6f)))
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "FX & Tools",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ready-To-Use Creator Tools",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TextPrimary
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = EmeraldSuccess.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "100% FREE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldSuccess,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "One-tap tools and visual assets to skyrocket retention, dynamic pacing, cinematic effects and sound presence.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // 1. Ready-To-Use Free Creator Tools
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🛠️ One-Tap Viral Enhancement Tools",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ready to use immediately without complex timelines or plugins:",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                SampleMediaData.FreeCreatorTools.forEach { tool ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = StudioSurface,
                                        border = BorderStroke(1.dp, StudioSurfaceBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = tool.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = ElectricViolet.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = tool.category,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NeonCyanLight,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = tool.description,
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { viewModel.applyCreatorToolPreset(tool.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(36.dp)
                                                    .testTag("apply_tool_${tool.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bolt,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Apply ${tool.title}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Visual FX & Overlays
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✨ Cinematic Visual FX Overlays",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NeonCyan.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = currentProject?.activeEffect?.displayName ?: "Clean Raw",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyanLight,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Real-time rendering effects applied live across all video clips:",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                VisualEffectPreset.entries.forEach { effect ->
                                    val isSelected = currentProject?.activeEffect == effect
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { viewModel.updateVisualEffect(effect) }
                                            .testTag("effect_${effect.name}"),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) NeonCyan.copy(alpha = 0.12f) else StudioSurface,
                                        border = BorderStroke(1.dp, if (isSelected) NeonCyan else StudioSurfaceBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (isSelected) NeonCyan else StudioSurfaceBorder
                                                    ) {
                                                        Text(
                                                            text = effect.badge,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = if (isSelected) Color.Black else TextSecondary,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = effect.displayName,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) NeonCyan else TextPrimary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = effect.description,
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }

                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { viewModel.updateVisualEffect(effect) },
                                                colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Cinematic Cut Transitions
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🎬 Scene Cut Transitions",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "High-energy transitions inserted at scene cut points:",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TransitionStyle.entries.forEach { transition ->
                                        val isSelected = currentProject?.transitionStyle == transition
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { viewModel.updateTransitionStyle(transition) }
                                                .testTag("transition_${transition.name}"),
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) ElectricViolet.copy(alpha = 0.2f) else StudioSurface,
                                            border = BorderStroke(1.dp, if (isSelected) ElectricViolet else StudioSurfaceBorder)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(text = transition.icon, fontSize = 20.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = transition.displayName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) ElectricVioletLight else TextPrimary
                                                )
                                                Text(
                                                    text = transition.description,
                                                    fontSize = 9.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Animated Sticker & Callout Assets
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🎨 Animated Stickers & Reaction Assets",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Playhead: ${(playbackPosition * 10).toInt() / 10f}s",
                                        fontSize = 10.sp,
                                        color = NeonCyan
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap to stamp an animated overlay at current scrubber position:",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val stickerPresets = listOf(
                                    Pair("🔥", "Hype Fire"),
                                    Pair("⚡", "Energy Bolt"),
                                    Pair("🎯", "Hook Target"),
                                    Pair("🚀", "Rocket"),
                                    Pair("👀", "Attention"),
                                    Pair("💬", "Comment"),
                                    Pair("💯", "100 Value"),
                                    Pair("⭐", "5 Stars"),
                                    Pair("💥", "Impact"),
                                    Pair("🏆", "Winner")
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    stickerPresets.forEach { (emoji, label) ->
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { viewModel.addSticker(emoji, label) }
                                                .testTag("sticker_$emoji"),
                                            shape = RoundedCornerShape(10.dp),
                                            color = StudioSurface,
                                            border = BorderStroke(1.dp, StudioSurfaceBorder)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(text = emoji, fontSize = 22.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = label,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                    }
                                }

                                // Display currently active stickers in project
                                val activeStickers = currentProject?.stickers ?: emptyList()
                                if (activeStickers.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = StudioSurfaceBorder, thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Active Stickers on Timeline (${activeStickers.size}):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    activeStickers.forEach { sticker ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = StudioSurface
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = sticker.emoji, fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${sticker.label} at ${(sticker.timestampSec * 10).toInt() / 10f}s",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = TextPrimary
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { viewModel.removeSticker(sticker.id) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Remove",
                                                        tint = TextMuted,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                EditorTab.AUDIO -> {
                    // Audio Soundtrack, Ducking & Volume
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Smart Audio Ducking & Volume",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Automatically lowers background music by 75% when speech is detected so voice is loud & clear.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Music Level", fontSize = 12.sp, color = TextPrimary)
                                    Text("${(currentProject.musicVolume * 100).toInt()}%", fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                }

                                Slider(
                                    value = currentProject.musicVolume,
                                    onValueChange = { viewModel.updateMusicVolume(it) },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = EmeraldSuccess,
                                        activeTrackColor = EmeraldSuccess,
                                        inactiveTrackColor = StudioSurfaceBorder
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Select Royalty-Free Soundtrack",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        SampleMediaData.MusicTracks.forEach { track ->
                            val isSelected = currentProject.selectedMusic?.id == track.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateMusic(track) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) StudioSurfaceElevated else StudioSurface
                                ),
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(EmeraldSuccess, NeonCyan))
                                ) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "🎵 ${track.title}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) EmeraldSuccess else TextPrimary
                                        )
                                        Text(
                                            text = "${track.genre} • ${track.bpm} BPM • Energy: ${track.energyLevel}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.updateMusic(track) },
                                        colors = RadioButtonDefaults.colors(selectedColor = EmeraldSuccess)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            project = currentProject,
            isExporting = isExporting,
            exportProgress = exportProgress,
            pipelineStage = exportPipelineStage,
            exportSuccess = exportSuccess,
            onStartExport = { quality ->
                viewModel.startExport(quality)
            },
            onToggleCaptions = { enabled ->
                viewModel.toggleCaptionsEnabled(enabled)
            },
            onSelectFontStyle = { fontStyle ->
                viewModel.updateCaptionFontStyle(fontStyle)
            },
            onDismiss = {
                showExportDialog = false
                viewModel.dismissExport()
            }
        )
    }

    // AI Generation Loading Dialog
    if (isGenerating) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = StudioSurfaceElevated,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = NeonCyan,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Applying AI Edit...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Text(text = generationStage, fontSize = 13.sp, color = NeonCyanLight)
            },
            confirmButton = {}
        )
    }
}

package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.SampleMediaData
import com.example.model.*
import com.example.ui.components.RawVideoPreviewCard
import com.example.ui.components.StylePickerSheet
import com.example.ui.theme.*
import com.example.viewmodel.ShortyViewModel

@Composable
fun HomeScreen(
    viewModel: ShortyViewModel,
    onNavigateToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val availableClips by viewModel.availableClips.collectAsState()
    val selectedClips by viewModel.selectedClips.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationStage by viewModel.generationStage.collectAsState()

    var showStyleSheet by remember { mutableStateOf(false) }

    // Zero-permission Android Photo Picker for picking user's own real raw footage!
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEachIndexed { index, uri ->
                viewModel.addCustomUserClip(
                    title = "Imported Raw Clip ${index + 1}",
                    uriString = uri.toString(),
                    durationSec = 25f + (index * 10f)
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioBackground,
        floatingActionButton = {
            if (selectedClips.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showStyleSheet = true },
                    modifier = Modifier.testTag("create_edit_fab"),
                    containerColor = ElectricViolet,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.AutoAwesome, "AI Cut") },
                    text = {
                        Text(
                            text = "AI Edit ${selectedClips.size} Clip(s) • Free",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Brand Header & Free Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(ElectricViolet, CyberPink))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = "Shorty Logo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Shorty AI",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Raw Footage to Viral Shorts & Reels",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldSuccess.copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(EmeraldSuccess, EmeraldSuccess)))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "100% FREE • $0/MO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }

            // Hero Card: How Shorty Works
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(ElectricViolet, NeonCyan)))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ElectricViolet.copy(alpha = 0.3f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Bolt, "AI", tint = ElectricVioletLight, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = "AI Video Editor for Raw Footage",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Upload or pick your raw clips. Describe your edit or select a viral reference. Shorty selects the best shots, auto-reframes to 9:16, removes silences, adds kinetic captions, and cloud-renders MP4 for free.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("import_raw_footage_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                            ) {
                                Icon(Icons.Default.VideoLibrary, "Import", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import Clips", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onNavigateToEditor,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(NeonCyan, ElectricViolet)))
                            ) {
                                Icon(Icons.Default.PlayCircle, "Open Studio", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Studio", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Reference Edit Styles Carousel
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trending Reference Styles",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Reference-Driven AI",
                            fontSize = 11.sp,
                            color = NeonCyanLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(SampleMediaData.EditStyles) { style ->
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable {
                                        viewModel.generateNewEdit(
                                            style = style,
                                            customPrompt = style.defaultPrompt,
                                            aspectRatio = AspectRatioType.RATIO_9_16,
                                            removeSilences = true
                                        )
                                        onNavigateToEditor()
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
                                    listOf(StudioSurfaceBorder, Color.Black)
                                ))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ElectricViolet.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = style.tag,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ElectricVioletLight,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Select",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = style.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = style.description,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "⚡ Pacing: ${style.cutPacingSec}s cuts • ${style.captionPreset.displayName}",
                                        fontSize = 10.sp,
                                        color = NeonCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Ready-to-Use Free AI Tools Deck
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("free_tools_deck"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.35f), ElectricViolet.copy(alpha = 0.35f)))
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Build, "Tools", tint = NeonCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ready-to-Use Free Tools", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f)
                            ) {
                                Text("$0.00 INCLUDED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val tools = listOf(
                                Triple("⚡ Auto-Silence Cutter", "Eliminates pauses & stutter", NeonCyan),
                                Triple("📱 9:16 Face Reframe", "Auto-tracks speaker in center", ElectricVioletLight),
                                Triple("💬 Kinetic Subtitles", "Word-by-word active highlights", AmberVibrant),
                                Triple("📼 VHS & Film Shaders", "Real-time aesthetic filter canvas", CyberPink),
                                Triple("🔊 Sound FX Engine", "Whooshes placed on cut beats", EmeraldSuccess)
                            )

                            tools.forEach { (title, desc, color) ->
                                Surface(
                                    modifier = Modifier.width(170.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = StudioSurface,
                                    border = BorderStroke(1.dp, StudioSurfaceBorder)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(desc, fontSize = 10.sp, color = TextSecondary, lineHeight = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dedicated Video Preview & Manual Trimmer Component
            if (selectedClips.isNotEmpty()) {
                item {
                    RawVideoPreviewCard(
                        selectedClips = selectedClips,
                        onUpdateTrim = { clipId, startSec, endSec ->
                            viewModel.updateClipTrim(clipId, startSec, endSec)
                        },
                        onStartAiProcessing = { showStyleSheet = true }
                    )
                }
            }

            // Raw Footage Library Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Raw Footage Clips",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Select clips for Shorty to analyze, cut & assemble",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = "${selectedClips.size} selected",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }

            // Raw Clips List
            items(availableClips) { clip ->
                val isSelected = selectedClips.any { it.id == clip.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleClipSelection(clip) }
                        .testTag("clip_item_${clip.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) StudioSurfaceElevated else StudioSurface
                    ),
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(ElectricViolet, NeonCyan))
                    ) else CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(StudioSurfaceBorder, StudioSurfaceBorder))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Coil-powered Video Thumbnail Box
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(clip.gradientColors.map { Color(it) })),
                            contentAlignment = Alignment.Center
                        ) {
                            val modelData = clip.sourceUri ?: clip.previewImageUrl
                            if (modelData != null) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(modelData)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Thumbnail for ${clip.title}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = NeonCyan
                                            )
                                        }
                                    },
                                    error = {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.75f)
                            ) {
                                val displayDuration = if (clip.trimEndSec != null) clip.trimmedDuration else clip.durationSec
                                Text(
                                    text = "${displayDuration.toInt()}s",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = clip.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Metadata Tags
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${clip.fileSizeMb}MB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                                Text(text = "•", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = clip.resolution,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                                Text(text = "•", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "Silence: -${clip.silencePercentage}%",
                                    fontSize = 10.sp,
                                    color = AmberVibrant,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(text = "•", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "Energy: ${clip.energyScore}/100",
                                    fontSize = 10.sp,
                                    color = NeonCyanLight,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${clip.dimensions} • ${clip.codec} • ${clip.detectedScenes.size} scenes",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleClipSelection(clip) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ElectricViolet,
                                uncheckedColor = TextMuted,
                                checkmarkColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Modal Style Sheet
    if (showStyleSheet) {
        StylePickerSheet(
            initialStyle = SampleMediaData.EditStyles.first(),
            initialRatio = AspectRatioType.RATIO_9_16,
            initialPrompt = "",
            initialRemoveSilences = true,
            initialEnableCaptions = true,
            initialCaptionFontStyle = CaptionFontStyle.BOLD_IMPACT,
            onDismiss = { showStyleSheet = false },
            onConfirm = { style, prompt, ratio, removeSilences, enableCaptions, captionFontStyle ->
                showStyleSheet = false
                viewModel.generateNewEdit(
                    style = style,
                    customPrompt = prompt,
                    aspectRatio = ratio,
                    removeSilences = removeSilences,
                    enableCaptions = enableCaptions,
                    captionFontStyle = captionFontStyle
                )
                onNavigateToEditor()
            }
        )
    }

    // AI Generation Loading Overlay
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
                    Text("Shorty AI Editing...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = generationStage, fontSize = 13.sp, color = NeonCyanLight)
                    Text(
                        text = "Analyzing footage, cutting silences, reframing faces to 9:16, generating animated subtitles...",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {}
        )
    }
}

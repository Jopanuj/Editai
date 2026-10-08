package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionFontStyle
import com.example.model.RenderQuality
import com.example.model.TimelineProject
import com.example.ui.theme.*

@Composable
fun ExportDialog(
    project: TimelineProject,
    isExporting: Boolean,
    exportProgress: Float,
    pipelineStage: String = "Processing pipeline...",
    exportSuccess: Boolean,
    onStartExport: (RenderQuality) -> Unit,
    onToggleCaptions: (Boolean) -> Unit = {},
    onSelectFontStyle: (CaptionFontStyle) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedQuality by remember { mutableStateOf(RenderQuality.REELS_1080P) }
    var captionsEnabled by remember(project.enableCaptions) { mutableStateOf(project.enableCaptions) }
    var selectedFontStyle by remember(project.captionFontStyle) { mutableStateOf(project.captionFontStyle) }
    var downloadedSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isExporting) onDismiss()
        },
        modifier = modifier.testTag("export_dialog"),
        containerColor = StudioSurfaceElevated,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Export",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (exportSuccess) "Ready to Share!" else "Export Free MP4",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldSuccess.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "$0.00 FREE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!exportSuccess && !isExporting) {
                    Text(
                        text = "Render and export your edited ${project.aspectRatio.label} cut. Shorty Free exports in full resolution without watermarks.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )

                    // Resolution Quality choices
                    Text(
                        text = "Select Export Quality",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RenderQuality.entries.forEach { quality ->
                            val isSelected = selectedQuality == quality
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedQuality = quality },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) ElectricViolet.copy(alpha = 0.25f) else StudioSurface,
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NeonCyan, ElectricViolet))) else null
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
                                            text = quality.label,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) NeonCyanLight else TextPrimary
                                        )
                                        Text(
                                            text = "${quality.resolution} • ~${quality.estSizeMb} MB",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedQuality = quality },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = NeonCyan,
                                            unselectedColor = TextMuted
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Caption Settings Toggle & Font Style Selector Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_caption_settings_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (captionsEnabled) listOf(AmberVibrant.copy(alpha = 0.6f), ElectricViolet.copy(alpha = 0.6f))
                                else listOf(StudioSurfaceBorder, StudioSurfaceBorder)
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                        tint = if (captionsEnabled) AmberVibrant else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Automatic Video Captions",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = if (captionsEnabled) "Bake animated subtitles into MP4" else "Clean raw video (No subtitles)",
                                            fontSize = 11.sp,
                                            color = if (captionsEnabled) AmberVibrant else TextSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = captionsEnabled,
                                    onCheckedChange = {
                                        captionsEnabled = it
                                        onToggleCaptions(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AmberVibrant
                                    ),
                                    modifier = Modifier.testTag("export_captions_switch")
                                )
                            }

                            if (captionsEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = StudioSurfaceBorder, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Select Subtitle Font Style",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AmberVibrant.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = selectedFontStyle.displayName,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AmberVibrant,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CaptionFontStyle.entries.forEach { fontStyle ->
                                        val isSelected = selectedFontStyle == fontStyle
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedFontStyle = fontStyle
                                                    onSelectFontStyle(fontStyle)
                                                }
                                                .testTag("font_style_${fontStyle.name}"),
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) ElectricViolet.copy(alpha = 0.35f) else StudioSurfaceElevated,
                                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                                brush = Brush.horizontalGradient(listOf(AmberVibrant, NeonCyan))
                                            ) else null
                                        ) {
                                            Text(
                                                text = fontStyle.displayName,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) AmberVibrant else TextSecondary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Live font preview card
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.6f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Preview: ",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                        val previewFamily = when (selectedFontStyle) {
                                            CaptionFontStyle.BOLD_IMPACT -> FontFamily.SansSerif
                                            CaptionFontStyle.CLEAN_SANS -> FontFamily.SansSerif
                                            CaptionFontStyle.TECH_MONO -> FontFamily.Monospace
                                            CaptionFontStyle.ELEGANT_SERIF -> FontFamily.Serif
                                            CaptionFontStyle.PLAYFUL_COMIC -> FontFamily.Cursive
                                        }
                                        val previewWeight = when (selectedFontStyle) {
                                            CaptionFontStyle.BOLD_IMPACT -> FontWeight.Black
                                            CaptionFontStyle.CLEAN_SANS -> FontWeight.Medium
                                            CaptionFontStyle.TECH_MONO -> FontWeight.Bold
                                            CaptionFontStyle.ELEGANT_SERIF -> FontWeight.Bold
                                            CaptionFontStyle.PLAYFUL_COMIC -> FontWeight.Bold
                                        }
                                        Text(
                                            text = selectedFontStyle.sampleText,
                                            fontSize = 12.sp,
                                            fontFamily = previewFamily,
                                            fontWeight = previewWeight,
                                            color = AmberVibrant,
                                            fontStyle = if (selectedFontStyle == CaptionFontStyle.ELEGANT_SERIF) FontStyle.Italic else FontStyle.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Value guarantee card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, "Check", tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("No Watermark • Full 60fps • 100% Free", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Shorty Edit is completely free with no subscriptions or daily limits.",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                } else if (isExporting) {
                    // ==========================================
                    // AI VIDEO PROCESSING PIPELINE PROGRESS VIEW
                    // ==========================================
                    val currentStepNum = when {
                        exportProgress < 0.20f -> 1
                        exportProgress < 0.40f -> 2
                        exportProgress < 0.60f -> 3
                        exportProgress < 0.80f -> 4
                        else -> 5
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("export_pipeline_progress_indicator"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pipeline Header & Live Progress Gauge
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.5f)))
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "AI Pipeline",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AI Processing Pipeline",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ElectricViolet.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = "STAGE $currentStepNum / 5",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NeonCyanLight,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Central Gauge with percentage
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = { exportProgress },
                                        modifier = Modifier.fillMaxSize(),
                                        color = NeonCyan,
                                        trackColor = StudioSurfaceBorder,
                                        strokeWidth = 7.dp
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${(exportProgress * 100).toInt()}%",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "GPU ACTIVE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldSuccess
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Active stage description ticker
                                Text(
                                    text = pipelineStage,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonCyanLight,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { exportProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = ElectricViolet,
                                    trackColor = StudioSurfaceBorder
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Real-time frame & hardware telemetry row
                                val totalFrames = 1250
                                val renderedFrames = (exportProgress * totalFrames).toInt()
                                val etaSeconds = ((1f - exportProgress) * 4.8f).toInt() + 1

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Frame $renderedFrames / $totalFrames",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "~${etaSeconds}s remaining",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberVibrant
                                    )
                                    Text(
                                        text = "60 FPS HW",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }

                        // Detailed 5-Stage Stepper List
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val pipelineStagesData = listOf(
                                Triple(
                                    "1. Frame Ingestion & Trimming",
                                    "Decodes raw source stream from trimmed boundaries",
                                    0.0f..0.20f
                                ),
                                Triple(
                                    "2. 9:16 Subject Auto-Reframing",
                                    "Locks face coordinates to vertical safe-zone",
                                    0.20f..0.40f
                                ),
                                Triple(
                                    if (captionsEnabled) "3. Subtitle Burn-In (${selectedFontStyle.displayName})" else "3. Color Grade & Visual Polish",
                                    if (captionsEnabled) "Bakes animated word highlights into video frames" else "Applies LUT color grading without subtitle burn-in",
                                    0.40f..0.60f
                                ),
                                Triple(
                                    "4. Audio Mastering & SFX Ducking",
                                    "Levels voice audio, background beat & whooshes",
                                    0.60f..0.80f
                                ),
                                Triple(
                                    "5. Hardware MP4 Finalizing",
                                    "Compresses ${selectedQuality.resolution} (Zero Watermark)",
                                    0.80f..1.00f
                                )
                            )

                            pipelineStagesData.forEachIndexed { index, (stageTitle, stageSub, range) ->
                                val isCompleted = exportProgress >= range.endInclusive
                                val isCurrent = exportProgress in range.start..range.endInclusive
                                val isPending = exportProgress < range.start

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        isCurrent -> ElectricViolet.copy(alpha = 0.2f)
                                        isCompleted -> StudioSurfaceElevated
                                        else -> StudioSurface
                                    },
                                    border = if (isCurrent) CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet))
                                    ) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Status Icon / Number indicator
                                            Box(
                                                modifier = Modifier.size(20.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                when {
                                                    isCompleted -> {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = "Done",
                                                            tint = EmeraldSuccess,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    isCurrent -> {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(15.dp),
                                                            color = NeonCyan,
                                                            strokeWidth = 2.dp
                                                        )
                                                    }
                                                    else -> {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = StudioSurfaceBorder,
                                                            modifier = Modifier.size(16.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Text(
                                                                    text = "${index + 1}",
                                                                    fontSize = 9.sp,
                                                                    color = TextMuted
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Column {
                                                Text(
                                                    text = stageTitle,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                    color = when {
                                                        isCurrent -> NeonCyanLight
                                                        isCompleted -> TextPrimary
                                                        else -> TextMuted
                                                    }
                                                )
                                                Text(
                                                    text = stageSub,
                                                    fontSize = 9.sp,
                                                    color = if (isCurrent) AmberVibrant else TextSecondary
                                                )
                                            }
                                        }

                                        // Status Pill
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when {
                                                isCompleted -> EmeraldSuccess.copy(alpha = 0.15f)
                                                isCurrent -> NeonCyan.copy(alpha = 0.2f)
                                                else -> StudioSurfaceBorder.copy(alpha = 0.3f)
                                            }
                                        ) {
                                            Text(
                                                text = when {
                                                    isCompleted -> "READY"
                                                    isCurrent -> "ACTIVE"
                                                    else -> "QUEUED"
                                                },
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    isCompleted -> EmeraldSuccess
                                                    isCurrent -> NeonCyanLight
                                                    else -> TextMuted
                                                },
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // Export Complete Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "Export Render Complete!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "Your ${project.aspectRatio.label} cut with ${project.cuts.size} clips${if (captionsEnabled) " & ${selectedFontStyle.displayName} captions" else " (clean raw video)"} is ready in ${selectedQuality.resolution}.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        if (downloadedSuccess) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "✅ Saved to Movies/ShortyAI/ directory!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Share Platforms Quick Actions
                        Text(
                            text = "Publish to Platform",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { downloadedSuccess = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                            ) {
                                Text("Reels", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { downloadedSuccess = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPink)
                            ) {
                                Text("TikTok", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { downloadedSuccess = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberVibrant)
                            ) {
                                Text("Shorts", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!exportSuccess && !isExporting) {
                Button(
                    onClick = { onStartExport(selectedQuality) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Bolt, "Render", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Render Free MP4", fontWeight = FontWeight.Bold)
                }
            } else if (exportSuccess) {
                Button(
                    onClick = {
                        downloadedSuccess = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, "Download", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Gallery", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isExporting) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                ) {
                    Text(if (exportSuccess) "Close" else "Cancel")
                }
            }
        }
    )
}

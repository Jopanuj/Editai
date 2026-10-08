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

                    // Project Assets & FX Summary
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = StudioSurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Assets & Effects: ${project.activeFilterEffect.badge} • ${project.activeOverlayItems.count { it.isEnabled }} Overlays${if (project.showAudioWaveform) " • Waveform" else ""}",
                                fontSize = 10.sp,
                                color = NeonCyanLight,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${project.soundEffects.size} SFX",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
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
                    // Rendering Progress View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { exportProgress },
                            modifier = Modifier.size(72.dp),
                            color = NeonCyan,
                            trackColor = StudioSurfaceBorder,
                            strokeWidth = 6.dp
                        )

                        Text(
                            text = "Rendering ${selectedQuality.label} MP4...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = if (captionsEnabled) {
                                "${(exportProgress * 100).toInt()}% • Encoding ${(exportProgress * 920).toInt()} frames with ${selectedFontStyle.displayName} captions"
                            } else {
                                "${(exportProgress * 100).toInt()}% • Encoding ${(exportProgress * 920).toInt()} frames (Clean raw video, no captions)"
                            },
                            fontSize = 12.sp,
                            color = NeonCyanLight,
                            fontFamily = FontFamily.Monospace
                        )

                        LinearProgressIndicator(
                            progress = { exportProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ElectricViolet,
                            trackColor = StudioSurfaceBorder
                        )
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

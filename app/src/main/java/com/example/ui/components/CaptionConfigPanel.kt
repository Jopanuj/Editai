package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.SampleMediaData
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.ShortyViewModel

@Composable
fun CaptionConfigPanel(
    project: TimelineProject,
    viewModel: ShortyViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("caption_config_panel"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. LIVE CAPTION PREVIEW CARD
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(Color(project.captionHighlightColorHex), ElectricViolet)
                )
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
                            imageVector = Icons.Default.ClosedCaption,
                            contentDescription = "Caption",
                            tint = Color(project.captionHighlightColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Caption Overlay Preview",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (project.enableCaptions) EmeraldSuccess.copy(alpha = 0.2f) else StudioSurfaceBorder
                    ) {
                        Text(
                            text = if (project.enableCaptions) "ACTIVE ON VIDEO" else "DISABLED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (project.enableCaptions) EmeraldSuccess else TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Simulated Preview Screen Frame
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = when (project.captionPosition) {
                            CaptionPosition.TOP -> Alignment.TopCenter
                            CaptionPosition.CENTER -> Alignment.Center
                            CaptionPosition.BOTTOM -> Alignment.BottomCenter
                        }
                    ) {
                        val previewFamily = when (project.captionFontStyle) {
                            CaptionFontStyle.BOLD_IMPACT -> FontFamily.SansSerif
                            CaptionFontStyle.CLEAN_SANS -> FontFamily.SansSerif
                            CaptionFontStyle.TECH_MONO -> FontFamily.Monospace
                            CaptionFontStyle.ELEGANT_SERIF -> FontFamily.Serif
                            CaptionFontStyle.PLAYFUL_COMIC -> FontFamily.Cursive
                        }

                        val previewSurfaceColor = when (project.captionBackgroundStyle) {
                            CaptionBackgroundStyle.SOLID_PILL -> Color.Black.copy(alpha = 0.85f)
                            CaptionBackgroundStyle.SUBTLE_BLUR -> Color.Black.copy(alpha = 0.45f)
                            CaptionBackgroundStyle.NEON_BORDER -> Color(0xFF020617).copy(alpha = 0.95f)
                            CaptionBackgroundStyle.NONE -> Color.Transparent
                        }

                        val previewBorder = when (project.captionBackgroundStyle) {
                            CaptionBackgroundStyle.NEON_BORDER -> BorderStroke(
                                1.dp,
                                Brush.horizontalGradient(listOf(Color(project.captionHighlightColorHex), CyberPink))
                            )
                            CaptionBackgroundStyle.SUBTLE_BLUR -> BorderStroke(1.dp, StudioSurfaceBorder)
                            else -> null
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = previewSurfaceColor,
                            border = previewBorder,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val normalSize = (project.captionFontSizeSp * 0.9f).sp
                                val activeSize = (project.captionFontSizeSp * 1.05f).sp

                                Text(
                                    text = if (project.captionFontStyle == CaptionFontStyle.BOLD_IMPACT) "STOP " else "Stop ",
                                    fontSize = normalSize,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = previewFamily,
                                    color = Color(project.captionTextColorHex)
                                )

                                Text(
                                    text = if (project.captionFontStyle == CaptionFontStyle.BOLD_IMPACT) "SCROLLING 🔥 " else "Scrolling 🔥 ",
                                    fontSize = activeSize,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = previewFamily,
                                    color = Color(project.captionHighlightColorHex)
                                )

                                Text(
                                    text = "2026",
                                    fontSize = normalSize,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = previewFamily,
                                    color = Color(project.captionTextColorHex)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. MASTER TOGGLE & FONT TYPOGRAPHY
        // ==========================================
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
                    Column {
                        Text(
                            text = "Automatic Caption Overlay",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Word-by-word active karaoke animation",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = project.enableCaptions,
                        onCheckedChange = { viewModel.toggleCaptionsEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(project.captionHighlightColorHex)
                        ),
                        modifier = Modifier.testTag("caption_master_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Font Typography Style",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CaptionFontStyle.entries.forEach { fontStyle ->
                        val isSelected = project.captionFontStyle == fontStyle
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.updateCaptionFontStyle(fontStyle) }
                                .testTag("caption_font_${fontStyle.name}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) StudioSurface else StudioSurface.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(Color(project.captionHighlightColorHex), ElectricViolet))
                            ) else BorderStroke(1.dp, StudioSurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = fontStyle.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(project.captionHighlightColorHex) else TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${fontStyle.sampleText})",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
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
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(project.captionHighlightColorHex)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. CAPTION FONT SIZE SLIDER & PRESETS
        // ==========================================
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
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Size",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Caption Font Size",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "${project.captionFontSizeSp.toInt()} sp",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyanLight
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = project.captionFontSizeSp,
                    onValueChange = { viewModel.updateCaptionFontSize(it) },
                    valueRange = 12f..28f,
                    steps = 15,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("caption_font_size_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = Color(project.captionHighlightColorHex),
                        inactiveTrackColor = StudioSurfaceBorder
                    )
                )

                // Quick Size Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sizePresets = listOf(
                        Pair(13f, "Compact"),
                        Pair(16f, "Standard"),
                        Pair(20f, "Impact"),
                        Pair(24f, "Viral Giant")
                    )

                    sizePresets.forEach { (size, label) ->
                        val isCurrent = project.captionFontSizeSp.toInt() == size.toInt()
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.updateCaptionFontSize(size) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrent) NeonCyan.copy(alpha = 0.2f) else StudioSurface,
                            border = if (isCurrent) BorderStroke(1.dp, NeonCyan) else BorderStroke(1.dp, StudioSurfaceBorder)
                        ) {
                            Text(
                                text = "$label (${size.toInt()})",
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) NeonCyanLight else TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. CAPTION HIGHLIGHT COLOR PALETTE
        // ==========================================
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
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Color",
                            tint = AmberVibrant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Highlight Color Palette",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Active Word Glow",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Color Swatches Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SampleMediaData.CaptionColorPalettes.forEach { palette ->
                        val isSelected = project.captionHighlightColorHex == palette.highlightColorHex
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.updateCaptionColors(
                                        highlightHex = palette.highlightColorHex,
                                        textHex = palette.textColorHex
                                    )
                                }
                                .testTag("caption_color_${palette.id}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) StudioSurface else StudioSurface.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(1.5.dp, Color(palette.highlightColorHex)) else BorderStroke(1.dp, StudioSurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(palette.highlightColorHex))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = palette.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color(palette.highlightColorHex) else TextPrimary
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(palette.highlightColorHex),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. CAPTION POSITION SAFE-ZONE
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Caption Screen Position",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Position subtitles to avoid obscuring faces or UI controls.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CaptionPosition.entries.forEach { position ->
                        val isSelected = project.captionPosition == position
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.updateCaptionPosition(position) }
                                .testTag("caption_pos_${position.name}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ElectricViolet.copy(alpha = 0.25f) else StudioSurface,
                            border = if (isSelected) BorderStroke(1.dp, ElectricVioletLight) else BorderStroke(1.dp, StudioSurfaceBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = position.badge,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ElectricVioletLight else TextPrimary
                                )
                                Text(
                                    text = position.displayName,
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. CAPTION BOX BACKGROUND STYLE
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Caption Container Box Style",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CaptionBackgroundStyle.entries.forEach { style ->
                        val isSelected = project.captionBackgroundStyle == style
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.updateCaptionBackgroundStyle(style) }
                                .testTag("caption_bg_${style.name}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AmberVibrant.copy(alpha = 0.2f) else StudioSurface,
                            border = if (isSelected) BorderStroke(1.dp, AmberVibrant) else BorderStroke(1.dp, StudioSurfaceBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = style.badge,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AmberVibrant else TextPrimary
                                )
                                Text(
                                    text = style.displayName,
                                    fontSize = 8.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

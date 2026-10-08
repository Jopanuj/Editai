package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun VideoPlayerCard(
    project: TimelineProject,
    playbackPositionSec: Float,
    isPlaying: Boolean,
    playbackSpeed: Float,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onRatioChange: (AspectRatioType) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDuration = if (project.totalDurationSec > 0f) project.totalDurationSec else 1f

    // Find active cut at this playback timestamp
    val activeCut = project.cuts.find {
        playbackPositionSec >= it.timelineStartSec && playbackPositionSec < (it.timelineStartSec + it.durationSec)
    } ?: project.cuts.firstOrNull()

    // Find active caption phrase at this timestamp
    val activeCaption = project.captions.find {
        playbackPositionSec >= it.startTimeSec && playbackPositionSec <= it.endTimeSec
    }

    // Dynamic wave animation for audio simulation
    val infiniteTransition = rememberInfiniteTransition(label = "player_effects")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_player_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
            listOf(StudioSurfaceBorder, Color.Black)
        ))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Ratio Container (Constrained aspect ratio preview)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Video canvas simulating footage rendering & color grade
                val ratioModifier = when (project.aspectRatio) {
                    AspectRatioType.RATIO_9_16 -> Modifier.fillMaxHeight().aspectRatio(9f / 16f)
                    AspectRatioType.RATIO_16_9 -> Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                    AspectRatioType.RATIO_1_1 -> Modifier.fillMaxHeight().aspectRatio(1f)
                    AspectRatioType.RATIO_4_5 -> Modifier.fillMaxHeight().aspectRatio(4f / 5f)
                }

                Box(
                    modifier = ratioModifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTogglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    // Simulated Video Frame with Dynamic Gradient & Movement & Visual Effects
                    SimulatedVideoFrame(
                        activeCut = activeCut,
                        colorGrade = project.colorGrade,
                        activeEffect = project.activeEffect,
                        playbackPositionSec = playbackPositionSec,
                        zoomFactor = activeCut?.zoomFactor ?: 1.0f
                    )

                    // Animated Floating Stickers Overlay
                    project.stickers.forEach { sticker ->
                        val isStickerActive = playbackPositionSec >= sticker.timestampSec &&
                                playbackPositionSec <= (sticker.timestampSec + sticker.durationSec)
                        if (isStickerActive) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(y = (-60).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.horizontalGradient(listOf(AmberVibrant, CyberPink))
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = sticker.emoji, fontSize = 26.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = sticker.label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Active Animated Captions Overlay (Hormozi / Cyber / Clean style if enabled)
                    if (project.enableCaptions && activeCaption != null) {
                        AnimatedCaptionOverlay(
                            caption = activeCaption,
                            preset = project.style.captionPreset,
                            fontStyle = project.captionFontStyle,
                            phraseProgressSec = playbackPositionSec - activeCaption.startTimeSec,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 28.dp, start = 8.dp, end = 8.dp)
                        )
                    }

                    // Watermark-Free Badge indicator
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨ 100% FREE • NO WATERMARK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }

                    // Top Left Cut & Hook Label
                    if (activeCut != null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = if (activeCut.isHook) CyberPink.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.65f)
                        ) {
                            Text(
                                text = activeCut.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Play icon splash when paused
                    if (!isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Active sound effect indicator
                    val currentSfx = project.soundEffects.find {
                        playbackPositionSec >= it.timestampSec && playbackPositionSec <= (it.timestampSec + 0.6f)
                    }
                    if (currentSfx != null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp),
                            shape = CircleShape,
                            color = ElectricViolet.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = "${currentSfx.icon} ${currentSfx.name}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player Scrub Slider & Timestamps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(playbackPositionSec),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = NeonCyanLight
                )

                Text(
                    text = "Ratio: ${project.aspectRatio.label} • ${project.colorGrade.displayName}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Text(
                    text = formatTime(totalDuration),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }

            Slider(
                value = playbackPositionSec.coerceIn(0f, totalDuration),
                onValueChange = { onSeek(it) },
                valueRange = 0f..totalDuration,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playback_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = ElectricViolet,
                    inactiveTrackColor = StudioSurfaceBorder
                )
            )

            // Playback Transport Controls & Ratio Quick Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Speed Selector
                OutlinedButton(
                    onClick = {
                        val nextSpeed = when (playbackSpeed) {
                            1.0f -> 1.5f
                            1.5f -> 2.0f
                            else -> 1.0f
                        }
                        onSpeedChange(nextSpeed)
                    },
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(StudioSurfaceBorder, StudioSurfaceBorder)))
                ) {
                    Text(text = "${playbackSpeed}x", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Center: Transport Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onSeek((playbackPositionSec - 2f).coerceAtLeast(0f)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Replay 2s",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("play_pause_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = ElectricViolet
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSeek((playbackPositionSec + 2f).coerceAtMost(totalDuration)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward5,
                            contentDescription = "Forward 5s",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Right: Aspect Ratio Cycle button
                OutlinedButton(
                    onClick = {
                        val nextRatio = when (project.aspectRatio) {
                            AspectRatioType.RATIO_9_16 -> AspectRatioType.RATIO_16_9
                            AspectRatioType.RATIO_16_9 -> AspectRatioType.RATIO_1_1
                            AspectRatioType.RATIO_1_1 -> AspectRatioType.RATIO_4_5
                            AspectRatioType.RATIO_4_5 -> AspectRatioType.RATIO_9_16
                        }
                        onRatioChange(nextRatio)
                    },
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(NeonCyan, ElectricViolet)))
                ) {
                    Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = "Change Aspect Ratio",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = project.aspectRatio.label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SimulatedVideoFrame(
    activeCut: CutTimelineItem?,
    colorGrade: ColorGradePreset,
    activeEffect: VisualEffectPreset = VisualEffectPreset.NONE,
    playbackPositionSec: Float,
    zoomFactor: Float
) {
    // Generate simulated dynamic video frame background
    val baseGrad = when (activeCut?.clipId) {
        "clip_podcast_1" -> listOf(Color(0xFF2E1065), Color(0xFF1E1B4B), Color(0xFF0F172A))
        "clip_street_food" -> listOf(Color(0xFF7C2D12), Color(0xFF9A3412), Color(0xFF18181B))
        "clip_gym_pr" -> listOf(Color(0xFF064E3B), Color(0xFF065F46), Color(0xFF111827))
        "clip_tech_setup" -> listOf(Color(0xFF083344), Color(0xFF164E63), Color(0xFF020617))
        "clip_travel_bali" -> listOf(Color(0xFF831843), Color(0xFF701A75), Color(0xFF172554))
        else -> listOf(Color(0xFF1E1035), Color(0xFF311042), Color(0xFF0F172A))
    }

    // Apply color grade tint
    val tintOverlay = when (colorGrade) {
        ColorGradePreset.TEAL_ORANGE -> Color(0xFF06B6D4).copy(alpha = 0.12f)
        ColorGradePreset.VIBRANT_POP -> Color(0xFFEC4899).copy(alpha = 0.08f)
        ColorGradePreset.MOODY_NOIR -> Color.Black.copy(alpha = 0.25f)
        ColorGradePreset.GOLDEN_HOUR -> Color(0xFFF59E0B).copy(alpha = 0.15f)
        ColorGradePreset.NATURAL -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(baseGrad, radius = 900f * zoomFactor))
    ) {
        // Subtle motion wave simulation & Visual Effects
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val timeOffset = (playbackPositionSec * 4f)

            // Draw simulated video subjects / cinematic camera grid lines
            drawRect(
                color = tintOverlay,
                size = size
            )

            // Ambient light beam
            val beamX = width * 0.5f + (sin(timeOffset) * width * 0.15f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(beamX, height * 0.35f),
                    radius = width * 0.4f * zoomFactor
                ),
                radius = width * 0.4f * zoomFactor,
                center = Offset(beamX, height * 0.35f)
            )

            // Auto-reframe face tracking target box
            val faceCenter = Offset(width * 0.5f, height * 0.38f)
            val boxSize = Size(width * 0.38f * zoomFactor, height * 0.28f * zoomFactor)
            drawRoundRect(
                color = NeonCyan.copy(alpha = 0.35f),
                topLeft = Offset(faceCenter.x - boxSize.width / 2, faceCenter.y - boxSize.height / 2),
                size = boxSize,
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 2f)
            )

            // Visual Effects Overlays
            when (activeEffect) {
                VisualEffectPreset.FILM_GRAIN -> {
                    // Textured 35mm film grain micro-dots
                    for (i in 0..24) {
                        val gx = ((sin(timeOffset + i * 1.9f) * 0.5f + 0.5f) * width)
                        val gy = ((Math.cos(timeOffset * 1.4 + i * 2.3) * 0.5 + 0.5) * height).toFloat()
                        drawCircle(Color.White.copy(alpha = 0.12f), radius = 2.5f, center = Offset(gx, gy))
                    }
                }
                VisualEffectPreset.CYBER_GLITCH -> {
                    // RGB Glitch Slices
                    val sliceY = (height * 0.40f) + (sin(timeOffset * 9f) * height * 0.25f)
                    drawRect(NeonCyan.copy(alpha = 0.25f), Offset(0f, sliceY), Size(width, 14f))
                    drawRect(CyberPink.copy(alpha = 0.22f), Offset(0f, sliceY + 16f), Size(width, 10f))
                }
                VisualEffectPreset.LIGHT_LEAK -> {
                    // Optical Warm Lens Flare
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(AmberVibrant.copy(alpha = 0.4f), Color.Transparent),
                            center = Offset(width * 0.85f, height * 0.15f),
                            radius = width * 0.65f
                        ),
                        radius = width * 0.65f,
                        center = Offset(width * 0.85f, height * 0.15f)
                    )
                }
                VisualEffectPreset.FLASH_IMPACT -> {
                    // High-energy strobe flashes on transitions
                    val strobe = sin(timeOffset * 6f)
                    if (strobe > 0.82f) {
                        drawRect(Color.White.copy(alpha = 0.32f), size = size)
                    }
                }
                VisualEffectPreset.VHS_RETRO -> {
                    // Horizontal CRT Scanlines
                    var scanY = 0f
                    while (scanY < height) {
                        drawLine(Color.Black.copy(alpha = 0.35f), Offset(0f, scanY), Offset(width, scanY), strokeWidth = 1.2f)
                        scanY += 8f
                    }
                }
                VisualEffectPreset.NONE -> {}
            }
        }

        // Retro VHS Overlay tag if enabled
        if (activeEffect == VisualEffectPreset.VHS_RETRO) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "PLAY ▶ 00:0${(playbackPositionSec * 10).toInt() / 10} SP",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Clip Title Badge in center
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = activeCut?.clipTitle ?: "Raw Clip",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "⚡ Shot Auto-Reframed • Zoom ${(zoomFactor * 10).toInt() / 10f}x",
                fontSize = 10.sp,
                color = NeonCyanLight,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnimatedCaptionOverlay(
    caption: CaptionPhrase,
    preset: CaptionPreset,
    fontStyle: CaptionFontStyle,
    phraseProgressSec: Float,
    modifier: Modifier = Modifier
) {
    val totalPhraseDuration = (caption.endTimeSec - caption.startTimeSec).coerceAtLeast(0.1f)
    val progressFraction = (phraseProgressSec / totalPhraseDuration).coerceIn(0f, 1f)

    // Calculate active word index
    val activeWordIndex = if (caption.words.isNotEmpty()) {
        val wordDuration = 1f / caption.words.size
        (progressFraction / wordDuration).toInt().coerceIn(0, caption.words.size - 1)
    } else 0

    val fontFamily = when (fontStyle) {
        CaptionFontStyle.BOLD_IMPACT -> FontFamily.SansSerif
        CaptionFontStyle.CLEAN_SANS -> FontFamily.SansSerif
        CaptionFontStyle.TECH_MONO -> FontFamily.Monospace
        CaptionFontStyle.ELEGANT_SERIF -> FontFamily.Serif
        CaptionFontStyle.PLAYFUL_COMIC -> FontFamily.Cursive
    }

    val letterSpacing = when (fontStyle) {
        CaptionFontStyle.TECH_MONO -> 1.5.sp
        CaptionFontStyle.CLEAN_SANS -> 0.6.sp
        CaptionFontStyle.ELEGANT_SERIF -> 1.0.sp
        else -> 0.sp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = when (preset) {
                CaptionPreset.HORMOZI -> Color.Black.copy(alpha = 0.75f)
                CaptionPreset.NEON_PULSE -> Color(0xFF0F172A).copy(alpha = 0.85f)
                CaptionPreset.MINIMAL_STUDIO -> Color.Black.copy(alpha = 0.6f)
                CaptionPreset.BEAST_MODE -> Color(0xFF18181B).copy(alpha = 0.9f)
            },
            border = if (preset == CaptionPreset.NEON_PULSE) {
                CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NeonCyan, CyberPink)))
            } else null
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (caption.words.isNotEmpty()) {
                    caption.words.forEachIndexed { index, word ->
                        val isWordActive = index == activeWordIndex
                        val isHighlighted = word.highlight || isWordActive

                        val textColor = when {
                            isWordActive && preset == CaptionPreset.HORMOZI -> AmberVibrant // Glowing yellow
                            isWordActive && preset == CaptionPreset.NEON_PULSE -> NeonCyan // Cyan
                            isWordActive && preset == CaptionPreset.BEAST_MODE -> EmeraldSuccess
                            isHighlighted -> Color(preset.highlightColorHex)
                            else -> Color.White
                        }

                        val fontWeight = when (fontStyle) {
                            CaptionFontStyle.BOLD_IMPACT -> if (isHighlighted) FontWeight.Black else FontWeight.ExtraBold
                            CaptionFontStyle.CLEAN_SANS -> if (isHighlighted) FontWeight.Bold else FontWeight.Medium
                            CaptionFontStyle.TECH_MONO -> if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                            CaptionFontStyle.ELEGANT_SERIF -> if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                            CaptionFontStyle.PLAYFUL_COMIC -> FontWeight.Bold
                        }

                        val displayedWord = when (fontStyle) {
                            CaptionFontStyle.BOLD_IMPACT -> word.word.uppercase()
                            CaptionFontStyle.TECH_MONO -> word.word.uppercase()
                            else -> word.word
                        }

                        val isItalic = (preset == CaptionPreset.NEON_PULSE) || (fontStyle == CaptionFontStyle.ELEGANT_SERIF)

                        Text(
                            text = "${displayedWord}${if (word.emoji != null && isWordActive) " " + word.emoji else ""} ",
                            fontSize = if (isWordActive) 16.sp else 14.sp,
                            fontWeight = fontWeight,
                            fontFamily = fontFamily,
                            letterSpacing = letterSpacing,
                            color = textColor,
                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
                        )
                    }
                } else {
                    Text(
                        text = caption.text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        letterSpacing = letterSpacing,
                        color = Color(preset.highlightColorHex),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Float): String {
    val totalSecs = seconds.toInt()
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    val millis = ((seconds - totalSecs) * 10).toInt()
    return String.format("%02d:%02d.%01d", mins, secs, millis)
}

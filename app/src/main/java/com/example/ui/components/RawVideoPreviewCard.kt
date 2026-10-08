package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.model.RawVideoClip
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RawVideoPreviewCard(
    selectedClips: List<RawVideoClip>,
    onUpdateTrim: (clipId: String, startSec: Float, endSec: Float) -> Unit,
    onStartAiProcessing: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedClips.isEmpty()) return

    val context = LocalContext.current
    var activeClipIndex by remember { mutableIntStateOf(0) }
    val currentClip = selectedClips.getOrNull(activeClipIndex) ?: selectedClips.first()

    // Scrubber playback position
    var scrubTimeSec by remember(currentClip.id) { mutableFloatStateOf(currentClip.trimStartSec) }
    var isPreviewPlaying by remember { mutableStateOf(false) }

    // Manual trimming bounds
    var trimRange by remember(currentClip.id, currentClip.trimStartSec, currentClip.trimEndSec) {
        mutableStateOf(currentClip.trimStartSec..currentClip.effectiveTrimEnd)
    }

    // Auto-advance scrubber during preview playback (respecting trim boundaries)
    LaunchedEffect(isPreviewPlaying, currentClip.id, trimRange) {
        while (isPreviewPlaying) {
            delay(100)
            val nextTime = scrubTimeSec + 0.3f
            if (nextTime > trimRange.endInclusive) {
                scrubTimeSec = trimRange.start // Loop back to trim start point
            } else {
                scrubTimeSec = nextTime
            }
        }
    }

    // Active detected scene at this scrub timestamp
    val activeScene = currentClip.detectedScenes.find {
        scrubTimeSec >= it.startTimeSec && scrubTimeSec <= it.endTimeSec
    } ?: currentClip.detectedScenes.firstOrNull()

    // Coil ImageLoader with VideoFrameDecoder support
    val videoImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("raw_video_preview_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(NeonCyan.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.2f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Raw Footage Preview & Clip Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Raw Preview",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Raw Footage Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "COIL VIDEO ENGINE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyanLight,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multiple clips switcher tabs if user selected multiple raw videos
            if (selectedClips.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedClips.forEachIndexed { index, clip ->
                        val isSelected = index == activeClipIndex
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    activeClipIndex = index
                                    scrubTimeSec = clip.trimStartSec
                                    isPreviewPlaying = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ElectricViolet.copy(alpha = 0.35f) else StudioSurface,
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(ElectricViolet, NeonCyan))
                            ) else null
                        ) {
                            Text(
                                text = "Clip ${index + 1}: ${clip.title.take(14)}...",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyanLight else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Screen Canvas: Coil Image / Video Frame Rendering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val coilData = currentClip.sourceUri ?: currentClip.previewImageUrl

                val imageRequest: ImageRequest = remember(coilData, scrubTimeSec) {
                    ImageRequest.Builder(context)
                        .data(coilData)
                        .videoFrameMillis((scrubTimeSec * 1000).toLong())
                        .crossfade(true)
                        .build()
                }

                if (coilData != null) {
                    SubcomposeAsyncImage(
                        model = imageRequest,
                        imageLoader = videoImageLoader,
                        contentDescription = "Raw footage frame preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { isPreviewPlaying = !isPreviewPlaying },
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(currentClip.gradientColors.map { Color(it) })),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = NeonCyan,
                                    strokeWidth = 3.dp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(currentClip.gradientColors.map { Color(it) })),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = "Video preview",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(currentClip.gradientColors.map { Color(it) })),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Video preview",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                // AI Telemetry Overlay Canvas (Face tracking target box + rule of thirds)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val t = scrubTimeSec * 2f

                    drawLine(Color.White.copy(alpha = 0.12f), Offset(w * 0.33f, 0f), Offset(w * 0.33f, h), strokeWidth = 1f)
                    drawLine(Color.White.copy(alpha = 0.12f), Offset(w * 0.66f, 0f), Offset(w * 0.66f, h), strokeWidth = 1f)

                    if (currentClip.detectedFaces > 0) {
                        val faceX = w * 0.5f + (sin(t) * 15f)
                        val faceY = h * 0.4f
                        val boxW = w * 0.28f
                        val boxH = h * 0.40f

                        drawRoundRect(
                            color = NeonCyan.copy(alpha = 0.7f),
                            topLeft = Offset(faceX - boxW / 2, faceY - boxH / 2),
                            size = Size(boxW, boxH),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }

                // Top Left: Resolution & Category
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPreviewPlaying) EmeraldSuccess else NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${currentClip.resolution} • ${currentClip.category}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Top Right: Energy & Silence Indicator
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = if (activeScene?.isSilence == true) AmberVibrant.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = if (activeScene?.isSilence == true) "⚠️ Silence (To Cut)" else "⚡ Energy: ${currentClip.energyScore}/100",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Play Button overlay when paused
                if (!isPreviewPlaying) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable { isPreviewPlaying = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Preview Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Bottom Overlay: Face Tracked Readout
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Text(
                        text = "🎯 Face Tracked (${currentClip.detectedFaces}) • Scene: ${activeScene?.label ?: "Raw"}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonCyanLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // RAW VIDEO METADATA DASHBOARD
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("raw_video_metadata_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(StudioSurfaceBorder, StudioSurfaceBorder))
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Metadata",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Source Video Metadata",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StudioSurfaceElevated
                        ) {
                            Text(
                                text = "${currentClip.codec} • ${currentClip.frameRate}fps",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonCyanLight,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3-Column Metadata Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. File Size
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = StudioSurfaceElevated
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SdCard,
                                        contentDescription = "File Size",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "FILE SIZE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currentClip.fileSizeMb} MB",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "~${currentClip.bitrateMbps} Mbps",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // 2. Duration
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = StudioSurfaceElevated
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Duration",
                                        tint = AmberVibrant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "DURATION",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatDuration(currentClip.durationSec),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${(currentClip.durationSec * 10).toInt() / 10f}s total",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // 3. Resolution & Dimensions
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = StudioSurfaceElevated
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = "Resolution",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "RESOLUTION",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentClip.resolution.substringBefore(" "),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = currentClip.dimensions,
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // MANUAL TRIMMING SLIDER COMPONENT
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.4f), ElectricViolet.copy(alpha = 0.4f)))
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Trimming Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = "Manual Trim",
                                tint = ElectricVioletLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Manual Footage Trimmer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Trimmed duration badge
                        val trimmedDuration = (trimRange.endInclusive - trimRange.start).coerceAtLeast(0.1f)
                        val trimmedSaved = currentClip.durationSec - trimmedDuration

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldSuccess.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "${formatDuration(trimmedDuration)} kept (${String.format("-%.1fs", trimmedSaved)})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Start & End Point time readouts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("START POINT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text(
                                text = formatDuration(trimRange.start),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyanLight
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PLAYHEAD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text(
                                text = formatDuration(scrubTimeSec),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("END POINT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text(
                                text = formatDuration(trimRange.endInclusive),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ElectricVioletLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dual-handle RangeSlider for defining In-Point and Out-Point
                    RangeSlider(
                        value = trimRange,
                        onValueChange = { range ->
                            // Ensure minimum trim window of 1 second
                            if (range.endInclusive - range.start >= 1.0f) {
                                trimRange = range
                                scrubTimeSec = scrubTimeSec.coerceIn(range.start, range.endInclusive)
                                onUpdateTrim(currentClip.id, range.start, range.endInclusive)
                            }
                        },
                        valueRange = 0f..currentClip.durationSec,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_trim_range_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = ElectricViolet,
                            inactiveTrackColor = StudioSurfaceBorder
                        )
                    )

                    // Visual Scene Segmentation Bar with Trim Mask
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioSurfaceBorder)
                    ) {
                        // Colored scenes
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            val scenes = currentClip.detectedScenes
                            if (scenes.isNotEmpty()) {
                                scenes.forEach { scene ->
                                    val sceneDuration = (scene.endTimeSec - scene.startTimeSec).coerceAtLeast(0.1f)
                                    val weight = (sceneDuration / currentClip.durationSec).coerceAtLeast(0.01f)

                                    // Check if scene falls outside the manual trim range
                                    val isOutsideTrim = scene.endTimeSec < trimRange.start || scene.startTimeSec > trimRange.endInclusive

                                    Box(
                                        modifier = Modifier
                                            .weight(weight)
                                            .fillMaxHeight()
                                            .background(
                                                when {
                                                    isOutsideTrim -> Color.Black.copy(alpha = 0.7f) // Dimmed
                                                    scene.isSilence -> AmberVibrant // Silence
                                                    scene.importanceScore >= 95 -> CyberPink // Hook
                                                    else -> ElectricViolet
                                                }
                                            )
                                            .clickable {
                                                scrubTimeSec = scene.startTimeSec.coerceIn(trimRange.start, trimRange.endInclusive)
                                            }
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(ElectricViolet))
                            }
                        }

                        // Playhead indicator needle
                        val playheadFraction = (scrubTimeSec / currentClip.durationSec).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .offset(x = 0.dp) // Needle will sit over bar
                                .background(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Trim Quick Action Buttons: Set In, Set Out, Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val newStart = scrubTimeSec
                                if (trimRange.endInclusive - newStart >= 1.0f) {
                                    trimRange = newStart..trimRange.endInclusive
                                    onUpdateTrim(currentClip.id, newStart, trimRange.endInclusive)
                                }
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(NeonCyan, NeonCyan)))
                        ) {
                            Text("Set In Point", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val newEnd = scrubTimeSec
                                if (newEnd - trimRange.start >= 1.0f) {
                                    trimRange = trimRange.start..newEnd
                                    onUpdateTrim(currentClip.id, trimRange.start, newEnd)
                                }
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricVioletLight),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(ElectricViolet, ElectricViolet)))
                        ) {
                            Text("Set Out Point", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                trimRange = 0f..currentClip.durationSec
                                scrubTimeSec = 0f
                                onUpdateTrim(currentClip.id, 0f, currentClip.durationSec)
                            },
                            modifier = Modifier.weight(0.8f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("Reset", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main CTA: Proceed to AI processing with trimmed footage
            Button(
                onClick = onStartAiProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("start_ai_processing_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
            ) {
                Icon(Icons.Default.AutoAwesome, "AI Cut", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Cut & Reframe Trimmed Footage (Free)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun formatDuration(seconds: Float): String {
    val totalSecs = seconds.toInt()
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    val fraction = ((seconds - totalSecs) * 10).toInt()
    return String.format("%02d:%02d.%01d", mins, secs, fraction)
}

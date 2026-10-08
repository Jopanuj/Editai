package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TimelineView(
    project: TimelineProject,
    playbackPositionSec: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val totalDuration = if (project.totalDurationSec > 0f) project.totalDurationSec else 1f

    // 1 second corresponds to 40.dp on timeline canvas for nice readability
    val pixelsPerSec = 42.dp
    val totalTimelineWidth = pixelsPerSec * totalDuration

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_view"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
            listOf(StudioSurfaceBorder, Color.Black)
        ))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ViewTimeline,
                        contentDescription = "Timeline",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Multi-Track Edit Timeline",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioSurfaceElevated
                ) {
                    Text(
                        text = "${project.cuts.size} Cuts • ${(totalDuration * 10).toInt() / 10f}s",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonCyanLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontally Scrollable Multi-Track Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioBackground)
                    .horizontalScroll(scrollState)
            ) {
                Column(
                    modifier = Modifier
                        .width(totalTimelineWidth.coerceAtLeast(360.dp))
                        .fillMaxHeight()
                        .padding(vertical = 6.dp, horizontal = 8.dp)
                ) {
                    // Time Ruler Track (Seconds markers)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    ) {
                        val numMarkers = (totalDuration / 2f).toInt() + 1
                        for (i in 0..numMarkers) {
                            val sec = i * 2f
                            Box(
                                modifier = Modifier
                                    .width(pixelsPerSec * 2f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.5.dp)
                                            .height(8.dp)
                                            .background(TextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${sec.toInt()}s",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // TRACK 1: Video Cuts Track
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (cut in project.cuts) {
                            val cutWidth = pixelsPerSec * cut.durationSec
                            val isCurrentCut = playbackPositionSec >= cut.timelineStartSec && playbackPositionSec < (cut.timelineStartSec + cut.durationSec)

                            val cutColor = if (cut.isHook) {
                                Brush.horizontalGradient(listOf(CyberPink, ElectricViolet))
                            } else {
                                Brush.horizontalGradient(listOf(VideoTrackColor, ElectricViolet))
                            }

                            Box(
                                modifier = Modifier
                                    .width(cutWidth.coerceAtLeast(40.dp))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cutColor)
                                    .border(
                                        width = if (isCurrentCut) 2.dp else 1.dp,
                                        color = if (isCurrentCut) NeonCyan else Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSeek(cut.timelineStartSec) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (cut.isHook) "⚡ HOOK" else cut.label,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (cut.zoomFactor > 1.1f) {
                                            Text(
                                                text = "${cut.zoomFactor}x",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AmberVibrant
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${(cut.durationSec * 10).toInt() / 10f}s",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // TRACK 2: Audio Soundtrack & Sound Effects
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioSurfaceElevated)
                            .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Audio Track",
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🎵 ${project.selectedMusic?.title ?: "Original Audio"} (Ducked -12dB under voice)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = EmeraldSuccess,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Sound Effects markers
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (sfx in project.soundEffects.take(4)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = ElectricViolet.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = sfx.icon,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // TRACK 3: Animated Subtitles & Captions Track
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (cap in project.captions) {
                            val capWidth = pixelsPerSec * (cap.endTimeSec - cap.startTimeSec)
                            val isCurrentCap = playbackPositionSec >= cap.startTimeSec && playbackPositionSec <= cap.endTimeSec

                            Box(
                                modifier = Modifier
                                    .width(capWidth.coerceAtLeast(50.dp))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isCurrentCap) AmberVibrant.copy(alpha = 0.85f)
                                        else StudioSurfaceBorder
                                    )
                                    .clickable { onSeek(cap.startTimeSec) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "💬 ${cap.text}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isCurrentCap) Color.Black else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Playhead Needle (Red Vertical Scrubber line across all tracks)
                val playheadOffset = (pixelsPerSec * playbackPositionSec) + 8.dp
                Box(
                    modifier = Modifier
                        .offset(x = playheadOffset)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(NeonCyan)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-2).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(NeonCyan)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Tips & AI Feature Notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💡 Tap any cut to jump • Silences removed: ${if (project.removeSilences) "ON" else "OFF"}",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Text(
                    text = "Auto-Reframe: 9:16 Active",
                    fontSize = 11.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

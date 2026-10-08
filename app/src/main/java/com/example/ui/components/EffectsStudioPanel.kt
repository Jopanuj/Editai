package com.example.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleMediaData
import com.example.model.MotionOverlayItem
import com.example.model.TimelineProject
import com.example.model.VideoFilterEffect
import com.example.ui.theme.*
import com.example.viewmodel.ShortyViewModel

@Composable
fun EffectsStudioPanel(
    project: TimelineProject,
    viewModel: ShortyViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("effects_studio_panel"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. VISUAL FILTER EFFECTS & LENS SHADERS
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
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "FX",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Visual Effects & Lens Shaders",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "ACTIVE: ${project.activeFilterEffect.badge}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyanLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Real-time canvas shaders applied onto your video playback and export.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VideoFilterEffect.entries.forEach { effect ->
                        val isSelected = project.activeFilterEffect == effect
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.updateFilterEffect(effect) }
                                .testTag("fx_effect_${effect.name}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) StudioSurface else StudioSurface.copy(alpha = 0.6f),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet))
                            ) else CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(StudioSurfaceBorder, StudioSurfaceBorder))
                            )
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
                                            text = effect.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) NeonCyanLight else TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isSelected) ElectricViolet.copy(alpha = 0.3f) else StudioSurfaceBorder
                                        ) {
                                            Text(
                                                text = effect.badge,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) AmberVibrant else TextMuted,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = effect.description,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateFilterEffect(effect) },
                                    colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. READY-TO-USE MOTION STICKERS & BADGES
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
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Stickers",
                            tint = AmberVibrant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Viral Motion Stickers & Hook Badges",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    val activeCount = project.activeOverlayItems.count { it.isEnabled }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (activeCount > 0) EmeraldSuccess.copy(alpha = 0.2f) else StudioSurfaceBorder
                    ) {
                        Text(
                            text = "$activeCount ON",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeCount > 0) EmeraldSuccess else TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Add animated eye-catcher badges to maximize first 3-second retention.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SampleMediaData.ReadyToUseOverlays.forEach { presetItem ->
                        val isItemActive = project.activeOverlayItems.any { it.id == presetItem.id && it.isEnabled }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("motion_sticker_${presetItem.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isItemActive) StudioSurface else StudioSurface.copy(alpha = 0.5f),
                            border = if (isItemActive) CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(AmberVibrant, ElectricViolet))
                            ) else CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(StudioSurfaceBorder, StudioSurfaceBorder))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isItemActive) AmberVibrant.copy(alpha = 0.2f) else StudioSurfaceElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = presetItem.emoji, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = presetItem.text,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isItemActive) AmberVibrant else TextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = StudioSurfaceElevated
                                            ) {
                                                Text(
                                                    text = presetItem.position.label,
                                                    fontSize = 8.sp,
                                                    color = NeonCyanLight,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${presetItem.subtitle} • ${presetItem.animationType.label}",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = isItemActive,
                                    onCheckedChange = { viewModel.toggleOverlayItem(presetItem) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AmberVibrant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. AUDIO WAVEFORM SPECTRUM VISUALIZER
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
            border = if (project.showAudioWaveform) CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet))
            ) else null
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
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
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Waveform",
                            tint = if (project.showAudioWaveform) NeonCyan else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Audio Spectrum Waveform",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (project.showAudioWaveform) "Bouncing speech/music visualizer active on video" else "Display animated frequency bars (Great for podcasts)",
                                fontSize = 11.sp,
                                color = if (project.showAudioWaveform) NeonCyanLight else TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = project.showAudioWaveform,
                        onCheckedChange = { viewModel.toggleAudioWaveform(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("waveform_switch")
                    )
                }

                if (project.showAudioWaveform) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val dummyHeights = listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.8f, 1f, 0.6f, 0.3f, 0.85f, 0.6f, 0.95f, 0.4f, 0.75f, 0.5f)
                            dummyHeights.forEach { factor ->
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .fillMaxHeight(factor)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(Brush.verticalGradient(listOf(NeonCyan, ElectricViolet)))
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. TRANSITION WHOOSH SOUND EFFECTS (SFX)
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "SFX",
                            tint = if (project.autoTransitionSfx) ElectricVioletLight else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Auto-Transition Sound FX",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (project.autoTransitionSfx) "Whoosh & impact sounds placed on cuts" else "Transition sound effects muted",
                                fontSize = 11.sp,
                                color = if (project.autoTransitionSfx) ElectricVioletLight else TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = project.autoTransitionSfx,
                        onCheckedChange = { viewModel.toggleAutoTransitionSfx(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricViolet
                        ),
                        modifier = Modifier.testTag("auto_sfx_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Sound Library Previews (Tap to test):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SampleMediaData.SoundEffectsLibrary.forEach { sfx ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    // Trigger quick preview
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = StudioSurface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = sfx.icon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = sfx.name, fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. MOTION SPEED RAMPING PRESETS
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Speed Ramp Pacing Deck",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Scale video clip playback speed to alter flow and dramatic tension.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val speedOptions = listOf(
                        Pair(0.5f, "0.5x Slow-Mo"),
                        Pair(1.0f, "1.0x Real-Time"),
                        Pair(1.5f, "1.5x Viral"),
                        Pair(2.0f, "2.0x Hyper")
                    )

                    speedOptions.forEach { (speed, label) ->
                        val isSelected = project.speedRampFactor == speed
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.updateSpeedRamp(speed) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) EmeraldSuccess.copy(alpha = 0.25f) else StudioSurface,
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(EmeraldSuccess, NeonCyan))
                            ) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${speed}x",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) EmeraldSuccess else TextPrimary
                                )
                                Text(
                                    text = label.substringAfter(" "),
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

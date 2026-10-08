package com.example.ui.components

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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylePickerSheet(
    initialStyle: EditStyle,
    initialRatio: AspectRatioType,
    initialPrompt: String,
    initialRemoveSilences: Boolean,
    initialEnableCaptions: Boolean = true,
    initialCaptionFontStyle: CaptionFontStyle = CaptionFontStyle.BOLD_IMPACT,
    onDismiss: () -> Unit,
    onConfirm: (EditStyle, String, AspectRatioType, Boolean, Boolean, CaptionFontStyle) -> Unit
) {
    var selectedStyle by remember { mutableStateOf(initialStyle) }
    var selectedRatio by remember { mutableStateOf(initialRatio) }
    var promptText by remember { mutableStateOf(initialPrompt) }
    var referenceLink by remember { mutableStateOf("") }
    var removeSilences by remember { mutableStateOf(initialRemoveSilences) }
    var enableCaptions by remember { mutableStateOf(initialEnableCaptions) }
    var selectedFontStyle by remember { mutableStateOf(initialCaptionFontStyle) }

    val verticalScroll = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(verticalScroll)
                .testTag("style_picker_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Customize AI Edit & Style",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Choose a viral reference style or describe your custom cut",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(EmeraldSuccess, EmeraldSuccess)))
                ) {
                    Text(
                        text = "FREE FOREVER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Aspect Ratio Selector
            Text(
                text = "Target Platform & Ratio",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AspectRatioType.entries.forEach { ratio ->
                    val isSelected = selectedRatio == ratio
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedRatio = ratio },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) ElectricViolet.copy(alpha = 0.25f) else StudioSurfaceElevated,
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NeonCyan, ElectricViolet))) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = ratio.label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeonCyan else TextPrimary
                            )
                            Text(
                                text = ratio.name.substringAfter("RATIO_"),
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Edit Styles Grid / List
            Text(
                text = "Reference Edit Styles",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SampleMediaData.EditStyles.forEach { style ->
                    val isSelected = selectedStyle.id == style.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedStyle = style
                                if (promptText.isBlank()) {
                                    promptText = style.defaultPrompt
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
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
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedStyle = style },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeonCyan,
                                    unselectedColor = TextMuted
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = style.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NeonCyanLight else TextPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = ElectricViolet.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = style.tag,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricVioletLight,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = style.description,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Optional Reference URL (Shorty's reference feature)
            Text(
                text = "Add Reference Video Link (Optional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = referenceLink,
                onValueChange = { referenceLink = it },
                placeholder = { Text("Paste TikTok, Reel or Shorts URL (e.g. instagram.com/reel/...)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Link",
                        tint = NeonCyan
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = StudioSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Custom AI Prompt / Brief
            Text(
                text = "AI Editing Prompt & Instructions",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Quick Prompt Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Pacing: <1.5s cuts",
                    "Yellow Hormozi text",
                    "Add Phonk beat",
                    "Teal & orange LUT",
                    "Under 30 seconds"
                ).forEach { chip ->
                    SuggestionChip(
                        onClick = {
                            promptText = if (promptText.isBlank()) chip else "$promptText. $chip"
                        },
                        label = { Text(text = chip, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = StudioSurfaceElevated,
                            labelColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, StudioSurfaceBorder)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                placeholder = { Text("Describe how you want Shorty to edit your footage...", fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricViolet,
                    unfocusedBorderColor = StudioSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Silences and Pauses Switch
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Strip Silences & Dead Air",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Cuts speech pauses, stutter, and breathing gaps",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = removeSilences,
                        onCheckedChange = { removeSilences = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricViolet
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Automatic Captions & Font Style Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        if (enableCaptions) listOf(AmberVibrant.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.5f))
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Automatic Animated Captions",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (enableCaptions) "Generates dynamic subtitles with active word highlights" else "Export video clean without subtitles",
                                fontSize = 11.sp,
                                color = if (enableCaptions) AmberVibrant else TextSecondary
                            )
                        }
                        Switch(
                            checked = enableCaptions,
                            onCheckedChange = { enableCaptions = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AmberVibrant
                            ),
                            modifier = Modifier.testTag("style_captions_toggle")
                        )
                    }

                    if (enableCaptions) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Subtitle Font Style",
                            fontSize = 12.sp,
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
                            CaptionFontStyle.entries.forEach { fontStyle ->
                                val isSelected = selectedFontStyle == fontStyle
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedFontStyle = fontStyle },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) ElectricViolet.copy(alpha = 0.35f) else StudioSurface,
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Button
            Button(
                onClick = {
                    onConfirm(selectedStyle, promptText, selectedRatio, removeSilences, enableCaptions, selectedFontStyle)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("apply_style_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Generate",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate AI Video Cut (100% Free)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

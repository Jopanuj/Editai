package com.example.model

enum class AspectRatioType(val label: String, val ratio: Float, val subtitle: String) {
    RATIO_9_16("9:16", 9f / 16f, "Reels • Shorts • TikTok"),
    RATIO_16_9("16:9", 16f / 9f, "YouTube • Widescreen"),
    RATIO_1_1("1:1", 1f, "Instagram Feed"),
    RATIO_4_5("4:5", 4f / 5f, "Portrait Feed")
}

data class SceneSegment(
    val id: String,
    val startTimeSec: Float,
    val endTimeSec: Float,
    val label: String,
    val isSilence: Boolean = false,
    val importanceScore: Int = 80
)

data class RawVideoClip(
    val id: String,
    val title: String,
    val durationSec: Float,
    val sourceUri: String? = null,
    val previewImageUrl: String? = null,
    val category: String,
    val resolution: String = "1080p 60fps",
    val dimensions: String = "1920 x 1080",
    val fileSizeMb: Float = 68.4f,
    val frameRate: Int = 60,
    val codec: String = "H.264 / AAC",
    val bitrateMbps: Float = 24.5f,
    val silencePercentage: Int = 22,
    val detectedFaces: Int = 1,
    val energyScore: Int = 88,
    val gradientColors: List<Long> = listOf(0xFF3B82F6, 0xFF8B5CF6),
    val detectedScenes: List<SceneSegment> = emptyList(),
    val trimStartSec: Float = 0f,
    val trimEndSec: Float? = null
) {
    val effectiveTrimEnd: Float get() = trimEndSec ?: durationSec
    val trimmedDuration: Float get() = (effectiveTrimEnd - trimStartSec).coerceAtLeast(0.5f)
}

data class CaptionWord(
    val word: String,
    val startMs: Long,
    val endMs: Long,
    val highlight: Boolean = false,
    val emoji: String? = null
)

data class CaptionPhrase(
    val id: String,
    val startTimeSec: Float,
    val endTimeSec: Float,
    val text: String,
    val words: List<CaptionWord> = emptyList()
)

enum class CaptionPreset(val displayName: String, val fontStyle: String, val highlightColorHex: Long) {
    HORMOZI("Viral Hormozi", "Bold Knockout", 0xFFFACC15), // Neon yellow
    NEON_PULSE("Cyber Glow", "Italic Futuristic", 0xFF06B6D4), // Cyan
    MINIMAL_STUDIO("Clean Studio", "Modern Sans", 0xFFFFFFFF),
    BEAST_MODE("High Energy", "Impact Comic", 0xFF10B981) // Green
}

enum class CaptionFontStyle(
    val id: String,
    val displayName: String,
    val description: String,
    val sampleText: String
) {
    BOLD_IMPACT("font_impact", "Bold Impact", "Heavyweight viral knockout style", "STOP SCROLLING 🔥"),
    CLEAN_SANS("font_sans", "Clean Modern", "Crisp minimalist sans-serif", "Crisp & Modern ✨"),
    TECH_MONO("font_mono", "Cyber Mono", "Futuristic terminal code style", "SYSTEM.ONLINE ⚡"),
    ELEGANT_SERIF("font_serif", "Classic Serif", "Editorial luxury serif aesthetic", "Timeless Luxury 🏛️"),
    PLAYFUL_COMIC("font_comic", "Comic Punch", "Bubbly dynamic casual lettering", "WAIT FOR IT! 💥")
}

enum class ColorGradePreset(val displayName: String, val description: String) {
    NATURAL("Natural Raw", "Unfiltered original tones"),
    VIBRANT_POP("Vibrant Social", "High saturation & rich punch"),
    TEAL_ORANGE("Cinematic Gold", "Blockbuster teal shadows & warm skin"),
    MOODY_NOIR("Dark Luxury", "High contrast dramatic shadow"),
    GOLDEN_HOUR("Sunset Warmth", "Lush golden highlights")
}

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val genre: String,
    val bpm: Int,
    val durationSec: Float,
    val energyLevel: String
)

data class SoundEffect(
    val id: String,
    val name: String,
    val timestampSec: Float,
    val icon: String = "🔊"
)

data class EditStyle(
    val id: String,
    val name: String,
    val description: String,
    val tag: String,
    val targetPlatform: String,
    val cutPacingSec: Float,
    val captionPreset: CaptionPreset,
    val colorGrade: ColorGradePreset,
    val defaultPrompt: String,
    val iconKey: String = "flash"
)

data class CutTimelineItem(
    val id: String,
    val clipId: String,
    val clipTitle: String,
    val sourceStartSec: Float,
    val sourceEndSec: Float,
    val timelineStartSec: Float,
    val durationSec: Float,
    val label: String,
    val isHook: Boolean = false,
    val zoomFactor: Float = 1.0f,
    val panOffset: Float = 0f
)

data class TimelineProject(
    val id: String,
    val title: String,
    val rawClips: List<RawVideoClip>,
    val aspectRatio: AspectRatioType = AspectRatioType.RATIO_9_16,
    val style: EditStyle,
    val totalDurationSec: Float,
    val cuts: List<CutTimelineItem>,
    val captions: List<CaptionPhrase>,
    val enableCaptions: Boolean = true,
    val captionFontStyle: CaptionFontStyle = CaptionFontStyle.BOLD_IMPACT,
    val selectedMusic: MusicTrack?,
    val musicVolume: Float = 0.35f,
    val soundEffects: List<SoundEffect> = emptyList(),
    val colorGrade: ColorGradePreset = ColorGradePreset.VIBRANT_POP,
    val activeFilterEffect: VideoFilterEffect = VideoFilterEffect.NONE,
    val activeOverlayItems: List<MotionOverlayItem> = emptyList(),
    val showAudioWaveform: Boolean = false,
    val autoTransitionSfx: Boolean = true,
    val speedRampFactor: Float = 1.0f,
    val aiPromptBrief: String = "",
    val removeSilences: Boolean = true,
    val autoReframe: Boolean = true,
    val revisionHistory: List<String> = emptyList(),
    val isRendered: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis()
)

enum class VideoFilterEffect(
    val id: String,
    val displayName: String,
    val description: String,
    val badge: String
) {
    NONE("fx_none", "Clean Raw", "Natural untouched video clarity", "CLEAN"),
    VHS_GLITCH("fx_vhs", "VHS Retro Glitch", "CRT scanlines, tape noise & RGB edge displacement", "RETRO 📼"),
    RGB_SPLIT("fx_rgb", "RGB Hologram", "Chromatic aberration & futuristic prism split", "HOLO ⚡"),
    FILM_GRAIN("fx_grain", "35mm Film Grain", "Warm vintage cinematic grain & analogue warmth", "FILM 🎞️"),
    NEON_CYBER("fx_neon", "Cyber Neon Glow", "Vibrant edge bloom & stylized high-contrast glow", "CYBER 🌟"),
    FLASH_STROBE("fx_flash", "Impact Strobe Flash", "Dramatic lightning flash at key cut points", "PUNCH ⚡")
}

data class MotionOverlayItem(
    val id: String,
    val text: String,
    val emoji: String,
    val subtitle: String,
    val position: OverlayPosition = OverlayPosition.TOP_CENTER,
    val animationType: OverlayAnimation = OverlayAnimation.PULSE,
    val isEnabled: Boolean = true
)

enum class OverlayPosition(val label: String) {
    TOP_CENTER("Top Hook"),
    MIDDLE_SCREEN("Center Focus"),
    LOWER_THIRD("Lower Third")
}

enum class OverlayAnimation(val label: String) {
    PULSE("Gentle Pulse"),
    GLITCH_BOUNCE("Glitch Bounce"),
    NEON_FLICKER("Neon Flicker"),
    SLIDE_IN("Slide In")
}

enum class RenderQuality(val label: String, val resolution: String, val estSizeMb: Int) {
    SHARE_720P("720p HD", "720 x 1280", 12),
    REELS_1080P("1080p Full HD (Recommended)", "1080 x 1920", 38),
    ULTRA_4K("4K Ultra HD", "2160 x 3840", 115)
}

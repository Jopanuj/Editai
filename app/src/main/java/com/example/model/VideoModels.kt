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

enum class VisualEffectPreset(val id: String, val displayName: String, val description: String, val badge: String) {
    NONE("fx_none", "Clean Raw", "Original sharp raw footage without filters", "CLEAN"),
    FILM_GRAIN("fx_grain", "35mm Film Grain", "Textured organic analog film grain", "GRAIN"),
    CYBER_GLITCH("fx_glitch", "RGB Glitch Shift", "Chromatic aberration cyber glitch slices", "GLITCH"),
    LIGHT_LEAK("fx_leak", "Golden Sun Flare", "Warm cinematic optical lens leaks", "FLARE"),
    FLASH_IMPACT("fx_flash", "Whiteout Strobe", "High-energy beat drop impact flashes", "FLASH"),
    VHS_RETRO("fx_vhs", "90s Camcorder CRT", "Analog CRT scanlines & retro timestamp", "VHS")
}

enum class TransitionStyle(val id: String, val displayName: String, val description: String, val icon: String) {
    HARD_CUT("tr_hard", "Instant Cut", "Zero ms fast retention cut", "✂️"),
    WHIP_PAN("tr_whip", "Whip Pan", "Directional horizontal whoosh blur", "💨"),
    WARP_ZOOM("tr_zoom", "Warp Zoom", "Kinetic punch-in forward dive", "⚡"),
    FLASH_WHITE("tr_flash", "Impact Flash", "Explosive white strobe transition", "💥"),
    GLITCH_SLICE("tr_glitch", "Cyber Glitch", "Digital cybernetic slice glitch", "👾")
}

data class AnimatedSticker(
    val id: String,
    val emoji: String,
    val label: String,
    val timestampSec: Float,
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.28f,
    val durationSec: Float = 2.5f
)

data class CreatorToolPreset(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String,
    val category: String,
    val isFree: Boolean = true
)

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
    val activeEffect: VisualEffectPreset = VisualEffectPreset.NONE,
    val transitionStyle: TransitionStyle = TransitionStyle.WHIP_PAN,
    val stickers: List<AnimatedSticker> = emptyList(),
    val voiceEnhanceEnabled: Boolean = true,
    val aiPromptBrief: String = "",
    val removeSilences: Boolean = true,
    val autoReframe: Boolean = true,
    val revisionHistory: List<String> = emptyList(),
    val isRendered: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis()
)

enum class RenderQuality(val label: String, val resolution: String, val estSizeMb: Int) {
    SHARE_720P("720p HD", "720 x 1280", 12),
    REELS_1080P("1080p Full HD (Recommended)", "1080 x 1920", 38),
    ULTRA_4K("4K Ultra HD", "2160 x 3840", 115)
}

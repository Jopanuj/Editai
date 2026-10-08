package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleMediaData
import com.example.data.ShortyEditEngine
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

enum class EditorTab(val label: String, val icon: String) {
    STUDIO("Studio", "play"),
    TIMELINE("Timeline", "view_timeline"),
    AI_PROMPT("AI Brief", "auto_awesome"),
    STYLE("Style & Grade", "palette"),
    EFFECTS("FX & Assets", "wand"),
    AUDIO("Audio & SFX", "music_note")
}

data class UiNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isSuccess: Boolean = true
)

class ShortyViewModel : ViewModel() {

    // Available clips (samples + user imported)
    private val _availableClips = MutableStateFlow<List<RawVideoClip>>(SampleMediaData.SampleClips)
    val availableClips: StateFlow<List<RawVideoClip>> = _availableClips.asStateFlow()

    // Currently selected raw footage clips to be edited
    private val _selectedClips = MutableStateFlow<List<RawVideoClip>>(listOf(SampleMediaData.SampleClips.first()))
    val selectedClips: StateFlow<List<RawVideoClip>> = _selectedClips.asStateFlow()

    // Active project under editing
    private val _currentProject = MutableStateFlow<TimelineProject?>(null)
    val currentProject: StateFlow<TimelineProject?> = _currentProject.asStateFlow()

    // Playback state
    private val _playbackPositionSec = MutableStateFlow(0f)
    val playbackPositionSec: StateFlow<Float> = _playbackPositionSec.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _activeTab = MutableStateFlow(EditorTab.STUDIO)
    val activeTab: StateFlow<EditorTab> = _activeTab.asStateFlow()

    // AI Generation progress
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationStage = MutableStateFlow("")
    val generationStage: StateFlow<String> = _generationStage.asStateFlow()

    // Export & Render state
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    private val _exportPipelineStage = MutableStateFlow("Initializing pipeline...")
    val exportPipelineStage: StateFlow<String> = _exportPipelineStage.asStateFlow()

    private val _exportSuccess = MutableStateFlow(false)
    val exportSuccess: StateFlow<Boolean> = _exportSuccess.asStateFlow()

    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    // Saved projects library
    private val _savedProjects = MutableStateFlow<List<TimelineProject>>(emptyList())
    val savedProjects: StateFlow<List<TimelineProject>> = _savedProjects.asStateFlow()

    private var playbackJob: Job? = null

    init {
        // Initialize with default Hormozi edit of first sample clip for instant preview
        createInitialDraft()
    }

    private fun createInitialDraft() {
        val initialClip = SampleMediaData.SampleClips.first()
        val defaultStyle = SampleMediaData.EditStyles.first()
        val project = ShortyEditEngine.generateEditPlan(
            rawClips = listOf(initialClip),
            style = defaultStyle,
            customPrompt = "",
            aspectRatio = AspectRatioType.RATIO_9_16,
            removeSilences = true
        )
        _currentProject.value = project
    }

    fun toggleClipSelection(clip: RawVideoClip) {
        val current = _selectedClips.value.toMutableList()
        if (current.any { it.id == clip.id }) {
            if (current.size > 1) { // Keep at least one clip selected
                current.removeAll { it.id == clip.id }
            } else {
                showToast("Keep at least 1 clip selected to edit", false)
            }
        } else {
            current.add(clip)
        }
        _selectedClips.value = current
    }

    fun updateClipTrim(clipId: String, startSec: Float, endSec: Float) {
        val updatedAvailable = _availableClips.value.map { clip ->
            if (clip.id == clipId) {
                clip.copy(trimStartSec = startSec, trimEndSec = endSec)
            } else clip
        }
        _availableClips.value = updatedAvailable
        val updatedSelected = _selectedClips.value.map { clip ->
            if (clip.id == clipId) {
                clip.copy(trimStartSec = startSec, trimEndSec = endSec)
            } else clip
        }
        _selectedClips.value = updatedSelected
    }

    fun addCustomUserClip(title: String, uriString: String, durationSec: Float = 35.0f) {
        val estimatedSizeMb = (durationSec * 3.2f).coerceAtLeast(14.5f)
        val newClip = RawVideoClip(
            id = "user_clip_${System.currentTimeMillis()}",
            title = title,
            durationSec = max(5.0f, durationSec),
            sourceUri = uriString,
            category = "Imported Raw",
            resolution = "1080p 60fps",
            dimensions = "1920 x 1080",
            fileSizeMb = (estimatedSizeMb * 10).toInt() / 10f,
            frameRate = 60,
            codec = "H.264 / AAC",
            bitrateMbps = 25.6f,
            silencePercentage = 20,
            detectedFaces = 1,
            energyScore = 90,
            gradientColors = listOf(0xFF8B5CF6, 0xFFEC4899),
            detectedScenes = listOf(
                SceneSegment("us1", 0f, 4.0f, "Imported Hook", false, 95),
                SceneSegment("us2", 4.0f, min(14.0f, durationSec), "Main Sequence", false, 90),
                SceneSegment("us3", min(14.0f, durationSec), durationSec, "Ending Shot", false, 85)
            )
        )
        _availableClips.value = listOf(newClip) + _availableClips.value
        _selectedClips.value = listOf(newClip)
        showToast("Imported \"$title\" ready for AI editing!", true)
    }

    fun generateNewEdit(
        style: EditStyle,
        customPrompt: String,
        aspectRatio: AspectRatioType,
        removeSilences: Boolean,
        enableCaptions: Boolean = true,
        captionFontStyle: CaptionFontStyle = CaptionFontStyle.BOLD_IMPACT
    ) {
        viewModelScope.launch {
            _isGenerating.value = true
            stopPlayback()

            val stages = listOf(
                "Analyzing ${selectedClips.value.size} raw clip(s) for scene energy & faces...",
                "Removing speech silences, dead-air & pauses...",
                "Synthesizing ${style.name} cut rhythm and pacing...",
                "Auto-reframing to ${aspectRatio.label} portrait safe-zone...",
                if (enableCaptions) "Generating dynamic animated captions (${captionFontStyle.displayName})..." else "Skipping caption generation (disabled by user)...",
                "Mastering audio ducking & SFX whoosh placement..."
            )

            for (stage in stages) {
                _generationStage.value = stage
                delay(380)
            }

            val newProject = ShortyEditEngine.generateEditPlan(
                rawClips = _selectedClips.value,
                style = style,
                customPrompt = customPrompt,
                aspectRatio = aspectRatio,
                removeSilences = removeSilences,
                enableCaptions = enableCaptions,
                captionFontStyle = captionFontStyle
            )

            _currentProject.value = newProject
            _playbackPositionSec.value = 0f
            _isGenerating.value = false
            showToast("Cut ready! ${newProject.cuts.size} clips assembled in ${aspectRatio.label}", true)
        }
    }

    fun submitAiPromptRevision(promptText: String) {
        val proj = _currentProject.value ?: return
        if (promptText.isBlank()) return

        viewModelScope.launch {
            _isGenerating.value = true
            _generationStage.value = "Applying AI revision: \"$promptText\"..."
            delay(500)

            val (revised, log) = ShortyEditEngine.applyPromptRevision(proj, promptText)
            _currentProject.value = revised
            _playbackPositionSec.value = 0f
            _isGenerating.value = false
            showToast(log.lines().firstOrNull() ?: "Timeline updated!", true)
        }
    }

    fun seekTo(seconds: Float) {
        val maxDuration = _currentProject.value?.totalDurationSec ?: 10f
        _playbackPositionSec.value = seconds.coerceIn(0f, maxDuration)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    fun setActiveTab(tab: EditorTab) {
        _activeTab.value = tab
    }

    private fun startPlayback() {
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val tickMs = 50L
            while (_isPlaying.value) {
                val proj = _currentProject.value
                val maxDur = proj?.totalDurationSec ?: 10f
                val deltaSec = (tickMs / 1000f) * _playbackSpeed.value

                val nextPos = _playbackPositionSec.value + deltaSec
                if (nextPos >= maxDur) {
                    _playbackPositionSec.value = 0f // Loop back to start
                } else {
                    _playbackPositionSec.value = nextPos
                }
                delay(tickMs)
            }
        }
    }

    private fun stopPlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun updateAspectRatio(aspectRatio: AspectRatioType) {
        _currentProject.value = _currentProject.value?.copy(aspectRatio = aspectRatio)
        showToast("Switched ratio to ${aspectRatio.label} (${aspectRatio.subtitle})", true)
    }

    fun updateCaptionPreset(preset: CaptionPreset) {
        val currentStyle = _currentProject.value?.style ?: return
        _currentProject.value = _currentProject.value?.copy(
            style = currentStyle.copy(captionPreset = preset)
        )
        showToast("Caption style updated to ${preset.displayName}", true)
    }

    fun toggleCaptionsEnabled(enabled: Boolean) {
        val proj = _currentProject.value ?: return
        val updatedCaptions = if (enabled && proj.captions.isEmpty()) {
            ShortyEditEngine.generateEditPlan(
                rawClips = proj.rawClips,
                style = proj.style,
                aspectRatio = proj.aspectRatio,
                enableCaptions = true,
                captionFontStyle = proj.captionFontStyle
            ).captions
        } else proj.captions

        _currentProject.value = proj.copy(
            enableCaptions = enabled,
            captions = updatedCaptions
        )
        showToast(if (enabled) "Captions enabled for export" else "Captions disabled for export", true)
    }

    fun updateCaptionFontStyle(fontStyle: CaptionFontStyle) {
        _currentProject.value = _currentProject.value?.copy(
            captionFontStyle = fontStyle
        )
        showToast("Caption Font: ${fontStyle.displayName}", true)
    }

    fun updateColorGrade(grade: ColorGradePreset) {
        _currentProject.value = _currentProject.value?.copy(colorGrade = grade)
        showToast("Color grade: ${grade.displayName}", true)
    }

    fun updateVisualEffect(effect: VisualEffectPreset) {
        _currentProject.value = _currentProject.value?.copy(activeEffect = effect)
        showToast("Applied FX: ${effect.displayName}", true)
    }

    fun updateTransitionStyle(transition: TransitionStyle) {
        _currentProject.value = _currentProject.value?.copy(transitionStyle = transition)
        showToast("Transition: ${transition.displayName}", true)
    }

    fun addSticker(emoji: String, label: String) {
        val proj = _currentProject.value ?: return
        val currentPlayhead = _playbackPositionSec.value
        val newSticker = AnimatedSticker(
            id = "stk_${System.currentTimeMillis()}",
            emoji = emoji,
            label = label,
            timestampSec = currentPlayhead,
            xFraction = 0.5f,
            yFraction = 0.25f
        )
        _currentProject.value = proj.copy(stickers = proj.stickers + listOf(newSticker))
        showToast("Added $emoji $label at ${(currentPlayhead * 10).toInt() / 10f}s!", true)
    }

    fun removeSticker(stickerId: String) {
        val proj = _currentProject.value ?: return
        _currentProject.value = proj.copy(stickers = proj.stickers.filterNot { it.id == stickerId })
        showToast("Removed sticker asset", true)
    }

    fun toggleVoiceEnhancer(enabled: Boolean) {
        _currentProject.value = _currentProject.value?.copy(voiceEnhanceEnabled = enabled)
        showToast(if (enabled) "🎙️ AI Voice Enhancer ON (Studio broadcast clarity)" else "Voice Enhancer OFF", true)
    }

    fun applyCreatorToolPreset(toolId: String) {
        val proj = _currentProject.value ?: return
        when (toolId) {
            "tool_voice_enhancer" -> {
                _currentProject.value = proj.copy(voiceEnhanceEnabled = true)
                showToast("🎙️ Voice Enhancer: Cleaned reverb & balanced voice levels", true)
            }
            "tool_b_roll" -> {
                val enhancedCuts = proj.cuts.mapIndexed { idx, cut ->
                    if (idx % 2 == 1) cut.copy(label = "🎬 Cinematic B-Roll Insert", zoomFactor = 1.2f) else cut
                }
                _currentProject.value = proj.copy(cuts = enhancedCuts)
                showToast("🎞️ Wove in dynamic B-Roll cutaways to break up monologue", true)
            }
            "tool_hook_generator" -> {
                val hookCuts = proj.cuts.mapIndexed { idx, cut ->
                    if (idx == 0) cut.copy(label = "⚡ Curiosity Hook Variant", zoomFactor = 1.3f) else cut
                }
                _currentProject.value = proj.copy(cuts = hookCuts)
                showToast("🎯 Applied high-converting 3-second opening hook!", true)
            }
            "tool_silence_stripper" -> {
                val tightCuts = proj.cuts.map { cut -> cut.copy(durationSec = (cut.durationSec * 0.85f).coerceAtLeast(0.8f)) }
                _currentProject.value = proj.copy(cuts = tightCuts, removeSilences = true)
                showToast("✂️ Stripped dead air & breath gaps for faster retention pace", true)
            }
            "tool_fx_lab" -> {
                val fireSticker = AnimatedSticker("stk_fire", "🔥", "Hype Hit", 0.5f, 0.5f, 0.22f)
                _currentProject.value = proj.copy(
                    activeEffect = VisualEffectPreset.FILM_GRAIN,
                    stickers = proj.stickers + listOf(fireSticker)
                )
                showToast("🎨 Added 35mm Film Grain texture & animated 🔥 sticker!", true)
            }
            "tool_reframe_hub" -> {
                _currentProject.value = proj.copy(aspectRatio = AspectRatioType.RATIO_9_16, autoReframe = true)
                showToast("📱 Auto-reframed safe-zone for TikTok, Shorts & Reels", true)
            }
            "tool_emoji_highlighter" -> {
                val stickersList = listOf(
                    AnimatedSticker("stk_fire", "🔥", "Hype Hit", 0.5f, 0.5f, 0.22f),
                    AnimatedSticker("stk_rocket", "🚀", "Moon Launch", 2.5f, 0.5f, 0.22f),
                    AnimatedSticker("stk_100", "💯", "Pure Value", 5.0f, 0.5f, 0.22f)
                )
                _currentProject.value = proj.copy(stickers = proj.stickers + stickersList)
                showToast("✨ Injected kinetic animated stickers at key moments!", true)
            }
            "tool_punch_zooms" -> {
                val zoomedCuts = proj.cuts.mapIndexed { idx, cut ->
                    val factor = if (idx % 2 == 0) 1.25f else 1.0f
                    cut.copy(zoomFactor = factor)
                }
                _currentProject.value = proj.copy(cuts = zoomedCuts)
                showToast("⚡ Applied alternating 1.25x kinetic punch-in retention zooms!", true)
            }
            "tool_bass_boost" -> {
                _currentProject.value = proj.copy(musicVolume = 0.45f, voiceEnhanceEnabled = true)
                showToast("🔊 Mastered dynamic range with punchy bass limiter & clarity", true)
            }
            "tool_speed_ramp" -> {
                _playbackSpeed.value = 1.15f
                val rampedCuts = proj.cuts.map { cut -> cut.copy(durationSec = (cut.durationSec / 1.15f).coerceAtLeast(0.6f)) }
                _currentProject.value = proj.copy(cuts = rampedCuts)
                showToast("⏩ Pacing boosted to 1.15x speed ramp for viral retention!", true)
            }
        }
    }

    fun updateMusic(track: MusicTrack?) {
        _currentProject.value = _currentProject.value?.copy(selectedMusic = track)
        showToast(if (track != null) "Soundtrack: ${track.title}" else "Music muted", true)
    }

    fun updateMusicVolume(volume: Float) {
        _currentProject.value = _currentProject.value?.copy(musicVolume = volume.coerceIn(0f, 1f))
    }

    fun startExport(quality: RenderQuality) {
        val proj = _currentProject.value ?: return
        stopPlayback()
        viewModelScope.launch {
            _isExporting.value = true
            _exportSuccess.value = false
            _exportProgress.value = 0f

            val pipelineStages = listOf(
                Pair(0.20f, "Stage 1/5: Extracting & Pre-processing Cut Streams..."),
                Pair(0.40f, "Stage 2/5: Applying 9:16 Subject AI Safe-Zone Auto-Reframe..."),
                Pair(0.60f, if (proj.enableCaptions) "Stage 3/5: Subtitle Burn-In (${proj.captionFontStyle.displayName})..." else "Stage 3/5: Video Color Grading & Filter Pass..."),
                Pair(0.80f, "Stage 4/5: Audio Mastering & SFX Ducking..."),
                Pair(1.00f, "Stage 5/5: GPU Hardware Encoding ${quality.label} MP4...")
            )

            for ((targetProgress, stageDesc) in pipelineStages) {
                _exportPipelineStage.value = stageDesc
                val startProgress = _exportProgress.value
                val steps = 8
                for (s in 1..steps) {
                    delay(60)
                    _exportProgress.value = startProgress + (targetProgress - startProgress) * (s / steps.toFloat())
                }
            }

            _exportSuccess.value = true
            _isExporting.value = false
            val rendered = proj.copy(isRendered = true)
            _currentProject.value = rendered
            _savedProjects.value = listOf(rendered) + _savedProjects.value
            showToast("Render complete! Ready to save & share for FREE ($0 forever)", true)
        }
    }

    fun dismissExport() {
        _isExporting.value = false
        _exportSuccess.value = false
        _exportProgress.value = 0f
    }

    fun clearNotification() {
        _notification.value = null
    }

    private fun showToast(msg: String, isSuccess: Boolean) {
        _notification.value = UiNotification(message = msg, isSuccess = isSuccess)
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}

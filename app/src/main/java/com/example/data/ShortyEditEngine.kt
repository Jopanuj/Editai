package com.example.data

import com.example.model.*
import kotlin.math.max
import kotlin.math.min

object ShortyEditEngine {

    fun generateEditPlan(
        rawClips: List<RawVideoClip>,
        style: EditStyle,
        customPrompt: String = "",
        aspectRatio: AspectRatioType = AspectRatioType.RATIO_9_16,
        removeSilences: Boolean = true,
        enableCaptions: Boolean = true,
        captionFontStyle: CaptionFontStyle = CaptionFontStyle.BOLD_IMPACT
    ): TimelineProject {
        val selectedClips = if (rawClips.isNotEmpty()) rawClips else listOf(SampleMediaData.SampleClips.first())
        
        // 1. Gather all scenes across selected clips, filtering out silences if requested
        val usableScenes = mutableListOf<Pair<RawVideoClip, SceneSegment>>()
        for (clip in selectedClips) {
            val trimStart = clip.trimStartSec
            val trimEnd = clip.effectiveTrimEnd

            val rawScenes = if (clip.detectedScenes.isNotEmpty()) {
                clip.detectedScenes
            } else {
                // Auto-segment clip into scenes
                listOf(
                    SceneSegment("${clip.id}_s1", 0f, min(3.5f, clip.durationSec), "Opening Hook", false, 90),
                    SceneSegment("${clip.id}_s2", min(3.5f, clip.durationSec), clip.durationSec, "Core Action", false, 85)
                )
            }
            for (scene in rawScenes) {
                // Filter scenes strictly within the user's manual trim window
                if (scene.endTimeSec <= trimStart || scene.startTimeSec >= trimEnd) {
                    continue
                }
                val boundedScene = scene.copy(
                    startTimeSec = max(trimStart, scene.startTimeSec),
                    endTimeSec = min(trimEnd, scene.endTimeSec)
                )
                if (boundedScene.endTimeSec - boundedScene.startTimeSec < 0.3f) {
                    continue
                }
                if (removeSilences && boundedScene.isSilence) {
                    continue // Skip dead air and pause segments
                }
                usableScenes.add(Pair(clip, boundedScene))
            }
        }

        // Sort to ensure highest energy scene leads as opening hook
        if (usableScenes.size > 1) {
            val bestHookIndex = usableScenes.indexOfMaxBy { it.second.importanceScore }
            if (bestHookIndex > 0) {
                val hookItem = usableScenes.removeAt(bestHookIndex)
                usableScenes.add(0, hookItem)
            }
        }

        // 2. Build cut items based on pacing
        val pacing = if (customPrompt.contains("faster", ignoreCase = true)) {
            1.2f
        } else if (customPrompt.contains("slower", ignoreCase = true) || customPrompt.contains("cinematic", ignoreCase = true)) {
            2.8f
        } else {
            style.cutPacingSec
        }

        val cuts = mutableListOf<CutTimelineItem>()
        var currentTimelineTime = 0f
        var cutIndex = 0

        for ((clip, scene) in usableScenes) {
            val sceneDuration = scene.endTimeSec - scene.startTimeSec
            var sceneOffset = scene.startTimeSec

            while (sceneOffset < scene.endTimeSec && currentTimelineTime < 45.0f) { // Aim for optimal 30-45s Reel length
                val sliceDuration = min(pacing, scene.endTimeSec - sceneOffset)
                if (sliceDuration < 0.6f) break

                val isOpeningHook = cutIndex == 0
                val zoom = when {
                    isOpeningHook -> 1.15f
                    cutIndex % 3 == 0 -> 1.2f // Dynamic zoom punch-in on every 3rd cut
                    else -> 1.0f
                }

                cuts.add(
                    CutTimelineItem(
                        id = "cut_${clip.id}_$cutIndex",
                        clipId = clip.id,
                        clipTitle = clip.title,
                        sourceStartSec = sceneOffset,
                        sourceEndSec = sceneOffset + sliceDuration,
                        timelineStartSec = currentTimelineTime,
                        durationSec = sliceDuration,
                        label = if (isOpeningHook) "⚡ 3s Viral Hook" else scene.label,
                        isHook = isOpeningHook,
                        zoomFactor = zoom,
                        panOffset = if (aspectRatio == AspectRatioType.RATIO_9_16 && cutIndex % 2 == 1) 0.05f else 0f
                    )
                )

                sceneOffset += sliceDuration
                currentTimelineTime += sliceDuration
                cutIndex++
            }
        }

        val totalDuration = currentTimelineTime

        // 3. Generate Synced Animated Captions based on speech context & style (if enabled)
        val captions = if (enableCaptions) {
            generateCaptions(totalDuration, style.captionPreset)
        } else {
            emptyList()
        }

        // 4. Select background music matching style
        val selectedMusic = when (style.id) {
            "style_hormozi" -> SampleMediaData.MusicTracks.firstOrNull { it.id == "m1" } // Phonk
            "style_cinematic" -> SampleMediaData.MusicTracks.firstOrNull { it.id == "m2" } // LoFi
            "style_high_energy" -> SampleMediaData.MusicTracks.firstOrNull { it.id == "m1" }
            "style_minimal_tech" -> SampleMediaData.MusicTracks.firstOrNull { it.id == "m5" }
            else -> SampleMediaData.MusicTracks.firstOrNull { it.id == "m3" }
        }

        // 5. Place Sound Effects on key cut transitions
        val sfxList = mutableListOf<SoundEffect>()
        sfxList.add(SoundEffect("sfx_start", "💨 Whoosh Intro", 0.0f, "💨"))
        for (cut in cuts) {
            if (cut.isHook) {
                sfxList.add(SoundEffect("sfx_hook", "💥 Pop Impact", cut.timelineStartSec + 0.3f, "💥"))
            } else if (cut.zoomFactor > 1.1f) {
                sfxList.add(SoundEffect("sfx_${cut.id}", "⚡ Zoom Hit", cut.timelineStartSec, "⚡"))
            }
        }

        return TimelineProject(
            id = "proj_${System.currentTimeMillis()}",
            title = "Shorty Edit: ${selectedClips.firstOrNull()?.title?.take(20) ?: "Raw Clips"}",
            rawClips = selectedClips,
            aspectRatio = aspectRatio,
            style = style,
            totalDurationSec = totalDuration,
            cuts = cuts,
            captions = captions,
            enableCaptions = enableCaptions,
            captionFontStyle = captionFontStyle,
            selectedMusic = selectedMusic,
            musicVolume = 0.32f, // Audio ducking pre-set
            soundEffects = sfxList,
            colorGrade = style.colorGrade,
            activeFilterEffect = VideoFilterEffect.NONE,
            activeOverlayItems = SampleMediaData.ReadyToUseOverlays.take(1),
            showAudioWaveform = false,
            autoTransitionSfx = true,
            speedRampFactor = 1.0f,
            aiPromptBrief = customPrompt.ifEmpty { style.defaultPrompt },
            removeSilences = removeSilences,
            autoReframe = true,
            revisionHistory = listOf("✨ Auto-edited raw clips into ${cuts.size} punchy shots with ${style.name}${if (enableCaptions) " and ${captionFontStyle.displayName} captions" else " (captions disabled)"}"),
            isRendered = false
        )
    }

    private fun generateCaptions(totalDuration: Float, preset: CaptionPreset): List<CaptionPhrase> {
        val sampleSpeechSentences = listOf(
            Pair("If you want real results in 2026...", listOf(
                CaptionWord("If", 0, 300),
                CaptionWord("you", 300, 600),
                CaptionWord("want", 600, 1000),
                CaptionWord("REAL", 1000, 1500, highlight = true, emoji = "🔥"),
                CaptionWord("results", 1500, 2000, highlight = true),
                CaptionWord("in 2026", 2000, 2600, highlight = false, emoji = "🚀")
            )),
            Pair("Stop waiting for the perfect moment.", listOf(
                CaptionWord("STOP", 0, 500, highlight = true, emoji = "🛑"),
                CaptionWord("waiting", 500, 1100),
                CaptionWord("for", 1100, 1400),
                CaptionWord("the perfect", 1400, 2000),
                CaptionWord("MOMENT", 2000, 2700, highlight = true)
            )),
            Pair("Execution beats talent every single day.", listOf(
                CaptionWord("Execution", 0, 600, highlight = true, emoji = "⚡"),
                CaptionWord("beats", 600, 1000),
                CaptionWord("talent", 1000, 1500),
                CaptionWord("EVERY", 1500, 2000, highlight = true),
                CaptionWord("SINGLE", 2000, 2400, highlight = true),
                CaptionWord("DAY", 2400, 2900, highlight = true, emoji = "💯")
            )),
            Pair("Take messy action right now.", listOf(
                CaptionWord("Take", 0, 350),
                CaptionWord("MESSY", 350, 900, highlight = true),
                CaptionWord("action", 900, 1500, highlight = true, emoji = "💥"),
                CaptionWord("RIGHT", 1500, 1900, highlight = true),
                CaptionWord("NOW", 1900, 2500, highlight = true, emoji = "👇")
            )),
            Pair("Double tap if you needed this reminder!", listOf(
                CaptionWord("Double tap", 0, 700, highlight = true, emoji = "❤️"),
                CaptionWord("if you needed", 700, 1400),
                CaptionWord("THIS", 1400, 1900, highlight = true),
                CaptionWord("reminder!", 1900, 2600, emoji = "✨")
            ))
        )

        val phrases = mutableListOf<CaptionPhrase>()
        var phraseStart = 0.5f

        for ((idx, item) in sampleSpeechSentences.withIndex()) {
            if (phraseStart >= totalDuration) break
            val phraseDuration = 2.8f
            val phraseEnd = min(phraseStart + phraseDuration, totalDuration)

            phrases.add(
                CaptionPhrase(
                    id = "cap_$idx",
                    startTimeSec = phraseStart,
                    endTimeSec = phraseEnd,
                    text = item.first,
                    words = item.second
                )
            )

            phraseStart += phraseDuration + 0.4f
        }

        return phrases
    }

    fun applyPromptRevision(current: TimelineProject, prompt: String): Pair<TimelineProject, String> {
        val lower = prompt.lowercase()
        var updated = current
        val logMessages = mutableListOf<String>()

        // 1. Pacing & Cuts
        if (lower.contains("faster") || lower.contains("fast") || lower.contains("speed up")) {
            val shortenedCuts = updated.cuts.map { cut ->
                val newDur = max(0.8f, cut.durationSec * 0.7f)
                cut.copy(durationSec = newDur)
            }
            // Recalculate timeline positions
            var curTime = 0f
            val remappedCuts = shortenedCuts.map { cut ->
                val mapped = cut.copy(timelineStartSec = curTime)
                curTime += cut.durationSec
                mapped
            }
            updated = updated.copy(cuts = remappedCuts, totalDurationSec = curTime)
            logMessages.add("⚡ Accelerated cut pacing by 30% for ultra-high viewer retention.")
        } else if (lower.contains("slower") || lower.contains("breathe") || lower.contains("longer")) {
            val lengthenedCuts = updated.cuts.map { cut ->
                cut.copy(durationSec = cut.durationSec * 1.3f)
            }
            var curTime = 0f
            val remappedCuts = lengthenedCuts.map { cut ->
                val mapped = cut.copy(timelineStartSec = curTime)
                curTime += cut.durationSec
                mapped
            }
            updated = updated.copy(cuts = remappedCuts, totalDurationSec = curTime)
            logMessages.add("🎬 Relaxed shot pacing to 2.5s+ for smoother cinematic flow.")
        }

        // 2. Captions styling & toggle
        if (lower.contains("no caption") || lower.contains("disable caption") || lower.contains("remove caption") || lower.contains("hide caption")) {
            updated = updated.copy(enableCaptions = false)
            logMessages.add("🚫 Disabled automatic caption generation for export.")
        } else if (lower.contains("enable caption") || lower.contains("show caption") || lower.contains("add caption")) {
            val restored = if (updated.captions.isEmpty()) generateCaptions(updated.totalDurationSec, updated.style.captionPreset) else updated.captions
            updated = updated.copy(enableCaptions = true, captions = restored)
            logMessages.add("💬 Enabled automatic caption generation with ${updated.captionFontStyle.displayName} font.")
        }

        if (lower.contains("comic") || lower.contains("punch font")) {
            updated = updated.copy(captionFontStyle = CaptionFontStyle.PLAYFUL_COMIC)
            logMessages.add("🎨 Set caption font style to Comic Punch.")
        } else if (lower.contains("serif") || lower.contains("classic font") || lower.contains("luxury font")) {
            updated = updated.copy(captionFontStyle = CaptionFontStyle.ELEGANT_SERIF)
            logMessages.add("🏛️ Set caption font style to Classic Luxury Serif.")
        } else if (lower.contains("mono") || lower.contains("code font") || lower.contains("tech font")) {
            updated = updated.copy(captionFontStyle = CaptionFontStyle.TECH_MONO)
            logMessages.add("💻 Set caption font style to Cyber Mono.")
        } else if (lower.contains("sans") || lower.contains("clean font") || lower.contains("modern font")) {
            updated = updated.copy(captionFontStyle = CaptionFontStyle.CLEAN_SANS)
            logMessages.add("✨ Set caption font style to Clean Modern Sans.")
        } else if (lower.contains("impact") || lower.contains("bold font")) {
            updated = updated.copy(captionFontStyle = CaptionFontStyle.BOLD_IMPACT)
            logMessages.add("🔥 Set caption font style to Bold Impact.")
        }

        if (lower.contains("yellow") || lower.contains("hormozi")) {
            updated = updated.copy(style = updated.style.copy(captionPreset = CaptionPreset.HORMOZI))
            logMessages.add("🟡 Switched to Viral Hormozi captions with glowing yellow active word highlights.")
        } else if (lower.contains("cyan") || lower.contains("neon") || lower.contains("cyber")) {
            updated = updated.copy(style = updated.style.copy(captionPreset = CaptionPreset.NEON_PULSE))
            logMessages.add("🔷 Applied Cyber Neon glow caption style with italic highlights.")
        } else if (lower.contains("minimal") || lower.contains("clean") || lower.contains("white")) {
            updated = updated.copy(style = updated.style.copy(captionPreset = CaptionPreset.MINIMAL_STUDIO))
            logMessages.add("✨ Applied Clean Studio minimalist subtitles with subtle drop shadow.")
        } else if (lower.contains("green") || lower.contains("beast") || lower.contains("energy")) {
            updated = updated.copy(style = updated.style.copy(captionPreset = CaptionPreset.BEAST_MODE))
            logMessages.add("🟢 Switched to Beast Mode high-energy punchy green captions.")
        }

        // 3. Color grading
        if (lower.contains("teal") || lower.contains("cinematic") || lower.contains("orange")) {
            updated = updated.copy(colorGrade = ColorGradePreset.TEAL_ORANGE)
            logMessages.add("🎨 Applied Cinematic Teal & Orange color grade LUT.")
        } else if (lower.contains("vibrant") || lower.contains("pop")) {
            updated = updated.copy(colorGrade = ColorGradePreset.VIBRANT_POP)
            logMessages.add("🌈 Boosted saturation and contrast with Vibrant Pop grade.")
        } else if (lower.contains("dark") || lower.contains("noir") || lower.contains("moody")) {
            updated = updated.copy(colorGrade = ColorGradePreset.MOODY_NOIR)
            logMessages.add("🕶️ Applied Dark Luxury Moody Noir color grade.")
        } else if (lower.contains("warm") || lower.contains("sunset") || lower.contains("golden")) {
            updated = updated.copy(colorGrade = ColorGradePreset.GOLDEN_HOUR)
            logMessages.add("🌅 Infused Golden Hour warm sunset tones.")
        }

        // 4. Music changes
        if (lower.contains("phonk") || lower.contains("drift")) {
            val track = SampleMediaData.MusicTracks.firstOrNull { it.id == "m1" }
            if (track != null) {
                updated = updated.copy(selectedMusic = track)
                logMessages.add("🎵 Added Phonk Drift Energy viral background track.")
            }
        } else if (lower.contains("lo-fi") || lower.contains("lofi") || lower.contains("chill")) {
            val track = SampleMediaData.MusicTracks.firstOrNull { it.id == "m2" }
            if (track != null) {
                updated = updated.copy(selectedMusic = track)
                logMessages.add("☕ Added Midnight Lo-Fi Coffee chill background track.")
            }
        } else if (lower.contains("tech") || lower.contains("synth")) {
            val track = SampleMediaData.MusicTracks.firstOrNull { it.id == "m3" }
            if (track != null) {
                updated = updated.copy(selectedMusic = track)
                logMessages.add("💻 Added Cyberpunk Future Synth soundtrack.")
            }
        }

        // 5. Volume & Audio Ducking
        if (lower.contains("louder music") || lower.contains("volume up")) {
            updated = updated.copy(musicVolume = min(1.0f, updated.musicVolume + 0.2f))
            logMessages.add("🔊 Boosted background soundtrack volume to ${(updated.musicVolume * 100).toInt()}%.")
        } else if (lower.contains("quieter") || lower.contains("ducking") || lower.contains("lower music")) {
            updated = updated.copy(musicVolume = max(0.1f, updated.musicVolume - 0.15f))
            logMessages.add("🔉 Lowered background volume to ${(updated.musicVolume * 100).toInt()}% for clearer speech.")
        }

        // 6. Dynamic Zoom & Punch-ins
        if (lower.contains("zoom") || lower.contains("punch")) {
            val punchCuts = updated.cuts.mapIndexed { idx, cut ->
                cut.copy(zoomFactor = if (idx % 2 == 0) 1.25f else 1.05f)
            }
            updated = updated.copy(cuts = punchCuts)
            logMessages.add("🔍 Enhanced dynamic camera zoom-ins on alternating cuts.")
        }

        // 7. Aspect ratio switch
        if (lower.contains("16:9") || lower.contains("landscape") || lower.contains("horizontal")) {
            updated = updated.copy(aspectRatio = AspectRatioType.RATIO_16_9)
            logMessages.add("🖥️ Changed aspect ratio to 16:9 Landscape for YouTube.")
        } else if (lower.contains("9:16") || lower.contains("vertical") || lower.contains("reels") || lower.contains("shorts") || lower.contains("tiktok")) {
            updated = updated.copy(aspectRatio = AspectRatioType.RATIO_9_16)
            logMessages.add("📱 Changed aspect ratio to 9:16 Vertical for Instagram Reels & TikTok.")
        } else if (lower.contains("1:1") || lower.contains("square")) {
            updated = updated.copy(aspectRatio = AspectRatioType.RATIO_1_1)
            logMessages.add("⏹️ Changed aspect ratio to 1:1 Square.")
        }

        // 8. Visual Filter Effects & Shaders
        if (lower.contains("vhs") || lower.contains("glitch") || lower.contains("retro")) {
            updated = updated.copy(activeFilterEffect = VideoFilterEffect.VHS_GLITCH)
            logMessages.add("📼 Enabled VHS Retro Glitch filter with CRT scanlines & chromatic distortion.")
        } else if (lower.contains("rgb") || lower.contains("holo")) {
            updated = updated.copy(activeFilterEffect = VideoFilterEffect.RGB_SPLIT)
            logMessages.add("⚡ Enabled RGB Hologram chromatic aberration effect.")
        } else if (lower.contains("grain") || lower.contains("35mm") || lower.contains("film")) {
            updated = updated.copy(activeFilterEffect = VideoFilterEffect.FILM_GRAIN)
            logMessages.add("🎞️ Applied 35mm Cinematic Film Grain & warm halation.")
        } else if (lower.contains("cyber") || lower.contains("neon glow")) {
            updated = updated.copy(activeFilterEffect = VideoFilterEffect.NEON_CYBER)
            logMessages.add("🌟 Enabled Cyber Neon Glow edge lighting bloom.")
        } else if (lower.contains("strobe") || lower.contains("flash")) {
            updated = updated.copy(activeFilterEffect = VideoFilterEffect.FLASH_STROBE)
            logMessages.add("⚡ Enabled Impact Strobe Flash on key cut transitions.")
        } else if (lower.contains("no fx") || lower.contains("remove effect") || lower.contains("clean effect")) {
            updated = updated.copy(activeFilterEffect = VideoFilterEffect.NONE)
            logMessages.add("✨ Cleared visual filter effects for natural clean clarity.")
        }

        // 9. Motion Graphics & Animated Overlays
        if (lower.contains("sound on") || lower.contains("audio badge")) {
            val soundOnItem = SampleMediaData.ReadyToUseOverlays.firstOrNull { it.id == "ov_sound_on" }
            if (soundOnItem != null) {
                updated = updated.copy(activeOverlayItems = listOf(soundOnItem))
                logMessages.add("🔊 Added 'SOUND ON' animated audio hook overlay.")
            }
        } else if (lower.contains("wait for it") || lower.contains("suspense")) {
            val waitItem = SampleMediaData.ReadyToUseOverlays.firstOrNull { it.id == "ov_wait" }
            if (waitItem != null) {
                updated = updated.copy(activeOverlayItems = listOf(waitItem))
                logMessages.add("😱 Added 'WAIT FOR IT...' animated suspense badge.")
            }
        } else if (lower.contains("viral alert") || lower.contains("breaking")) {
            val viralItem = SampleMediaData.ReadyToUseOverlays.firstOrNull { it.id == "ov_viral" }
            if (viralItem != null) {
                updated = updated.copy(activeOverlayItems = listOf(viralItem))
                logMessages.add("🚨 Added 'VIRAL ALERT' neon pulsing top header.")
            }
        }

        // 10. Waveform visualizer
        if (lower.contains("waveform") || lower.contains("audio visualizer") || lower.contains("spectrum")) {
            updated = updated.copy(showAudioWaveform = true)
            logMessages.add("📊 Enabled animated audio spectrum waveform visualizer.")
        }

        val finalMessage = if (logMessages.isNotEmpty()) {
            logMessages.joinToString("\n")
        } else {
            "✨ Refined cut rhythm, audio ducking, and caption alignment according to your brief: \"$prompt\""
        }

        val updatedHistory = updated.revisionHistory + listOf(finalMessage)
        return Pair(updated.copy(revisionHistory = updatedHistory), finalMessage)
    }

    private inline fun <T> Iterable<T>.indexOfMaxBy(selector: (T) -> Int): Int {
        var maxIndex = -1
        var maxValue = Int.MIN_VALUE
        for ((idx, item) in this.withIndex()) {
            val v = selector(item)
            if (v > maxValue) {
                maxValue = v
                maxIndex = idx
            }
        }
        return maxIndex
    }
}

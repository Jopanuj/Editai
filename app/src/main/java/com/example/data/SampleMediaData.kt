package com.example.data

import com.example.model.*

object SampleMediaData {

    val SampleClips = listOf(
        RawVideoClip(
            id = "clip_podcast_1",
            title = "Podcast: Stop Waiting For Perfect",
            durationSec = 62.4f,
            category = "Talking Head",
            resolution = "4K 60fps",
            dimensions = "3840 x 2160",
            fileSizeMb = 312.4f,
            frameRate = 60,
            codec = "ProRes / H.264",
            bitrateMbps = 42.0f,
            silencePercentage = 28,
            detectedFaces = 1,
            energyScore = 92,
            gradientColors = listOf(0xFF8B5CF6, 0xFF3B82F6),
            detectedScenes = listOf(
                SceneSegment("s1", 0f, 3.5f, "Powerful Hook Quote", false, 95),
                SceneSegment("s2", 3.5f, 6.2f, "Awkward Pause & Mic Adjust", true, 10),
                SceneSegment("s3", 6.2f, 15.0f, "The 3-Step Execution Rule", false, 90),
                SceneSegment("s4", 15.0f, 18.5f, "Thinking Pause", true, 15),
                SceneSegment("s5", 18.5f, 32.0f, "Emotional Breakthrough Point", false, 98),
                SceneSegment("s6", 32.0f, 45.0f, "Viewer Call-to-action", false, 85)
            )
        ),
        RawVideoClip(
            id = "clip_street_food",
            title = "Tokyo Midnight Ramen & Wagyu",
            durationSec = 78.0f,
            category = "Food & Travel",
            resolution = "4K HDR 30fps",
            dimensions = "3840 x 2160",
            fileSizeMb = 485.0f,
            frameRate = 30,
            codec = "H.265 / HEVC HDR",
            bitrateMbps = 52.0f,
            silencePercentage = 15,
            detectedFaces = 2,
            energyScore = 89,
            gradientColors = listOf(0xFFF97316, 0xFFEF4444),
            detectedScenes = listOf(
                SceneSegment("t1", 0f, 4.0f, "Flames Sizzling Wagyu", false, 96),
                SceneSegment("t2", 4.0f, 12.0f, "Chef Noodles Toss in Broth", false, 92),
                SceneSegment("t3", 12.0f, 15.0f, "Camera Shaky Transition", false, 40),
                SceneSegment("t4", 15.0f, 26.0f, "Crisp Crunch First Bite Reaction", false, 99),
                SceneSegment("t5", 26.0f, 38.0f, "Night Alley Lanterns Walking", false, 88)
            )
        ),
        RawVideoClip(
            id = "clip_gym_pr",
            title = "Gym Heavy Deadlift & Mindset",
            durationSec = 54.5f,
            category = "Fitness",
            resolution = "1080p 120fps",
            dimensions = "1920 x 1080",
            fileSizeMb = 148.2f,
            frameRate = 120,
            codec = "H.264 High Profile",
            bitrateMbps = 22.5f,
            silencePercentage = 30,
            detectedFaces = 1,
            energyScore = 95,
            gradientColors = listOf(0xFF10B981, 0xFF059669),
            detectedScenes = listOf(
                SceneSegment("g1", 0f, 5.0f, "Chalk Slap High Intensity", false, 98),
                SceneSegment("g2", 5.0f, 12.0f, "Barbell Setup Breathing", true, 30),
                SceneSegment("g3", 12.0f, 22.0f, "Explosive 500lb Lift Lockout", false, 100),
                SceneSegment("g4", 22.0f, 35.0f, "Celebration & Direct Camera Advice", false, 94)
            )
        ),
        RawVideoClip(
            id = "clip_tech_setup",
            title = "Minimalist Cyber Desk Tour",
            durationSec = 65.0f,
            category = "Tech & Design",
            resolution = "4K 60fps",
            dimensions = "3840 x 2160",
            fileSizeMb = 295.0f,
            frameRate = 60,
            codec = "H.264 / AAC",
            bitrateMbps = 38.0f,
            silencePercentage = 18,
            detectedFaces = 1,
            energyScore = 84,
            gradientColors = listOf(0xFF06B6D4, 0xFF3B82F6),
            detectedScenes = listOf(
                SceneSegment("k1", 0f, 3.5f, "Ambient RGB Lighting Macro Shot", false, 91),
                SceneSegment("k2", 3.5f, 14.0f, "Mechanical Keyboard Sound Test", false, 95),
                SceneSegment("k3", 14.0f, 25.0f, "Ultrawide Curved Monitor Glow", false, 87),
                SceneSegment("k4", 25.0f, 38.0f, "Cable Management Hidden Secret", false, 93)
            )
        ),
        RawVideoClip(
            id = "clip_travel_bali",
            title = "Bali Cliffside Sunset Drone Run",
            durationSec = 90.0f,
            category = "Cinematic Travel",
            resolution = "4K 60fps",
            dimensions = "3840 x 2160",
            fileSizeMb = 540.6f,
            frameRate = 60,
            codec = "D-Log H.265",
            bitrateMbps = 50.0f,
            silencePercentage = 40,
            detectedFaces = 0,
            energyScore = 91,
            gradientColors = listOf(0xFFEC4899, 0xFF8B5CF6),
            detectedScenes = listOf(
                SceneSegment("b1", 0f, 6.0f, "Dramatic Ocean Wave Crash", false, 94),
                SceneSegment("b2", 6.0f, 18.0f, "Drone Reveal of Cliff Temple", false, 97),
                SceneSegment("b3", 18.0f, 30.0f, "Golden Hour Motorcycle Coastal Cruise", false, 93)
            )
        )
    )

    val EditStyles = listOf(
        EditStyle(
            id = "style_hormozi",
            name = "Alex Hormozi Viral Cut",
            description = "Fast 1.2s cuts, kinetic bouncing yellow/green captions, zoom punch-ins, whoosh sound effects.",
            tag = "⚡ Viral Retention #1",
            targetPlatform = "Reels / Shorts / TikTok",
            cutPacingSec = 1.4f,
            captionPreset = CaptionPreset.HORMOZI,
            colorGrade = ColorGradePreset.VIBRANT_POP,
            defaultPrompt = "Cut all pauses >0.3s. Keep fast 1.5s cuts with dynamic face zoom. Add bold yellow animated captions with emojis and punchy sound effects.",
            iconKey = "bolt"
        ),
        EditStyle(
            id = "style_cinematic",
            name = "Cinematic B-Roll & Story",
            description = "Smooth pacing, teal & orange blockbuster grade, lo-fi atmospheric score, clean minimal subtitle bars.",
            tag = "🎬 High Aesthetics",
            targetPlatform = "YouTube / Reels / Travel",
            cutPacingSec = 2.8f,
            captionPreset = CaptionPreset.MINIMAL_STUDIO,
            colorGrade = ColorGradePreset.TEAL_ORANGE,
            defaultPrompt = "Preserve dramatic scenery and emotional moments. Smooth speed ramps into motion beats. Rich cinematic color grading and subtle ambient background track.",
            iconKey = "movie"
        ),
        EditStyle(
            id = "style_high_energy",
            name = "TikTok Split-Hook Trend",
            description = "Ultra high energy 3s hook, explosive transitions, comic style captions with pop sound effects on keywords.",
            tag = "🔥 3-Second Hook",
            targetPlatform = "TikTok & IG Reels",
            cutPacingSec = 1.0f,
            captionPreset = CaptionPreset.BEAST_MODE,
            colorGrade = ColorGradePreset.VIBRANT_POP,
            defaultPrompt = "Start with highest energy scene in first 2 seconds. Apply rapid pace cuts synced to phonk beats. Add big text callouts and screen shakes on impacts.",
            iconKey = "whatshot"
        ),
        EditStyle(
            id = "style_podcast",
            name = "Podcast Auto-Reframe 9:16",
            description = "Auto-crops landscape video into centered 9:16 vertical video, removes stutter and silence, highlights quotes.",
            tag = "🎙️ Talking Head Pro",
            targetPlatform = "Shorts & Reels",
            cutPacingSec = 2.2f,
            captionPreset = CaptionPreset.NEON_PULSE,
            colorGrade = ColorGradePreset.NATURAL,
            defaultPrompt = "Auto-reframe 16:9 to vertical 9:16 keeping speaker centered. Eliminate all 'ums' and dead air. Word-by-word karaoke style captions.",
            iconKey = "mic"
        ),
        EditStyle(
            id = "style_minimal_tech",
            name = "Clean Tech & SaaS Explainer",
            description = "Sleek modern minimalist aesthetics, crisp keyboard sound effects, subtle upbeat synth, studio lighting tone.",
            tag = "✨ Modern Product",
            targetPlatform = "LinkedIn / X / Reels",
            cutPacingSec = 2.0f,
            captionPreset = CaptionPreset.MINIMAL_STUDIO,
            colorGrade = ColorGradePreset.MOODY_NOIR,
            defaultPrompt = "Clean professional presentation. Highlight crisp product b-roll with smooth zoom pans. Minimal white typography and subtle whoosh accents.",
            iconKey = "devices"
        )
    )

    val MusicTracks = listOf(
        MusicTrack("m1", "Phonk Drift Energy", "Bass Syndicate", "Phonk / Viral", 135, 120f, "Very High"),
        MusicTrack("m2", "Midnight Lo-Fi Coffee", "Chilled Echoes", "Lo-Fi Beats", 85, 180f, "Chill / Focus"),
        MusicTrack("m3", "Cyberpunk Future Synth", "Neon Wave", "Synthwave", 120, 150f, "Driving High"),
        MusicTrack("m4", "Golden Hour Acoustic", "Sunset Collective", "Acoustic / Warm", 95, 140f, "Inspirational"),
        MusicTrack("m5", "Upbeat Tech Innovation", "Pulse Studio", "Modern Corporate", 115, 110f, "Crisp Clean")
    )

    val SoundEffectsLibrary = listOf(
        SoundEffect("sfx_whoosh", "Fast Air Whoosh", 1.2f, "💨"),
        SoundEffect("sfx_pop", "Pop Notification", 2.8f, "💥"),
        SoundEffect("sfx_bell", "Success Ding", 5.0f, "🔔"),
        SoundEffect("sfx_glitch", "Cyber Glitch Hit", 8.4f, "⚡"),
        SoundEffect("sfx_camera", "Camera Shutter Click", 12.0f, "📸"),
        SoundEffect("sfx_bass", "Sub Bass Impact", 15.5f, "🔊")
    )

    val ViralHookSuggestions = listOf(
        "Watch this before you make this massive mistake 👇",
        "Nobody is talking about this hidden trick in 2026...",
        "I tested this for 30 days and the results are wild 🤯",
        "Stop doing this right now if you want real results!",
        "The #1 secret 99% of people miss every single day 🔥"
    )

    val PresetPrompts = listOf(
        "Make cuts faster (under 1.5s per shot)",
        "Change captions to neon yellow with fire emojis 🔥",
        "Add subtle dramatic zooms on every speaker punchline",
        "Add energetic Phonk background music with auto-ducking",
        "Keep total final length strictly under 30 seconds"
    )

    val ReadyToUseOverlays = listOf(
        MotionOverlayItem(
            id = "ov_sound_on",
            text = "SOUND ON",
            emoji = "🔊",
            subtitle = "Pulsing audio badge to boost watch time",
            position = OverlayPosition.TOP_CENTER,
            animationType = OverlayAnimation.PULSE,
            isEnabled = true
        ),
        MotionOverlayItem(
            id = "ov_wait",
            text = "WAIT FOR IT...",
            emoji = "😱",
            subtitle = "Suspense hook badge for punchlines",
            position = OverlayPosition.TOP_CENTER,
            animationType = OverlayAnimation.GLITCH_BOUNCE,
            isEnabled = false
        ),
        MotionOverlayItem(
            id = "ov_real",
            text = "100% REAL",
            emoji = "💯",
            subtitle = "Authenticity gold badge for credibility",
            position = OverlayPosition.LOWER_THIRD,
            animationType = OverlayAnimation.PULSE,
            isEnabled = false
        ),
        MotionOverlayItem(
            id = "ov_viral",
            text = "VIRAL ALERT",
            emoji = "🚨",
            subtitle = "High-urgency flashing red alert header",
            position = OverlayPosition.TOP_CENTER,
            animationType = OverlayAnimation.NEON_FLICKER,
            isEnabled = false
        ),
        MotionOverlayItem(
            id = "ov_fire",
            text = "FIRE TAKE",
            emoji = "🔥",
            subtitle = "Hot streak animated fire sticker",
            position = OverlayPosition.MIDDLE_SCREEN,
            animationType = OverlayAnimation.PULSE,
            isEnabled = false
        ),
        MotionOverlayItem(
            id = "ov_sub",
            text = "FOLLOW FOR PART 2",
            emoji = "🔔",
            subtitle = "Viral Call-to-action button overlay",
            position = OverlayPosition.LOWER_THIRD,
            animationType = OverlayAnimation.SLIDE_IN,
            isEnabled = false
        )
    )
}

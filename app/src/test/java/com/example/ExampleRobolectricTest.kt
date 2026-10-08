package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SampleMediaData
import com.example.data.ShortyEditEngine
import com.example.model.AspectRatioType
import com.example.model.CaptionFontStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Shorty AI", appName)
  }

  @Test
  fun `shorty edit engine generates cuts from raw clips`() {
    val sampleClip = SampleMediaData.SampleClips.first()
    val hormoziStyle = SampleMediaData.EditStyles.first()

    val project = ShortyEditEngine.generateEditPlan(
      rawClips = listOf(sampleClip),
      style = hormoziStyle,
      customPrompt = "",
      aspectRatio = AspectRatioType.RATIO_9_16,
      removeSilences = true,
      enableCaptions = true,
      captionFontStyle = CaptionFontStyle.BOLD_IMPACT
    )

    assertNotNull(project)
    assertTrue("Should generate cuts", project.cuts.isNotEmpty())
    assertTrue("Total duration should be positive", project.totalDurationSec > 0f)
    assertTrue("Should generate animated captions", project.captions.isNotEmpty())
    assertTrue("Captions should be enabled", project.enableCaptions)
    assertEquals(CaptionFontStyle.BOLD_IMPACT, project.captionFontStyle)
    assertEquals(AspectRatioType.RATIO_9_16, project.aspectRatio)
  }

  @Test
  fun `shorty edit engine can disable captions for clean video export`() {
    val sampleClip = SampleMediaData.SampleClips.first()
    val hormoziStyle = SampleMediaData.EditStyles.first()

    val cleanProject = ShortyEditEngine.generateEditPlan(
      rawClips = listOf(sampleClip),
      style = hormoziStyle,
      enableCaptions = false
    )

    assertNotNull(cleanProject)
    assertFalse("Captions should be disabled", cleanProject.enableCaptions)
    assertTrue("Captions list should be empty when disabled", cleanProject.captions.isEmpty())
  }

  @Test
  fun `caption font styles can be customized`() {
    val sampleClip = SampleMediaData.SampleClips.first()
    val hormoziStyle = SampleMediaData.EditStyles.first()

    val monoProject = ShortyEditEngine.generateEditPlan(
      rawClips = listOf(sampleClip),
      style = hormoziStyle,
      enableCaptions = true,
      captionFontStyle = CaptionFontStyle.TECH_MONO
    )

    assertNotNull(monoProject)
    assertTrue(monoProject.enableCaptions)
    assertEquals(CaptionFontStyle.TECH_MONO, monoProject.captionFontStyle)
  }

  @Test
  fun `shorty edit engine respects manual trimming boundaries`() {
    val sampleClip = SampleMediaData.SampleClips.first()
    val trimmedClip = sampleClip.copy(trimStartSec = 6f, trimEndSec = 30f)
    val hormoziStyle = SampleMediaData.EditStyles.first()

    val project = ShortyEditEngine.generateEditPlan(
      rawClips = listOf(trimmedClip),
      style = hormoziStyle,
      aspectRatio = AspectRatioType.RATIO_9_16
    )

    assertNotNull(project)
    assertTrue("Should generate cuts within trimmed range", project.cuts.isNotEmpty())
    project.cuts.forEach { cut ->
      assertTrue("Cut sourceStartSec should be >= trimStartSec", cut.sourceStartSec >= 6f)
      assertTrue("Cut sourceEndSec should be <= trimEndSec", cut.sourceEndSec <= 30.5f)
    }
  }

  @Test
  fun `ai prompt revision updates cut pacing`() {
    val sampleClip = SampleMediaData.SampleClips.first()
    val hormoziStyle = SampleMediaData.EditStyles.first()
    val project = ShortyEditEngine.generateEditPlan(
      rawClips = listOf(sampleClip),
      style = hormoziStyle,
      aspectRatio = AspectRatioType.RATIO_9_16
    )

    val (revised, _) = ShortyEditEngine.applyPromptRevision(project, "make cuts faster")
    assertNotNull(revised)
    assertTrue("Revision history should be updated", revised.revisionHistory.isNotEmpty())
  }

  @Test
  fun `raw video clips provide metadata for preview`() {
    val sampleClip = SampleMediaData.SampleClips.first()
    assertTrue("File size should be greater than 0 MB", sampleClip.fileSizeMb > 0f)
    assertTrue("Duration should be positive", sampleClip.durationSec > 0f)
    assertTrue("Resolution should not be empty", sampleClip.resolution.isNotBlank())
    assertTrue("Dimensions should not be empty", sampleClip.dimensions.isNotBlank())
    assertTrue("Codec should not be empty", sampleClip.codec.isNotBlank())
  }

  @Test
  fun `video filter effects can be applied via prompt revision`() {
    val sampleClip = SampleMediaData.SampleClips.first()
    val hormoziStyle = SampleMediaData.EditStyles.first()
    val project = ShortyEditEngine.generateEditPlan(
      rawClips = listOf(sampleClip),
      style = hormoziStyle
    )

    val (vhsProject, _) = ShortyEditEngine.applyPromptRevision(project, "add retro vhs glitch")
    assertEquals(com.example.model.VideoFilterEffect.VHS_GLITCH, vhsProject.activeFilterEffect)

    val (filmProject, _) = ShortyEditEngine.applyPromptRevision(project, "give it 35mm film grain")
    assertEquals(com.example.model.VideoFilterEffect.FILM_GRAIN, filmProject.activeFilterEffect)
  }

  @Test
  fun `motion overlay badges and audio waveform can be toggled`() {
    val sampleClip = SampleMediaData.SampleClips.first()
    val hormoziStyle = SampleMediaData.EditStyles.first()
    val project = ShortyEditEngine.generateEditPlan(
      rawClips = listOf(sampleClip),
      style = hormoziStyle
    )

    val (waveformProject, _) = ShortyEditEngine.applyPromptRevision(project, "show audio waveform visualizer")
    assertTrue("Audio waveform should be enabled", waveformProject.showAudioWaveform)

    val (stickerProject, _) = ShortyEditEngine.applyPromptRevision(project, "add sound on badge")
    assertTrue("Should have active overlay items", stickerProject.activeOverlayItems.isNotEmpty())
  }
}

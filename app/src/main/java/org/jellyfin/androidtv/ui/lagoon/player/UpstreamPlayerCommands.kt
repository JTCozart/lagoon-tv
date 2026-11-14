package org.jellyfin.androidtv.ui.lagoon.player

import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.ZoomMode
import org.jellyfin.androidtv.ui.playback.PlaybackController
import org.jellyfin.androidtv.ui.playback.VideoQualityController
import org.jellyfin.androidtv.ui.playback.VideoSpeedController
import org.jellyfin.androidtv.ui.playback.setSubtitleIndex

class UpstreamPlayerCommands(
	private val controller: () -> PlaybackController?,
	private val userPreferences: UserPreferences,
) : PlayerCommands {
	private val qualityController by lazy { VideoQualityController(userPreferences[UserPreferences.maxBitrate], userPreferences) }

	override fun playPause() = withController { playPause() }
	override fun rewind() = withController { rewind() }
	override fun fastForward() = withController { fastForward() }
	override fun next() = withController { next() }
	override fun previous() = withController { prev() }
	override fun seek(positionMs: Long) = withController { seek(positionMs) }
	override fun selectAudio(index: Int) = withController { switchAudioStream(index) }
	override fun selectSubtitle(index: Int) = withController { setSubtitleIndex(index) }
	override fun setZoom(mode: ZoomMode) = withController { setZoom(mode) }

	override fun setSpeed(step: VideoSpeedController.SpeedSteps) = withController {
		VideoSpeedController(this).currentSpeed = step
	}

	override fun setQuality(qualityKey: String) = withController {
		qualityController.currentQuality = qualityKey
		refreshStream()
	}

	override fun currentQuality(): String = qualityController.currentQuality

	private inline fun withController(action: PlaybackController.() -> Unit) {
		controller()?.action()
	}
}

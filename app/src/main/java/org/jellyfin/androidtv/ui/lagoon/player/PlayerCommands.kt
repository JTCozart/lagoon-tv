package org.jellyfin.androidtv.ui.lagoon.player

import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.ZoomMode
import org.jellyfin.androidtv.ui.playback.PlaybackController
import org.jellyfin.androidtv.ui.playback.VideoQualityController
import org.jellyfin.androidtv.ui.playback.VideoSpeedController
import org.jellyfin.androidtv.ui.playback.setSubtitleIndex

/**
 * What the player controls can ask the video to do. Each command calls the same upstream playback controller
 * method the leanback buttons used, so behaviour such as reporting progress to the server is unchanged.
 */
interface PlayerCommands {
	fun playPause()
	fun rewind()
	fun fastForward()
	fun next()
	fun previous()
	fun seek(positionMs: Long)
	fun selectAudio(index: Int)
	fun selectSubtitle(index: Int)
	fun setSpeed(step: VideoSpeedController.SpeedSteps)
	fun setQuality(qualityKey: String)
	fun currentQuality(): String
	fun setZoom(mode: ZoomMode)
}

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

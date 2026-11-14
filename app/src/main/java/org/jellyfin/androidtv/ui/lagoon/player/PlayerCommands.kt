package org.jellyfin.androidtv.ui.lagoon.player

import org.jellyfin.androidtv.preference.constant.ZoomMode
import org.jellyfin.androidtv.ui.playback.VideoSpeedController

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

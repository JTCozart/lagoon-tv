package org.jellyfin.androidtv.ui.lagoon.player

import org.jellyfin.androidtv.preference.constant.ZoomMode
import org.jellyfin.androidtv.ui.playback.PlaybackController
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ChapterInfo
import org.jellyfin.sdk.model.api.MediaStreamType

data class TrackOption(val index: Int, val label: String)

/**
 * Everything the player controls show, read from upstream's playback controller in one go so Compose
 * redraws from a single consistent value.
 */
data class PlayerSnapshot(
	val item: BaseItemDto? = null,
	val isPaused: Boolean = false,
	val positionMs: Long = 0L,
	val durationMs: Long = 0L,
	val bufferedMs: Long = 0L,
	val canSeek: Boolean = true,
	val hasNext: Boolean = false,
	val hasPrevious: Boolean = false,
	val audioTracks: List<TrackOption> = emptyList(),
	val selectedAudioIndex: Int = -1,
	val subtitleTracks: List<TrackOption> = emptyList(),
	val selectedSubtitleIndex: Int = -1,
	val speed: Float = 1f,
	val zoom: ZoomMode = ZoomMode.FIT,
	val chapters: List<ChapterInfo> = emptyList(),
) {
	companion object {
		fun read(controller: PlaybackController): PlayerSnapshot {
			val subtitles = controller.currentMediaSource?.mediaStreams.orEmpty()
				.filter { it.type == MediaStreamType.SUBTITLE }
				.map { TrackOption(it.index, it.displayTitle ?: it.language ?: "#${it.index}") }
			val audio = controller.currentStreamInfo?.selectableAudioStreams.orEmpty()
				.map { TrackOption(it.index, it.displayTitle ?: it.language ?: "#${it.index}") }
			val item = controller.currentlyPlayingItem

			return PlayerSnapshot(
				item = item,
				isPaused = controller.isPaused,
				positionMs = controller.currentPosition,
				durationMs = controller.duration,
				bufferedMs = controller.bufferedPosition,
				canSeek = controller.canSeek(),
				hasNext = controller.hasNextItem(),
				hasPrevious = controller.hasPreviousItem(),
				audioTracks = audio,
				// The controller only knows the audio track once a stream has started
				selectedAudioIndex = if (controller.currentStreamInfo != null) controller.audioStreamIndex else -1,
				subtitleTracks = subtitles,
				selectedSubtitleIndex = controller.subtitleStreamIndex,
				speed = controller.playbackSpeed,
				zoom = controller.zoomMode,
				chapters = item?.chapters.orEmpty(),
			)
		}
	}
}

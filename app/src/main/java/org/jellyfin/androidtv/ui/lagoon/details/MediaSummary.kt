package org.jellyfin.androidtv.ui.lagoon.details

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.MediaStreamType
import org.jellyfin.sdk.model.api.VideoRangeType

/** The few facts about a file that are worth a badge. */
data class MediaSummary(
	val videoWidth: Int?,
	val videoRange: VideoRangeType?,
	val maxAudioChannels: Int?,
	val hasSubtitles: Boolean,
) {
	companion object {
		fun of(item: BaseItemDto): MediaSummary {
			val streams = item.mediaSources?.firstOrNull()?.mediaStreams.orEmpty()
			val video = streams.firstOrNull { it.type == MediaStreamType.VIDEO }
			return MediaSummary(
				videoWidth = video?.width,
				videoRange = video?.videoRangeType,
				maxAudioChannels = streams.filter { it.type == MediaStreamType.AUDIO }.mapNotNull { it.channels }.maxOrNull(),
				hasSubtitles = streams.any { it.type == MediaStreamType.SUBTITLE },
			)
		}
	}
}

package org.jellyfin.androidtv.ui.lagoon.details

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.VideoRangeType

/**
 * Quality badges for a details page, in the order streaming apps show them: picture size, dynamic range, sound, captions.
 */
object MediaBadges {
	private const val UltraHdWidth = 3200
	private const val FullHdWidth = 1900
	private const val HdWidth = 1200
	private const val SevenOneChannels = 8
	private const val FiveOneChannels = 6

	fun of(item: BaseItemDto): List<String> = of(MediaSummary.of(item))

	fun of(media: MediaSummary): List<String> = listOfNotNull(
		resolutionOf(media.videoWidth),
		dynamicRangeOf(media.videoRange),
		surroundOf(media.maxAudioChannels),
		"CC".takeIf { media.hasSubtitles },
	)

	private fun resolutionOf(width: Int?): String? = when {
		width == null -> null
		width >= UltraHdWidth -> "4K"
		width >= FullHdWidth -> "1080p"
		width >= HdWidth -> "720p"
		else -> "SD"
	}

	private fun dynamicRangeOf(range: VideoRangeType?): String? = when (range) {
		VideoRangeType.HDR10 -> "HDR10"
		VideoRangeType.HDR10_PLUS -> "HDR10+"
		VideoRangeType.HLG -> "HLG"
		VideoRangeType.DOVI,
		VideoRangeType.DOVI_WITH_HDR10,
		VideoRangeType.DOVI_WITH_HLG,
		VideoRangeType.DOVI_WITH_SDR,
		VideoRangeType.DOVI_WITH_EL,
		VideoRangeType.DOVI_WITH_HDR10_PLUS,
		VideoRangeType.DOVI_WITH_ELHDR10_PLUS -> "Dolby Vision"

		else -> null
	}

	private fun surroundOf(channels: Int?): String? = when {
		channels == null -> null
		channels >= SevenOneChannels -> "7.1"
		channels >= FiveOneChannels -> "5.1"
		else -> null
	}
}

package org.jellyfin.androidtv.ui.lagoon.details

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

/**
 * Where a details page starts when it is opened from a series, a season or a single episode: an episode or season
 * opens its series' page, on the right season, with Play set to the right episode.
 */
object SeriesPageEntry {
	private val partsOfSeries = setOf(BaseItemKind.EPISODE, BaseItemKind.SEASON)

	fun titleIdOf(opened: BaseItemDto): UUID =
		if (opened.type in partsOfSeries) opened.seriesId ?: opened.id else opened.id

	fun startingEpisode(opened: BaseItemDto, nextUp: BaseItemDto?): BaseItemDto? =
		if (opened.type == BaseItemKind.EPISODE) opened else nextUp

	fun seasonToShow(
		opened: BaseItemDto,
		startingEpisode: BaseItemDto?,
		seasons: List<BaseItemDto>,
		previouslyShown: UUID?,
	): UUID? = when {
		opened.type == BaseItemKind.SEASON -> opened.id
		else -> startingEpisode?.seasonId ?: previouslyShown ?: seasons.firstOrNull()?.id
	}

	fun playTarget(startingEpisode: BaseItemDto?, episodesInSeason: List<BaseItemDto>): BaseItemDto? =
		startingEpisode ?: episodesInSeason.firstOrNull()
}

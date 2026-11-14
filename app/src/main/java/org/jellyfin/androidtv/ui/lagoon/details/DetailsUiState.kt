package org.jellyfin.androidtv.ui.lagoon.details

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemPerson
import java.util.UUID

data class DetailsUiState(
	val isLoading: Boolean = true,
	val hasFailed: Boolean = false,
	/** The movie or series the page is about. An episode opens its series' page. */
	val title: BaseItemDto? = null,
	val playPlan: PlayPlan? = null,
	val badges: List<String> = emptyList(),
	val isFavorite: Boolean = false,
	val isPlayed: Boolean = false,
	val seasons: List<BaseItemDto> = emptyList(),
	val selectedSeasonId: UUID? = null,
	val episodes: List<BaseItemDto> = emptyList(),
	/** The episode to put focus on when the episode row first appears. */
	val highlightedEpisodeId: UUID? = null,
	val cast: List<BaseItemPerson> = emptyList(),
	val similar: List<BaseItemDto> = emptyList(),
)

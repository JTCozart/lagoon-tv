package org.jellyfin.androidtv.ui.lagoon.search

import org.jellyfin.sdk.model.api.BaseItemDto

data class SearchUiState(
	val query: String = "",
	val isSearching: Boolean = false,
	/** The query the results below belong to; lags [query] while a search is in flight. */
	val searchedQuery: String = "",
	val titles: List<BaseItemDto> = emptyList(),
	val episodes: List<BaseItemDto> = emptyList(),
	val suggestions: List<BaseItemDto> = emptyList(),
	val libraries: List<BaseItemDto> = emptyList(),
) {
	val hasResults get() = titles.isNotEmpty() || episodes.isNotEmpty()
}

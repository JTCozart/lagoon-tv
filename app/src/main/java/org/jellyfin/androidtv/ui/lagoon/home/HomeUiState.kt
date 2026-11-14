package org.jellyfin.androidtv.ui.lagoon.home

import org.jellyfin.androidtv.ui.lagoon.browse.Shelf
import org.jellyfin.sdk.model.api.BaseItemDto

data class HomeUiState(
	val isLoading: Boolean = true,
	val featured: List<BaseItemDto> = emptyList(),
	val shelves: List<Shelf> = emptyList(),
	val libraries: List<BaseItemDto> = emptyList(),
)

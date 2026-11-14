package org.jellyfin.androidtv.ui.lagoon.library

import org.jellyfin.androidtv.ui.lagoon.browse.Shelf
import org.jellyfin.sdk.model.api.BaseItemDto

data class LibraryUiState(
	val mode: LibraryMode = LibraryMode.Browse,
	val isLoading: Boolean = true,
	val libraries: List<BaseItemDto> = emptyList(),
	val featured: List<BaseItemDto> = emptyList(),
	val shelves: List<Shelf> = emptyList(),
	val sort: LibrarySort = LibrarySort.RecentlyAdded,
	val unwatchedOnly: Boolean = false,
	val titles: List<BaseItemDto> = emptyList(),
	val totalTitles: Int = 0,
	val isLoadingTitles: Boolean = false,
)

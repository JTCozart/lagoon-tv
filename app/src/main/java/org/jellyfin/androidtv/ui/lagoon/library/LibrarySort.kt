package org.jellyfin.androidtv.ui.lagoon.library

import androidx.annotation.StringRes
import org.jellyfin.androidtv.R
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder

enum class LibrarySort(
	@StringRes val label: Int,
	val sortBy: ItemSortBy,
	val order: SortOrder,
) {
	RecentlyAdded(R.string.lagoon_sort_recently_added, ItemSortBy.DATE_CREATED, SortOrder.DESCENDING),
	AToZ(R.string.lagoon_sort_a_to_z, ItemSortBy.SORT_NAME, SortOrder.ASCENDING),
	ReleaseDate(R.string.lagoon_sort_release_date, ItemSortBy.PREMIERE_DATE, SortOrder.DESCENDING),
	Rating(R.string.lagoon_sort_rating, ItemSortBy.COMMUNITY_RATING, SortOrder.DESCENDING);

	fun next(): LibrarySort = entries[(ordinal + 1) % entries.size]
}

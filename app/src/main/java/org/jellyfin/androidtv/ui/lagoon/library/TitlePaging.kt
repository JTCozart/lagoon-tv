package org.jellyfin.androidtv.ui.lagoon.library

/** Fetches the next page of a library grid while there are still a couple of rows left to scroll through. */
object TitlePaging {
	private const val LoadAheadCount = 24

	fun shouldLoadMore(focusedIndex: Int, loadedCount: Int, totalCount: Int, isLoading: Boolean): Boolean =
		!isLoading && loadedCount < totalCount && focusedIndex >= loadedCount - LoadAheadCount
}

package org.jellyfin.androidtv.ui.lagoon.home

import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.CollectionType
import java.util.UUID

/**
 * Decides which shelves the home screen shows, from the home sections the user picked in settings.
 */
object HomeShelfPlanner {
	private val collectionsWithoutLatestShelf = setOf(
		CollectionType.PLAYLISTS,
		CollectionType.LIVETV,
		CollectionType.BOXSETS,
		CollectionType.BOOKS,
	)

	fun plan(
		sections: List<HomeSectionType>,
		libraries: Collection<BaseItemDto>,
		excludedFromLatest: Set<UUID>,
	): List<ShelfPlan> = sections
		.distinct()
		.flatMap { section -> shelvesFor(section, libraries, excludedFromLatest) }

	private fun shelvesFor(
		section: HomeSectionType,
		libraries: Collection<BaseItemDto>,
		excludedFromLatest: Set<UUID>,
	): List<ShelfPlan> = when (section) {
		HomeSectionType.RESUME -> listOf(ShelfPlan.ContinueWatching)
		HomeSectionType.NEXT_UP -> listOf(ShelfPlan.NextUp)
		HomeSectionType.LATEST_MEDIA -> libraries
			.filter { it.collectionType !in collectionsWithoutLatestShelf && it.id !in excludedFromLatest }
			.map { ShelfPlan.LatestIn(it) }

		else -> emptyList()
	}
}

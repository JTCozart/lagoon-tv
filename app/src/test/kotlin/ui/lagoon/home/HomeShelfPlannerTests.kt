package org.jellyfin.androidtv.ui.lagoon.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import java.util.UUID

private fun library(name: String, type: CollectionType?) = BaseItemDto(
	id = UUID.randomUUID(),
	type = BaseItemKind.COLLECTION_FOLDER,
	name = name,
	collectionType = type,
)

private val movies = library("Movies", CollectionType.MOVIES)
private val shows = library("Shows", CollectionType.TVSHOWS)
private val liveTv = library("Live TV", CollectionType.LIVETV)
private val playlists = library("Playlists", CollectionType.PLAYLISTS)

class HomeShelfPlannerTests : FunSpec({
	test("Shelves follow the order of the user's home sections") {
		val plan = HomeShelfPlanner.plan(
			sections = listOf(HomeSectionType.NEXT_UP, HomeSectionType.RESUME),
			libraries = listOf(movies),
			excludedFromLatest = emptySet(),
		)

		plan shouldContainExactly listOf(ShelfPlan.NextUp, ShelfPlan.ContinueWatching)
	}

	test("Latest media becomes one shelf per library, in library order") {
		val plan = HomeShelfPlanner.plan(
			sections = listOf(HomeSectionType.LATEST_MEDIA),
			libraries = listOf(shows, movies),
			excludedFromLatest = emptySet(),
		)

		plan shouldContainExactly listOf(ShelfPlan.LatestIn(shows), ShelfPlan.LatestIn(movies))
	}

	test("Libraries the user excluded from latest media get no shelf") {
		val plan = HomeShelfPlanner.plan(
			sections = listOf(HomeSectionType.LATEST_MEDIA),
			libraries = listOf(shows, movies),
			excludedFromLatest = setOf(shows.id),
		)

		plan shouldContainExactly listOf(ShelfPlan.LatestIn(movies))
	}

	test("Live TV and playlists never get a latest shelf") {
		val plan = HomeShelfPlanner.plan(
			sections = listOf(HomeSectionType.LATEST_MEDIA),
			libraries = listOf(liveTv, playlists, movies),
			excludedFromLatest = emptySet(),
		)

		plan shouldContainExactly listOf(ShelfPlan.LatestIn(movies))
	}

	test("Library tiles are left out because libraries live in the navigation rail") {
		val plan = HomeShelfPlanner.plan(
			sections = listOf(HomeSectionType.LIBRARY_TILES_SMALL, HomeSectionType.LIBRARY_BUTTONS, HomeSectionType.RESUME),
			libraries = listOf(movies),
			excludedFromLatest = emptySet(),
		)

		plan shouldContainExactly listOf(ShelfPlan.ContinueWatching)
	}

	test("A section listed twice gives one shelf") {
		val plan = HomeShelfPlanner.plan(
			sections = listOf(HomeSectionType.RESUME, HomeSectionType.RESUME),
			libraries = emptyList(),
			excludedFromLatest = emptySet(),
		)

		plan shouldContainExactly listOf(ShelfPlan.ContinueWatching)
	}
})

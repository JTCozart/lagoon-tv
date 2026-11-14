package org.jellyfin.androidtv.ui.lagoon.library

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import java.util.UUID

private fun genre(name: String, movies: Int? = null, series: Int? = null) = BaseItemDto(
	id = UUID.randomUUID(),
	type = BaseItemKind.GENRE,
	name = name,
	movieCount = movies,
	seriesCount = series,
)

class LibraryContentTests : FunSpec({
	test("A movie library lists movies and a show library lists series") {
		LibraryContent.itemKindsFor(CollectionType.MOVIES) shouldBe listOf(BaseItemKind.MOVIE)
		LibraryContent.itemKindsFor(CollectionType.TVSHOWS) shouldBe listOf(BaseItemKind.SERIES)
	}

	test("Music, photos and Live TV stay on their own screens") {
		LibraryContent.itemKindsFor(CollectionType.MUSIC) shouldBe null
		LibraryContent.itemKindsFor(CollectionType.PHOTOS) shouldBe null
		LibraryContent.itemKindsFor(CollectionType.LIVETV) shouldBe null
	}
})

class LibrarySortTests : FunSpec({
	test("Recently added shows the newest first") {
		LibrarySort.RecentlyAdded.sortBy shouldBe ItemSortBy.DATE_CREATED
		LibrarySort.RecentlyAdded.order shouldBe SortOrder.DESCENDING
	}

	test("A to Z sorts by name ascending") {
		LibrarySort.AToZ.sortBy shouldBe ItemSortBy.SORT_NAME
		LibrarySort.AToZ.order shouldBe SortOrder.ASCENDING
	}

	test("Pressing the sort chip cycles through every option and wraps around") {
		val visited = generateSequence(LibrarySort.RecentlyAdded) { it.next() }.take(LibrarySort.entries.size + 1).toList()

		visited.toSet() shouldBe LibrarySort.entries.toSet()
		visited.last() shouldBe LibrarySort.RecentlyAdded
	}
})

class GenreRankerTests : FunSpec({
	test("Movie genres are ranked by how many movies they hold") {
		val drama = genre("Drama", movies = 40)
		val action = genre("Action", movies = 90)
		val western = genre("Western", movies = 3)

		GenreRanker.top(listOf(drama, action, western), BaseItemKind.MOVIE, maximum = 2) shouldContainExactly listOf(action, drama)
	}

	test("Show genres are ranked by series count") {
		val comedy = genre("Comedy", movies = 100, series = 5)
		val animation = genre("Animation", movies = 1, series = 30)

		GenreRanker.top(listOf(comedy, animation), BaseItemKind.SERIES, maximum = 1) shouldContainExactly listOf(animation)
	}

	test("Genres with nothing in this library are skipped") {
		val empty = genre("Empty", movies = 0)
		val unknown = genre("Unknown")

		GenreRanker.top(listOf(empty, unknown), BaseItemKind.MOVIE, maximum = 5) shouldContainExactly emptyList()
	}
})

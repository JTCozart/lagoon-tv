package org.jellyfin.androidtv.ui.lagoon.browse

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

private const val TicksPerMinute = 600_000_000L

private fun movie(
	name: String = "The Long Tide",
	year: Int? = 2021,
	rating: String? = "PG-13",
	minutes: Long? = 112,
	communityRating: Float? = null,
	genres: List<String>? = listOf("Drama", "Adventure"),
) = BaseItemDto(
	id = UUID.randomUUID(),
	type = BaseItemKind.MOVIE,
	name = name,
	productionYear = year,
	officialRating = rating,
	runTimeTicks = minutes?.let { it * TicksPerMinute },
	communityRating = communityRating,
	genres = genres,
)

private fun episode() = BaseItemDto(
	id = UUID.randomUUID(),
	type = BaseItemKind.EPISODE,
	name = "Low Water",
	seriesName = "Harbor Lights",
	parentIndexNumber = 2,
	indexNumber = 5,
	runTimeTicks = 44 * TicksPerMinute,
)

class HeroMetadataTests : FunSpec({
	test("A movie is titled by its own name and has no subtitle") {
		HeroMetadata.titleOf(movie()) shouldBe "The Long Tide"
		HeroMetadata.subtitleOf(movie()) shouldBe null
	}

	test("An episode is titled by its series and subtitled with season, episode and name") {
		HeroMetadata.titleOf(episode()) shouldBe "Harbor Lights"
		HeroMetadata.subtitleOf(episode()) shouldBe "S2 E5  Low Water"
	}

	test("Details list year, rating, runtime and genres in that order") {
		HeroMetadata.detailsOf(movie()) shouldContainExactly listOf("2021", "PG-13", "1h 52m", "Drama", "Adventure")
	}

	test("A runtime under an hour shows minutes only") {
		HeroMetadata.detailsOf(episode()) shouldContainExactly listOf("44m")
	}

	test("A community rating shows with one decimal after the official rating") {
		HeroMetadata.detailsOf(movie(minutes = null, genres = null, communityRating = 7.84f)) shouldContainExactly
			listOf("2021", "PG-13", "★ 7.8")
	}

	test("Only the first three genres are shown") {
		val item = movie(year = null, rating = null, minutes = null, genres = listOf("A", "B", "C", "D"))
		HeroMetadata.detailsOf(item) shouldContainExactly listOf("A", "B", "C")
	}

	test("Missing values are left out rather than shown blank") {
		val item = movie(year = null, rating = " ", minutes = 0, genres = emptyList())
		HeroMetadata.detailsOf(item) shouldContainExactly emptyList()
	}
})

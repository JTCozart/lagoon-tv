package org.jellyfin.androidtv.ui.lagoon.details

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

private val seriesId = UUID.randomUUID()
private val seasonOne = BaseItemDto(id = UUID.randomUUID(), type = BaseItemKind.SEASON, seriesId = seriesId, name = "Season 1")
private val seasonTwo = BaseItemDto(id = UUID.randomUUID(), type = BaseItemKind.SEASON, seriesId = seriesId, name = "Season 2")
private val seasons = listOf(seasonOne, seasonTwo)

private fun episode(season: BaseItemDto) =
	BaseItemDto(id = UUID.randomUUID(), type = BaseItemKind.EPISODE, seriesId = seriesId, seasonId = season.id)

private val series = BaseItemDto(id = seriesId, type = BaseItemKind.SERIES)
private val movie = BaseItemDto(id = UUID.randomUUID(), type = BaseItemKind.MOVIE)

class SeriesPageEntryTests : FunSpec({
	test("An episode or season opens the page of its series") {
		SeriesPageEntry.titleIdOf(episode(seasonTwo)) shouldBe seriesId
		SeriesPageEntry.titleIdOf(seasonOne) shouldBe seriesId
	}

	test("A movie or series opens its own page") {
		SeriesPageEntry.titleIdOf(movie) shouldBe movie.id
		SeriesPageEntry.titleIdOf(series) shouldBe seriesId
	}

	test("An opened episode is where playback starts, ahead of the server's next up") {
		val opened = episode(seasonOne)
		val nextUp = episode(seasonTwo)

		SeriesPageEntry.startingEpisode(opened, nextUp) shouldBe opened
	}

	test("Opening the series starts from the server's next up") {
		val nextUp = episode(seasonTwo)

		SeriesPageEntry.startingEpisode(series, nextUp) shouldBe nextUp
	}

	test("An opened season is the season shown") {
		SeriesPageEntry.seasonToShow(seasonTwo, startingEpisode = episode(seasonOne), seasons, previouslyShown = null) shouldBe seasonTwo.id
	}

	test("Otherwise the season of the starting episode is shown") {
		SeriesPageEntry.seasonToShow(series, startingEpisode = episode(seasonTwo), seasons, previouslyShown = null) shouldBe seasonTwo.id
	}

	test("With nothing to resume, the season already on screen stays, then the first season") {
		SeriesPageEntry.seasonToShow(series, startingEpisode = null, seasons, previouslyShown = seasonTwo.id) shouldBe seasonTwo.id
		SeriesPageEntry.seasonToShow(series, startingEpisode = null, seasons, previouslyShown = null) shouldBe seasonOne.id
	}

	test("Play starts the starting episode, or else the first episode of the season shown") {
		val starting = episode(seasonTwo)
		val first = episode(seasonOne)

		SeriesPageEntry.playTarget(starting, listOf(first)) shouldBe starting
		SeriesPageEntry.playTarget(null, listOf(first)) shouldBe first
		SeriesPageEntry.playTarget(null, emptyList()) shouldBe null
	}
})

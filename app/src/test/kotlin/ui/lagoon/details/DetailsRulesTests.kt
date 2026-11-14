package org.jellyfin.androidtv.ui.lagoon.details

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.BaseItemPerson
import org.jellyfin.sdk.model.api.PersonKind
import org.jellyfin.sdk.model.api.UserItemDataDto
import org.jellyfin.sdk.model.api.VideoRangeType
import java.util.UUID

private const val TicksPerMinute = 600_000_000L

private fun userData(positionMinutes: Long = 0) = UserItemDataDto(
	playbackPositionTicks = positionMinutes * TicksPerMinute,
	playCount = 0,
	isFavorite = false,
	played = false,
	key = "key",
	itemId = UUID.randomUUID(),
)

private fun movie(runtimeMinutes: Long = 120, positionMinutes: Long = 0) = BaseItemDto(
	id = UUID.randomUUID(),
	type = BaseItemKind.MOVIE,
	runTimeTicks = runtimeMinutes * TicksPerMinute,
	userData = userData(positionMinutes),
)

private fun episode(season: Int = 2, number: Int = 3, positionMinutes: Long = 0) = BaseItemDto(
	id = UUID.randomUUID(),
	type = BaseItemKind.EPISODE,
	parentIndexNumber = season,
	indexNumber = number,
	runTimeTicks = 44 * TicksPerMinute,
	userData = userData(positionMinutes),
)

private fun media(
	width: Int? = null,
	range: VideoRangeType? = null,
	channels: Int? = null,
	hasSubtitles: Boolean = false,
) = MediaSummary(videoWidth = width, videoRange = range, maxAudioChannels = channels, hasSubtitles = hasSubtitles)

private fun person(name: String, kind: PersonKind) = BaseItemPerson(id = UUID.randomUUID(), name = name, type = kind)

class PlayPlanTests : FunSpec({
	test("An unwatched movie plays from the start") {
		val plan = PlayPlan.of(movie())

		plan.isResume shouldBe false
		plan.remainingMinutes shouldBe null
	}

	test("A part watched movie resumes and says how much is left") {
		val plan = PlayPlan.of(movie(runtimeMinutes = 120, positionMinutes = 88))

		plan.isResume shouldBe true
		plan.remainingMinutes shouldBe 32L
	}

	test("An episode names itself on the play button") {
		PlayPlan.of(episode(season = 2, number = 3)).episodeLabel shouldBe "S2 E3"
	}

	test("A movie has no episode label") {
		PlayPlan.of(movie()).episodeLabel shouldBe null
	}
})

class RuntimeTextTests : FunSpec({
	test("Durations of an hour or more show hours and minutes") {
		RuntimeText.of(65) shouldBe "1h 5m"
	}

	test("Shorter durations show minutes only") {
		RuntimeText.of(32) shouldBe "32m"
	}
})

class MediaBadgesTests : FunSpec({
	test("Picture size, dynamic range, surround sound and subtitles each get a badge") {
		MediaBadges.of(media(3840, VideoRangeType.HDR10, channels = 6, hasSubtitles = true)) shouldContainExactly
			listOf("4K", "HDR10", "5.1", "CC")
	}

	test("Dolby Vision is named as such") {
		MediaBadges.of(media(3840, VideoRangeType.DOVI_WITH_HDR10)) shouldContainExactly listOf("4K", "Dolby Vision")
	}

	test("Full HD and seven channel audio") {
		MediaBadges.of(media(1920, VideoRangeType.SDR, channels = 8)) shouldContainExactly listOf("1080p", "7.1")
	}

	test("Stereo and standard range add nothing") {
		MediaBadges.of(media(1280, VideoRangeType.SDR, channels = 2)) shouldContainExactly listOf("720p")
	}

	test("An item without media information has no badges") {
		MediaBadges.of(media()) shouldContainExactly emptyList()
	}
})

class CastListTests : FunSpec({
	test("Actors come first, then directors and writers, and crew roles are left out") {
		val director = person("Director", PersonKind.DIRECTOR)
		val actor = person("Actor", PersonKind.ACTOR)
		val producer = person("Producer", PersonKind.PRODUCER)
		val writer = person("Writer", PersonKind.WRITER)

		CastList.of(listOf(director, producer, writer, actor)) shouldContainExactly listOf(actor, director, writer)
	}
})

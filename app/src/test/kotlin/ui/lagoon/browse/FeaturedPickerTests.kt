package org.jellyfin.androidtv.ui.lagoon.browse

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

private fun item(
	name: String,
	type: BaseItemKind = BaseItemKind.MOVIE,
	hasBackdrop: Boolean = true,
) = BaseItemDto(
	id = UUID.randomUUID(),
	type = type,
	name = name,
	backdropImageTags = if (hasBackdrop) listOf("tag") else emptyList(),
)

class FeaturedPickerTests : FunSpec({
	test("Picks alternate between shelves so one library does not fill the billboard") {
		val movieA = item("Movie A")
		val movieB = item("Movie B")
		val showA = item("Show A", BaseItemKind.SERIES)
		val showB = item("Show B", BaseItemKind.SERIES)

		FeaturedPicker.pick(listOf(listOf(movieA, movieB), listOf(showA, showB))) shouldContainExactly
			listOf(movieA, showA, movieB, showB)
	}

	test("Only movies and series with backdrop art are featured") {
		val withArt = item("With art")
		val withoutArt = item("Without art", hasBackdrop = false)
		val episode = item("Episode", BaseItemKind.EPISODE)

		FeaturedPicker.pick(listOf(listOf(withoutArt, episode, withArt))) shouldContainExactly listOf(withArt)
	}

	test("The same title appearing on two shelves is featured once") {
		val shared = item("Shared")

		FeaturedPicker.pick(listOf(listOf(shared), listOf(shared))) shouldContainExactly listOf(shared)
	}

	test("No more than the maximum number of titles are featured") {
		val many = (1..20).map { item("Movie $it") }

		FeaturedPicker.pick(listOf(many), maximum = 5) shouldHaveSize 5
	}
})

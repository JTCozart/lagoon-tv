package org.jellyfin.androidtv.ui.lagoon.browse

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType
import java.util.UUID

private val seriesId = UUID.randomUUID()

private fun episode(
	stillTag: String? = "still",
	seriesPosterTag: String? = "series-poster",
	seriesBackdropTag: String? = "series-backdrop",
	seriesLogoTag: String? = "series-logo",
) = BaseItemDto(
	id = UUID.randomUUID(),
	type = BaseItemKind.EPISODE,
	seriesId = seriesId,
	imageTags = stillTag?.let { mapOf(ImageType.PRIMARY to it) },
	seriesPrimaryImageTag = seriesPosterTag,
	parentBackdropItemId = seriesBackdropTag?.let { seriesId },
	parentBackdropImageTags = seriesBackdropTag?.let { listOf(it) },
	parentLogoItemId = seriesLogoTag?.let { seriesId },
	parentLogoImageTag = seriesLogoTag,
)

private fun movie(
	posterTag: String? = "poster",
	thumbTag: String? = null,
	backdropTag: String? = "backdrop",
) = BaseItemDto(
	id = UUID.randomUUID(),
	type = BaseItemKind.MOVIE,
	imageTags = buildMap {
		posterTag?.let { put(ImageType.PRIMARY, it) }
		thumbTag?.let { put(ImageType.THUMB, it) }
	},
	backdropImageTags = backdropTag?.let { listOf(it) },
)

class CardArtworkTests : FunSpec({
	test("An episode's wide card shows its own still") {
		CardArtwork.landscape(episode())?.tag shouldBe "still"
	}

	test("An episode without a still falls back to the series backdrop") {
		CardArtwork.landscape(episode(stillTag = null))?.tag shouldBe "series-backdrop"
	}

	test("A movie's wide card prefers its thumb, then its backdrop") {
		CardArtwork.landscape(movie(thumbTag = "thumb"))?.tag shouldBe "thumb"
		CardArtwork.landscape(movie())?.tag shouldBe "backdrop"
	}

	test("A movie's wide card never stretches a tall poster when there is no wide art") {
		CardArtwork.landscape(movie(backdropTag = null)) shouldBe null
	}

	test("An episode's poster is its series poster") {
		CardArtwork.poster(episode())?.tag shouldBe "series-poster"
	}

	test("Backdrop and logo come from the series for an episode") {
		CardArtwork.backdrop(episode())?.tag shouldBe "series-backdrop"
		CardArtwork.logo(episode())?.tag shouldBe "series-logo"
	}
})

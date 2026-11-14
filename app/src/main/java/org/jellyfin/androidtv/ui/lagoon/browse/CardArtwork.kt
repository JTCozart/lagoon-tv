package org.jellyfin.androidtv.ui.lagoon.browse

import org.jellyfin.androidtv.util.apiclient.JellyfinImage
import org.jellyfin.androidtv.util.apiclient.itemBackdropImages
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.androidtv.util.apiclient.parentBackdropImages
import org.jellyfin.androidtv.util.apiclient.parentImages
import org.jellyfin.androidtv.util.apiclient.seriesPrimaryImage
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType

/**
 * Picks which server image each kind of card shows for an item.
 */
object CardArtwork {
	fun landscape(item: BaseItemDto): JellyfinImage? =
		item.itemImages[ImageType.THUMB]
			?: episodeStill(item)
			?: item.itemBackdropImages.firstOrNull()
			?: item.parentImages[ImageType.THUMB]
			?: item.parentBackdropImages.firstOrNull()

	fun poster(item: BaseItemDto): JellyfinImage? = when (item.type) {
		BaseItemKind.EPISODE -> item.seriesPrimaryImage ?: item.itemImages[ImageType.PRIMARY]
		else -> item.itemImages[ImageType.PRIMARY]
	}

	fun backdrop(item: BaseItemDto): JellyfinImage? =
		item.itemBackdropImages.firstOrNull() ?: item.parentBackdropImages.firstOrNull()

	fun logo(item: BaseItemDto): JellyfinImage? =
		item.itemImages[ImageType.LOGO] ?: item.parentImages[ImageType.LOGO]

	private fun episodeStill(item: BaseItemDto): JellyfinImage? =
		if (item.type == BaseItemKind.EPISODE) item.itemImages[ImageType.PRIMARY] else null
}

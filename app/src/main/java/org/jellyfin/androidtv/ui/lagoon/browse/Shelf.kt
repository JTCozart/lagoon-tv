package org.jellyfin.androidtv.ui.lagoon.browse

import org.jellyfin.sdk.model.api.BaseItemDto

enum class ShelfCardStyle {
	/** Wide 16:9 stills with a progress bar, for things the user is part way through. */
	Landscape,

	/** Tall 2:3 posters, for browsing new arrivals. */
	Poster,
}

data class Shelf(
	val key: String,
	val title: String,
	val cardStyle: ShelfCardStyle,
	val items: List<BaseItemDto>,
)

package org.jellyfin.androidtv.ui.lagoon.browse

import org.jellyfin.sdk.model.api.BaseItemDto

data class Shelf(
	val key: String,
	val title: String,
	val cardStyle: ShelfCardStyle,
	val items: List<BaseItemDto>,
)

package org.jellyfin.androidtv.ui.lagoon.library

import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType

/**
 * Which libraries get the Lagoon library screen, and what kind of item each one lists.
 * Anything else keeps the upstream screen built for it (music, photos, Live TV and so on).
 */
object LibraryContent {
	fun itemKindsFor(collectionType: CollectionType?): List<BaseItemKind>? = when (collectionType) {
		CollectionType.MOVIES -> listOf(BaseItemKind.MOVIE)
		CollectionType.TVSHOWS -> listOf(BaseItemKind.SERIES)
		else -> null
	}
}

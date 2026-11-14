package org.jellyfin.androidtv.ui.lagoon.library

import org.jellyfin.androidtv.ui.lagoon.browse.Shelf
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind

interface LibraryLoader {
	suspend fun browseShelves(library: BaseItemDto, itemKind: BaseItemKind): List<Shelf>
	suspend fun page(library: BaseItemDto, itemKind: BaseItemKind, sort: LibrarySort, unwatchedOnly: Boolean, startIndex: Int): LibraryPage
}

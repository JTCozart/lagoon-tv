package org.jellyfin.androidtv.ui.lagoon.library

import org.jellyfin.sdk.model.api.BaseItemDto

data class LibraryPage(val items: List<BaseItemDto>, val totalCount: Int)

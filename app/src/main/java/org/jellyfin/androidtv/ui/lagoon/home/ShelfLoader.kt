package org.jellyfin.androidtv.ui.lagoon.home

import org.jellyfin.androidtv.ui.lagoon.browse.Shelf

interface ShelfLoader {
	suspend fun load(plan: ShelfPlan): Shelf
}

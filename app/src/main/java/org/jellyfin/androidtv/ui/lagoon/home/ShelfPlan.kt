package org.jellyfin.androidtv.ui.lagoon.home

import org.jellyfin.sdk.model.api.BaseItemDto

/**
 * One shelf the home screen intends to show, before its items are loaded.
 */
sealed interface ShelfPlan {
	data object ContinueWatching : ShelfPlan
	data object NextUp : ShelfPlan
	data class LatestIn(val library: BaseItemDto) : ShelfPlan
}

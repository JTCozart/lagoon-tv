package org.jellyfin.androidtv.ui.lagoon.browse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

/**
 * Finds what a Play button should start. A series plays its next unwatched episode rather than its first one,
 * which is what upstream's playback helper would do with the series itself.
 */
class PlayTargetResolver(private val api: ApiClient) {
	suspend fun resolve(item: BaseItemDto): UUID {
		if (item.type != BaseItemKind.SERIES) return item.id

		val nextEpisode = withContext(Dispatchers.IO) {
			api.tvShowsApi.getNextUp(seriesId = item.id, limit = 1, enableResumable = true).content.items.firstOrNull()
		}
		return nextEpisode?.id ?: item.id
	}
}

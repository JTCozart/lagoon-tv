package org.jellyfin.androidtv.ui.lagoon.details

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.libraryApi
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ItemFields
import java.util.UUID

class JellyfinDetailsLoader(private val api: ApiClient) : DetailsLoader {
	private companion object {
		const val SimilarLimit = 20
		val episodeFields = ItemRepository.browseFields + ItemFields.MEDIA_SOURCES
	}

	override suspend fun item(id: UUID): BaseItemDto = withContext(Dispatchers.IO) {
		api.userLibraryApi.getItem(itemId = id).content
	}

	override suspend fun seasons(seriesId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
		api.tvShowsApi.getSeasons(seriesId = seriesId, fields = ItemRepository.browseFields).content.items
	}

	override suspend fun episodes(seriesId: UUID, seasonId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
		api.tvShowsApi.getEpisodes(seriesId = seriesId, seasonId = seasonId, fields = episodeFields).content.items
	}

	override suspend fun nextEpisode(seriesId: UUID): BaseItemDto? = withContext(Dispatchers.IO) {
		api.tvShowsApi.getNextUp(seriesId = seriesId, limit = 1, enableResumable = true, fields = ItemRepository.browseFields)
			.content.items.firstOrNull()
	}

	override suspend fun similar(id: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
		api.libraryApi.getSimilarItems(itemId = id, limit = SimilarLimit, fields = ItemRepository.browseFields).content.items
	}
}

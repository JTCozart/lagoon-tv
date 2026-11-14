package org.jellyfin.androidtv.ui.lagoon.library

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.ui.lagoon.browse.Shelf
import org.jellyfin.androidtv.ui.lagoon.browse.ShelfCardStyle
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.exception.ApiClientException
import org.jellyfin.sdk.api.client.extensions.genresApi
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.ItemFilter
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import timber.log.Timber

data class LibraryPage(val items: List<BaseItemDto>, val totalCount: Int)

interface LibraryLoader {
	suspend fun browseShelves(library: BaseItemDto, itemKind: BaseItemKind): List<Shelf>
	suspend fun page(library: BaseItemDto, itemKind: BaseItemKind, sort: LibrarySort, unwatchedOnly: Boolean, startIndex: Int): LibraryPage
}

class JellyfinLibraryLoader(
	private val context: Context,
	private val api: ApiClient,
) : LibraryLoader {
	private companion object {
		const val ShelfLimit = 30
		const val GenreShelfLimit = 24
		const val GenreShelfCount = 8
		const val PageSize = 60
	}

	override suspend fun browseShelves(library: BaseItemDto, itemKind: BaseItemKind): List<Shelf> = withContext(Dispatchers.IO) {
		coroutineScope {
			val fixedShelves = listOf(
				async { orNull { continueWatching(library) } },
				async { if (itemKind == BaseItemKind.SERIES) orNull { nextUp(library) } else null },
				async { orNull { recentlyAdded(library) } },
				async { orNull { topRated(library, itemKind) } },
			)
			val genreShelves = orNull { genreShelves(library, itemKind) }.orEmpty()

			(fixedShelves.awaitAll().filterNotNull() + genreShelves).filter { it.items.isNotEmpty() }
		}
	}

	override suspend fun page(
		library: BaseItemDto,
		itemKind: BaseItemKind,
		sort: LibrarySort,
		unwatchedOnly: Boolean,
		startIndex: Int,
	): LibraryPage = withContext(Dispatchers.IO) {
		val result = api.itemsApi.getItems(
			parentId = library.id,
			recursive = true,
			includeItemTypes = listOf(itemKind),
			sortBy = listOf(sort.sortBy, ItemSortBy.SORT_NAME),
			sortOrder = listOf(sort.order, SortOrder.ASCENDING),
			filters = if (unwatchedOnly) listOf(ItemFilter.IS_UNPLAYED) else null,
			fields = ItemRepository.browseFields,
			startIndex = startIndex,
			limit = PageSize,
			enableTotalRecordCount = true,
		).content

		LibraryPage(result.items, result.totalRecordCount)
	}

	private suspend fun continueWatching(library: BaseItemDto): Shelf {
		val items = api.itemsApi.getResumeItems(
			parentId = library.id,
			fields = ItemRepository.browseFields,
			limit = ShelfLimit,
			enableTotalRecordCount = false,
		).content.items

		return Shelf("continue", context.getString(R.string.lbl_continue_watching), ShelfCardStyle.Landscape, items)
	}

	private suspend fun nextUp(library: BaseItemDto): Shelf {
		val items = api.tvShowsApi.getNextUp(
			parentId = library.id,
			fields = ItemRepository.browseFields,
			limit = ShelfLimit,
			enableResumable = false,
		).content.items

		return Shelf("nextup", context.getString(R.string.lbl_next_up), ShelfCardStyle.Landscape, items)
	}

	private suspend fun recentlyAdded(library: BaseItemDto): Shelf {
		val items = api.userLibraryApi.getLatestMedia(
			parentId = library.id,
			fields = ItemRepository.browseFields,
			groupItems = true,
			limit = ShelfLimit,
		).content

		return Shelf("recent", context.getString(R.string.lagoon_shelf_recently_added), ShelfCardStyle.Poster, items)
	}

	private suspend fun topRated(library: BaseItemDto, itemKind: BaseItemKind): Shelf {
		val items = api.itemsApi.getItems(
			parentId = library.id,
			recursive = true,
			includeItemTypes = listOf(itemKind),
			sortBy = listOf(ItemSortBy.COMMUNITY_RATING),
			sortOrder = listOf(SortOrder.DESCENDING),
			fields = ItemRepository.browseFields,
			limit = ShelfLimit,
			enableTotalRecordCount = false,
		).content.items

		return Shelf("toprated", context.getString(R.string.lagoon_shelf_top_rated), ShelfCardStyle.Poster, items)
	}

	private suspend fun genreShelves(library: BaseItemDto, itemKind: BaseItemKind): List<Shelf> = coroutineScope {
		val genres = api.genresApi.getGenres(
			parentId = library.id,
			includeItemTypes = listOf(itemKind),
			fields = listOf(ItemFields.ITEM_COUNTS),
			sortBy = listOf(ItemSortBy.SORT_NAME),
		).content.items

		// Some servers leave the counts empty; then fall back to the first genres by name
		val chosen = GenreRanker.top(genres, itemKind, GenreShelfCount).ifEmpty { genres.take(GenreShelfCount) }

		chosen.map { genre ->
			async {
				orNull {
					val items = api.itemsApi.getItems(
						parentId = library.id,
						recursive = true,
						includeItemTypes = listOf(itemKind),
						genreIds = listOf(genre.id),
						sortBy = listOf(ItemSortBy.COMMUNITY_RATING),
						sortOrder = listOf(SortOrder.DESCENDING),
						fields = ItemRepository.browseFields,
						limit = GenreShelfLimit,
						enableTotalRecordCount = false,
					).content.items
					Shelf("genre-${genre.id}", genre.name.orEmpty(), ShelfCardStyle.Poster, items)
				}
			}
		}.awaitAll().filterNotNull()
	}

	private inline fun <T> orNull(load: () -> T): T? = try {
		load()
	} catch (error: ApiClientException) {
		// One failing shelf should not blank the whole page
		Timber.w(error, "Library shelf failed to load")
		null
	}
}

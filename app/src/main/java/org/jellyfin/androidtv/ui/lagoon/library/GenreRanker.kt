package org.jellyfin.androidtv.ui.lagoon.library

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind

/**
 * Picks the genres that get their own row on a library's browse page: the biggest ones first.
 */
object GenreRanker {
	fun top(genres: List<BaseItemDto>, itemKind: BaseItemKind, maximum: Int): List<BaseItemDto> =
		genres
			.map { genre -> genre to countOf(genre, itemKind) }
			.filter { (_, count) -> count > 0 }
			.sortedByDescending { (_, count) -> count }
			.take(maximum)
			.map { (genre, _) -> genre }

	private fun countOf(genre: BaseItemDto, itemKind: BaseItemKind): Int = when (itemKind) {
		BaseItemKind.SERIES -> genre.seriesCount
		else -> genre.movieCount
	} ?: 0
}

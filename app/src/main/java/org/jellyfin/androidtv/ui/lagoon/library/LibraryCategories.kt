package org.jellyfin.androidtv.ui.lagoon.library

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind

/** Turns a library's genres into the category list beside the All titles grid. */
object LibraryCategories {
	fun from(genres: List<BaseItemDto>, itemKind: BaseItemKind): List<LibraryCategory> {
		val named = genres.filter { !it.name.isNullOrBlank() }
		// Some servers leave the counts empty; then every genre is offered rather than none
		val hasCounts = named.any { countOf(it, itemKind) != null }
		val offered = if (hasCounts) named.filter { (countOf(it, itemKind) ?: 0) > 0 } else named

		return offered
			.map { LibraryCategory(it.id, it.name.orEmpty()) }
			.sortedBy { it.name.lowercase() }
	}

	private fun countOf(genre: BaseItemDto, itemKind: BaseItemKind): Int? = when (itemKind) {
		BaseItemKind.SERIES -> genre.seriesCount
		else -> genre.movieCount
	}
}

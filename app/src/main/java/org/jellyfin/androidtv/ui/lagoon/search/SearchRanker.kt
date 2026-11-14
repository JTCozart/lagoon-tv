package org.jellyfin.androidtv.ui.lagoon.search

import org.jellyfin.sdk.model.api.BaseItemDto

/**
 * Orders search results the way people expect when typing a title: an exact match first, then titles that start
 * with what was typed, then titles where a later word starts with it, then any other match. The server only sorts by name.
 */
object SearchRanker {
	private val leadingArticles = listOf("the ", "a ", "an ")

	fun rank(items: List<BaseItemDto>, query: String): List<BaseItemDto> {
		val wanted = query.trim().lowercase()
		return items.sortedWith(
			compareBy<BaseItemDto> { matchStrength(normalized(it.name), wanted) }
				.thenBy { normalized(it.name) }
		)
	}

	private fun matchStrength(name: String, wanted: String): Int = when {
		name == wanted -> 0
		name.startsWith(wanted) -> 1
		name.split(' ').any { word -> word.startsWith(wanted) } -> 2
		else -> 3
	}

	private fun normalized(name: String?): String {
		val lower = name.orEmpty().trim().lowercase()
		val article = leadingArticles.firstOrNull { lower.startsWith(it) }
		return if (article != null) lower.removePrefix(article) else lower
	}
}

package org.jellyfin.androidtv.ui.lagoon.search

/**
 * Edits a search query one key at a time, the way the on-screen keyboard and a remote's number pad do.
 */
object QueryEditor {
	const val MaximumLength = 60

	fun type(query: String, character: Char): String = when {
		query.length >= MaximumLength -> query
		character == ' ' && (query.isEmpty() || query.endsWith(' ')) -> query
		else -> query + character
	}

	fun deleteLast(query: String): String = query.dropLast(1)
}

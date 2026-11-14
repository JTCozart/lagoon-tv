package org.jellyfin.androidtv.ui.lagoon.browse

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.Locale

/**
 * Turns a library item into the short lines of text shown in the home screen hero panel.
 */
object HeroMetadata {
	private const val TicksPerMinute = 600_000_000L
	private const val MinutesPerHour = 60L
	private const val MaxGenres = 3

	fun titleOf(item: BaseItemDto): String = when {
		item.type == BaseItemKind.EPISODE && !item.seriesName.isNullOrBlank() -> item.seriesName.orEmpty()
		else -> item.name.orEmpty()
	}

	fun subtitleOf(item: BaseItemDto): String? {
		if (item.type != BaseItemKind.EPISODE) return null

		val position = listOfNotNull(
			item.parentIndexNumber?.let { "S$it" },
			item.indexNumber?.let { "E$it" },
		).joinToString(" ")

		return listOf(position, item.name.orEmpty())
			.filter { it.isNotBlank() }
			.joinToString("  ")
			.ifBlank { null }
	}

	fun detailsOf(item: BaseItemDto): List<String> = buildList {
		item.productionYear?.let { add(it.toString()) }
		item.officialRating?.takeIf { it.isNotBlank() }?.let(::add)
		item.runTimeTicks?.let(::runtimeOf)?.let(::add)
		item.communityRating?.takeIf { it > 0f }?.let { add("★ " + String.format(Locale.US, "%.1f", it)) }
		item.genres.orEmpty().filter { it.isNotBlank() }.take(MaxGenres).forEach(::add)
	}

	private fun runtimeOf(ticks: Long): String? {
		val totalMinutes = ticks / TicksPerMinute
		if (totalMinutes <= 0) return null

		val hours = totalMinutes / MinutesPerHour
		val minutes = totalMinutes % MinutesPerHour
		return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
	}
}

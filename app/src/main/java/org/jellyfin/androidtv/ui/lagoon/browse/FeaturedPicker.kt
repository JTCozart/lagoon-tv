package org.jellyfin.androidtv.ui.lagoon.browse

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind

/**
 * Chooses the titles that rotate through the billboard at the top of the home screen.
 */
object FeaturedPicker {
	const val DefaultMaximum = 6

	private val featurableKinds = setOf(BaseItemKind.MOVIE, BaseItemKind.SERIES)

	fun pick(shelves: List<List<BaseItemDto>>, maximum: Int = DefaultMaximum): List<BaseItemDto> =
		interleave(shelves.map { shelf -> shelf.filter(::isFeaturable) })
			.distinctBy { it.id }
			.take(maximum)

	private fun isFeaturable(item: BaseItemDto) =
		item.type in featurableKinds && !item.backdropImageTags.isNullOrEmpty()

	private fun interleave(lists: List<List<BaseItemDto>>): List<BaseItemDto> {
		val longest = lists.maxOfOrNull { it.size } ?: 0
		return (0 until longest).flatMap { index -> lists.mapNotNull { it.getOrNull(index) } }
	}
}

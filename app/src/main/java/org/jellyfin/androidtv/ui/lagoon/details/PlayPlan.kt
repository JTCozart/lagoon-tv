package org.jellyfin.androidtv.ui.lagoon.details

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind

/**
 * What the main button on a details page will do: which item it plays, whether it resumes, and how it is labelled.
 */
data class PlayPlan(
	val target: BaseItemDto,
	val isResume: Boolean,
	val remainingMinutes: Long?,
	val episodeLabel: String?,
) {
	companion object {
		private const val TicksPerMinute = 600_000_000L

		fun of(target: BaseItemDto): PlayPlan {
			val position = target.userData?.playbackPositionTicks ?: 0L
			val runtime = target.runTimeTicks
			val isResume = position > 0L

			return PlayPlan(
				target = target,
				isResume = isResume,
				remainingMinutes = if (isResume && runtime != null) (runtime - position).coerceAtLeast(0L) / TicksPerMinute else null,
				episodeLabel = episodeLabelOf(target),
			)
		}

		private fun episodeLabelOf(item: BaseItemDto): String? {
			if (item.type != BaseItemKind.EPISODE) return null
			return listOfNotNull(item.parentIndexNumber?.let { "S$it" }, item.indexNumber?.let { "E$it" })
				.joinToString(" ")
				.ifBlank { null }
		}
	}
}

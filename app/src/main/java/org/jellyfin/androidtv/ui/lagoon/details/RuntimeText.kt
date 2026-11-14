package org.jellyfin.androidtv.ui.lagoon.details

object RuntimeText {
	private const val MinutesPerHour = 60L

	fun of(totalMinutes: Long): String {
		val hours = totalMinutes / MinutesPerHour
		val minutes = totalMinutes % MinutesPerHour
		return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
	}
}

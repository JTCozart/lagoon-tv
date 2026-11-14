package org.jellyfin.androidtv.ui.lagoon.player

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * The times shown on the player: position, time left and the wall-clock time the title will finish.
 */
object PlayerTime {
	private const val MillisPerSecond = 1_000L
	private const val SecondsPerMinute = 60L
	private const val SecondsPerHour = 3_600L

	fun clock(ms: Long, includeHours: Boolean): String {
		val totalSeconds = ms.coerceAtLeast(0L) / MillisPerSecond
		val hours = totalSeconds / SecondsPerHour
		val minutes = (totalSeconds % SecondsPerHour) / SecondsPerMinute
		val seconds = totalSeconds % SecondsPerMinute
		return if (includeHours || hours > 0) "%d:%02d:%02d".format(Locale.US, hours, minutes, seconds)
		else "%d:%02d".format(Locale.US, minutes, seconds)
	}

	fun remaining(positionMs: Long, durationMs: Long): String =
		"-" + clock((durationMs - positionMs).coerceAtLeast(0L), includeHours = durationMs >= SecondsPerHour * MillisPerSecond)

	fun endsAt(now: Instant, zone: ZoneId, remainingMs: Long, speed: Float, locale: Locale): String {
		val wallClockMs = (remainingMs.coerceAtLeast(0L) / speed.coerceAtLeast(MinimumSpeed)).toLong()
		val end = now.plusMillis(wallClockMs).atZone(zone)
		return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale).format(end)
	}

	private const val MinimumSpeed = 0.1f
}

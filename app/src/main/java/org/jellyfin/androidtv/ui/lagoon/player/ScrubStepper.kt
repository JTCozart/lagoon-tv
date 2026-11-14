package org.jellyfin.androidtv.ui.lagoon.player

/**
 * How far one press of Left or Right moves the scrub position. Taps move in small steps; holding the button
 * repeats the press and the steps grow, so crossing a two hour film takes seconds rather than minutes.
 */
object ScrubStepper {
	private const val Second = 1_000L
	private const val SmallStep = 10 * Second
	private const val MediumStep = 30 * Second
	private const val LargeStep = 120 * Second
	private const val MediumAfterRepeats = 5
	private const val LargeAfterRepeats = 15

	fun stepFor(repeat: Int): Long = when {
		repeat >= LargeAfterRepeats -> LargeStep
		repeat >= MediumAfterRepeats -> MediumStep
		else -> SmallStep
	}

	fun move(from: Long, direction: Int, repeat: Int, durationMs: Long): Long =
		(from + direction * stepFor(repeat)).coerceIn(0L, durationMs.coerceAtLeast(0L))
}

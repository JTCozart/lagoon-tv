package org.jellyfin.androidtv.ui.lagoon.player

/** Pausing brings the controls up so the viewer sees where they are, but only at the moment playback pauses. */
object ControlsOnPause {
	fun shouldShow(wasPaused: Boolean, isPaused: Boolean, isVisible: Boolean): Boolean =
		isPaused && !wasPaused && !isVisible
}

package org.jellyfin.androidtv.ui.lagoon.player

/** What the player overlay knows when a key arrives. */
data class OverlayKeyContext(
	val isVisible: Boolean,
	val isPanelOpen: Boolean,
	val isScrubbing: Boolean,
	val canSeek: Boolean,
	val canShowControls: Boolean,
)

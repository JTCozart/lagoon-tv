package org.jellyfin.androidtv.ui.lagoon.player

/** Where a remote key goes in the player. */
sealed interface KeyRoute {
	data object ClosePanel : KeyRoute
	data object HideControls : KeyRoute

	/** Move the scrub position; [direction] is 1 for forward and -1 for back. */
	data class Scrub(val direction: Int) : KeyRoute

	/** Offer the key to upstream's player first: skip intro, media keys, Back to exit, stop. */
	data object AskPlayer : KeyRoute
	data object ShowControls : KeyRoute
	data object PassToControls : KeyRoute
}

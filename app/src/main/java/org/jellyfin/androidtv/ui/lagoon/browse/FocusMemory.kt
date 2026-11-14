package org.jellyfin.androidtv.ui.lagoon.browse

/** Held by a screen's ViewModel, which outlives the screen's views while details or the player are open. */
interface FocusMemory {
	/** Null while the billboard has focus. */
	var lastFocus: BrowseFocus?
}

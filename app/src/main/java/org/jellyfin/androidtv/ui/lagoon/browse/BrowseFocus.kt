package org.jellyfin.androidtv.ui.lagoon.browse

/** Where focus was when the user left a browse screen, so it can be put back when they return. */
data class BrowseFocus(val shelfIndex: Int, val itemIndex: Int)

/** Held by a screen's ViewModel, which outlives the screen's views while details or the player are open. */
interface FocusMemory {
	/** Null while the billboard has focus. */
	var lastFocus: BrowseFocus?
}

package org.jellyfin.androidtv.ui.lagoon.browse

/** Where focus was when the user left a browse screen, so it can be put back when they return. */
data class BrowseFocus(val shelfIndex: Int, val itemIndex: Int)

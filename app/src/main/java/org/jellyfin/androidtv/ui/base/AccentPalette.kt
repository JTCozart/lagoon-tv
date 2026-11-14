package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** The accent colors one app theme swaps in; the navy base stays the same in every theme. */
@Immutable
data class AccentPalette(
	val accent: Color,
	val accentDeep: Color,
	val accentSecondary: Color,
)

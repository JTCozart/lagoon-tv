package org.jellyfin.androidtv.ui.base

import androidx.compose.ui.graphics.Color
import org.jellyfin.androidtv.preference.constant.AppTheme

/** One accent palette per App theme setting. */
object AccentPalettes {
	val Lagoon = AccentPalette(
		accent = LagoonColors.Teal,
		accentDeep = LagoonColors.TealDeep,
		accentSecondary = LagoonColors.Blue,
	)

	val Emerald = AccentPalette(
		accent = Color(0xFF52B54B),
		accentDeep = Color(0xFF3E9A38),
		accentSecondary = Color(0xFF2BB59A),
	)

	val MutedPurple = AccentPalette(
		accent = Color(0xFF7C93D9),
		accentDeep = Color(0xFF627EC8),
		accentSecondary = Color(0xFF9B7BD1),
	)

	fun forTheme(theme: AppTheme): AccentPalette = when (theme) {
		AppTheme.DARK -> Lagoon
		AppTheme.EMERALD -> Emerald
		AppTheme.MUTED_PURPLE -> MutedPurple
	}
}

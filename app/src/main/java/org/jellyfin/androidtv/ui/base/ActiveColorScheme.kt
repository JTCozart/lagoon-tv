package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.jellyfin.androidtv.preference.constant.AppTheme

/**
 * The color scheme for the App theme setting. It is snapshot state, so every Compose screen that has not been
 * given its own scheme recolors as soon as the setting changes, without recreating the activity.
 */
object ActiveColorScheme {
	var current: ColorScheme by mutableStateOf(colorScheme())
		private set

	fun use(theme: AppTheme) {
		val scheme = colorScheme(AccentPalettes.forTheme(theme))
		if (scheme != current) current = scheme
	}
}

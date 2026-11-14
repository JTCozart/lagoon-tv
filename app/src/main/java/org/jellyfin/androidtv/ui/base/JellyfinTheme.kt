package org.jellyfin.androidtv.ui.base

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Leave [colorScheme] out to follow the App theme setting live; a scheme passed here is fixed for its content.
 */
@Composable
fun JellyfinTheme(
	colorScheme: ColorScheme? = LocalColorScheme.current,
	shapes: Shapes = JellyfinTheme.shapes,
	typography: Typography = JellyfinTheme.typography,
	content: @Composable () -> Unit
) {
	CompositionLocalProvider(
		LocalColorScheme provides colorScheme,
		LocalShapes provides shapes,
		LocalTypography provides typography,
		LocalTextStyle provides LocalTextStyle.current.merge(typography.default),
		// Disable overscroll
		LocalOverscrollFactory provides null,
	) {
		content()
	}
}

object JellyfinTheme {
	val colorScheme: ColorScheme
		@Composable @ReadOnlyComposable get() = LocalColorScheme.current ?: ActiveColorScheme.current

	val typography: Typography
		@Composable @ReadOnlyComposable get() = LocalTypography.current

	val shapes: Shapes
		@Composable @ReadOnlyComposable get() = LocalShapes.current
}

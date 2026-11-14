package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import org.jellyfin.design.Tokens

/**
 * The Lagoon palette: a deep navy base with a teal to blue accent. Other app themes swap the accent (see [AccentPalettes]).
 * Focused controls take the accent so the remote's position is obvious from across the room.
 */
object LagoonColors {
	val Teal = Color(0xFF2DD4BF)
	val TealDeep = Color(0xFF14B8A6)
	val Blue = Color(0xFF3B82F6)
	val Navy = Color(0xFF060B12)
	val NavySurface = Color(0xFF0F1A24)
	val NavyRaised = Color(0xFF17283A)
	val Ink = Color(0xFF04121A)
}

fun colorScheme(palette: AccentPalette = AccentPalettes.Lagoon): ColorScheme = ColorScheme(
	background = LagoonColors.Navy,
	onBackground = Tokens.Color.colorBluegrey25,
	accent = palette.accent,
	accentSecondary = palette.accentSecondary,
	onAccent = LagoonColors.Ink,
	button = LagoonColors.NavyRaised.copy(alpha = 0.85f),
	onButton = Color(0xFFDDE6EE),
	buttonFocused = palette.accent,
	onButtonFocused = LagoonColors.Ink,
	buttonDisabled = Color(0x33747474),
	onButtonDisabled = Color(0xFF686868),
	buttonActive = palette.accentDeep.copy(alpha = 0.3f),
	onButtonActive = palette.accent,
	input = LagoonColors.NavyRaised.copy(alpha = 0.85f),
	onInput = Color(0xE6CCCCCC),
	inputFocused = palette.accent,
	onInputFocused = LagoonColors.Ink,
	rangeControlBackground = Tokens.Color.colorBluegrey700,
	rangeControlFill = palette.accent,
	rangeControlKnob = Tokens.Color.colorBluegrey100,
	seekbarBuffer = Tokens.Color.colorBluegrey300,
	recording = Tokens.Color.colorRed300,
	onRecording = Tokens.Color.colorRed25,
	badge = palette.accentDeep,
	onBadge = LagoonColors.Ink,
	listHeader = Tokens.Color.colorGrey50,
	listOverline = palette.accent,
	listHeadline = Tokens.Color.colorGrey25,
	listCaption = Tokens.Color.colorGrey200,
	listButton = Color.Transparent,
	listButtonFocused = palette.accentDeep.copy(alpha = 0.35f),
	surface = LagoonColors.NavySurface,
	scrim = LagoonColors.Navy.copy(alpha = 0.75f),
)

@Immutable
data class ColorScheme(
	val background: Color,
	val onBackground: Color,

	val accent: Color,
	val accentSecondary: Color,
	val onAccent: Color,

	val button: Color,
	val onButton: Color,
	val buttonFocused: Color,
	val onButtonFocused: Color,
	val buttonDisabled: Color,
	val onButtonDisabled: Color,
	val buttonActive: Color,
	val onButtonActive: Color,

	val input: Color,
	val onInput: Color,
	val inputFocused: Color,
	val onInputFocused: Color,

	val rangeControlBackground: Color,
	val rangeControlFill: Color,
	val rangeControlKnob: Color,
	val seekbarBuffer: Color,

	val recording: Color,
	val onRecording: Color,

	val badge: Color,
	val onBadge: Color,

	val listHeader: Color,
	val listOverline: Color,
	val listHeadline: Color,
	val listCaption: Color,
	val listButton: Color,
	val listButtonFocused: Color,

	val surface: Color,
	val scrim: Color,
)

/** Null until a screen provides its own scheme; [JellyfinTheme.colorScheme] then falls back to [ActiveColorScheme]. */
val LocalColorScheme = staticCompositionLocalOf<ColorScheme?> { null }

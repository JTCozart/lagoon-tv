package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import org.jellyfin.design.Tokens

/**
 * The Lagoon palette: a deep navy base with a teal to blue accent.
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

fun colorScheme(): ColorScheme = ColorScheme(
	background = LagoonColors.Navy,
	onBackground = Tokens.Color.colorBluegrey25,
	accent = LagoonColors.Teal,
	accentSecondary = LagoonColors.Blue,
	onAccent = LagoonColors.Ink,
	button = LagoonColors.NavyRaised.copy(alpha = 0.85f),
	onButton = Color(0xFFDDE6EE),
	buttonFocused = LagoonColors.Teal,
	onButtonFocused = LagoonColors.Ink,
	buttonDisabled = Color(0x33747474),
	onButtonDisabled = Color(0xFF686868),
	buttonActive = LagoonColors.TealDeep.copy(alpha = 0.3f),
	onButtonActive = LagoonColors.Teal,
	input = LagoonColors.NavyRaised.copy(alpha = 0.85f),
	onInput = Color(0xE6CCCCCC),
	inputFocused = LagoonColors.Teal,
	onInputFocused = LagoonColors.Ink,
	rangeControlBackground = Tokens.Color.colorBluegrey700,
	rangeControlFill = LagoonColors.Teal,
	rangeControlKnob = Tokens.Color.colorBluegrey100,
	seekbarBuffer = Tokens.Color.colorBluegrey300,
	recording = Tokens.Color.colorRed300,
	onRecording = Tokens.Color.colorRed25,
	badge = LagoonColors.TealDeep,
	onBadge = LagoonColors.Ink,
	listHeader = Tokens.Color.colorGrey50,
	listOverline = LagoonColors.Teal,
	listHeadline = Tokens.Color.colorGrey25,
	listCaption = Tokens.Color.colorGrey200,
	listButton = Color.Transparent,
	listButtonFocused = LagoonColors.TealDeep.copy(alpha = 0.35f),
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

val LocalColorScheme = staticCompositionLocalOf { colorScheme() }

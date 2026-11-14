package org.jellyfin.androidtv.ui.lagoon.player

import android.view.KeyEvent

/**
 * Decides where a remote key goes in the player, in two steps around upstream's own key listener:
 * [beforePlayer] handles what Lagoon's controls must own outright, and [afterPlayerDeclined] handles keys the
 * player did not use.
 */
object PlayerKeyRouter {
	private val backKeys = setOf(KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_ESCAPE)
	private val showKeys = setOf(
		KeyEvent.KEYCODE_DPAD_UP,
		KeyEvent.KEYCODE_DPAD_DOWN,
		KeyEvent.KEYCODE_DPAD_CENTER,
		KeyEvent.KEYCODE_ENTER,
		KeyEvent.KEYCODE_MENU,
	)

	fun beforePlayer(keyCode: Int, context: OverlayKeyContext): KeyRoute {
		if (context.isVisible && keyCode in backKeys) {
			return if (context.isPanelOpen) KeyRoute.ClosePanel else KeyRoute.HideControls
		}

		val direction = scrubDirectionOf(keyCode)
		if (direction != null && canScrub(context)) return KeyRoute.Scrub(direction)

		return KeyRoute.AskPlayer
	}

	fun afterPlayerDeclined(keyCode: Int, context: OverlayKeyContext): KeyRoute =
		if (!context.isVisible && keyCode in showKeys) KeyRoute.ShowControls else KeyRoute.PassToControls

	// Hidden controls scrub straight away. Once a scrub is under way the keys keep moving it, so quick presses
	// count even before the seek bar has taken focus. An open panel keeps Left and Right for its own lists.
	private fun canScrub(context: OverlayKeyContext): Boolean {
		val canStart = !context.isVisible && context.canSeek && context.canShowControls
		val isContinuing = context.isVisible && context.isScrubbing && !context.isPanelOpen
		return canStart || isContinuing
	}

	private fun scrubDirectionOf(keyCode: Int): Int? = when (keyCode) {
		KeyEvent.KEYCODE_DPAD_RIGHT -> 1
		KeyEvent.KEYCODE_DPAD_LEFT -> -1
		else -> null
	}
}

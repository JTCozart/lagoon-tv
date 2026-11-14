package org.jellyfin.androidtv.ui.lagoon.player

import android.content.Context
import android.view.KeyEvent
import android.widget.FrameLayout

/** Sees every key before the views inside it, so the player can act on keys whatever has focus. */
internal class KeyInterceptLayout(
	context: Context,
	private val onKey: (KeyEvent) -> Boolean,
) : FrameLayout(context) {
	override fun dispatchKeyEvent(event: KeyEvent): Boolean = onKey(event) || super.dispatchKeyEvent(event)
}

package org.jellyfin.androidtv.ui.lagoon.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.delay

private const val CheckGapMillis = 50L
private const val MaximumChecks = 30
private const val SettledChecks = 8

/**
 * Puts focus on [requester] when a screen appears or comes back from another screen.
 *
 * Android also hands focus to the newly attached view, sometimes only after the screen's fade-in has finished,
 * so a single request can be overruled. This keeps checking for a moment and asks again until focus has
 * settled inside the content.
 */
@Composable
fun RequestInitialFocus(
	key: Any?,
	requester: () -> FocusRequester?,
	isFocusInsideContent: State<Boolean>,
	isEnabled: Boolean = true,
) {
	LaunchedEffect(key, isEnabled) {
		if (!isEnabled) return@LaunchedEffect
		var settledFor = 0
		repeat(MaximumChecks) {
			withFrameNanos { }
			if (isFocusInsideContent.value) {
				settledFor++
				if (settledFor >= SettledChecks) return@LaunchedEffect
			} else {
				settledFor = 0
				requester()?.let { runCatching { it.requestFocus() } }
			}
			delay(CheckGapMillis)
		}
	}
}

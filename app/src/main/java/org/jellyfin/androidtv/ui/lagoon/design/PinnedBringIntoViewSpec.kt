package org.jellyfin.androidtv.ui.lagoon.design

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private val PinDeadZone = 14.dp

/**
 * Scrolls a list so the focused child always lands at the same spot near the leading edge, the way
 * streaming apps keep the focused poster in a fixed column while the row slides underneath it.
 * The default behaviour only scrolls the minimum needed, which makes focus wander across the screen.
 * With [pinVisibleChildren] off, a child that is already fully on screen does not move the list.
 */
@OptIn(ExperimentalFoundationApi::class)
internal class PinnedBringIntoViewSpec(
	private val leadingEdgePx: Float,
	private val pinVisibleChildren: Boolean,
	private val deadZonePx: Float,
) : BringIntoViewSpec {
	override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
		val isFullyVisible = offset >= 0f && offset + size <= containerSize
		if (isFullyVisible && !pinVisibleChildren) return 0f

		val distance = offset - leadingEdgePx
		// A focused card grows, which moves its bounds a few pixels; chasing that makes the list twitch on every move
		return if (isFullyVisible && abs(distance) < deadZonePx) 0f else distance
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinFocusedChild(
	leadingEdge: Dp,
	pinVisibleChildren: Boolean = true,
	content: @Composable () -> Unit,
) {
	val leadingEdgePx = with(LocalDensity.current) { leadingEdge.toPx() }
	val deadZonePx = with(LocalDensity.current) { PinDeadZone.toPx() }
	val spec = remember(leadingEdgePx, pinVisibleChildren, deadZonePx) {
		PinnedBringIntoViewSpec(leadingEdgePx, pinVisibleChildren, deadZonePx)
	}
	CompositionLocalProvider(LocalBringIntoViewSpec provides spec, content = content)
}

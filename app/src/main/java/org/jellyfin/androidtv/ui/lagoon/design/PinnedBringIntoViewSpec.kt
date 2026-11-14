package org.jellyfin.androidtv.ui.lagoon.design

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Scrolls a list so the focused child always lands at the same spot near the leading edge, the way
 * streaming apps keep the focused poster in a fixed column while the row slides underneath it.
 * The default behaviour only scrolls the minimum needed, which makes focus wander across the screen.
 * With [pinVisibleChildren] off, a child that is already fully on screen does not move the list.
 */
@OptIn(ExperimentalFoundationApi::class)
private class PinnedBringIntoViewSpec(
	private val leadingEdgePx: Float,
	private val pinVisibleChildren: Boolean,
) : BringIntoViewSpec {
	override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
		val isFullyVisible = offset >= 0f && offset + size <= containerSize
		return if (isFullyVisible && !pinVisibleChildren) 0f else offset - leadingEdgePx
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
	val spec = remember(leadingEdgePx, pinVisibleChildren) { PinnedBringIntoViewSpec(leadingEdgePx, pinVisibleChildren) }
	CompositionLocalProvider(LocalBringIntoViewSpec provides spec, content = content)
}

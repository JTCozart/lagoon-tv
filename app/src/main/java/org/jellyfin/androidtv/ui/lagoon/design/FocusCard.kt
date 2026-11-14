package org.jellyfin.androidtv.ui.lagoon.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.jellyfin.androidtv.ui.base.JellyfinTheme

val CardShape = RoundedCornerShape(10.dp)

private const val FocusAnimationMillis = 160
private val FocusedGlow = 18.dp
private val FocusedRing = 3.dp

/**
 * A remote-friendly card: it grows, lifts and gains an accent ring when focused, and clicks on the select key.
 */
@Composable
fun FocusCard(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onFocused: () -> Unit = {},
	content: @Composable BoxScope.(isFocused: Boolean) -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val focusSpec = tween<Float>(FocusAnimationMillis, easing = FastOutSlowInEasing)
	val scale by animateFloatAsState(if (isFocused) LagoonDimens.FocusedCardScale else 1f, focusSpec, label = "CardScale")
	// The ring and glow ease in with the growth; popping in at full strength while the card is still small reads as a hop
	val emphasis by animateFloatAsState(if (isFocused) 1f else 0f, focusSpec, label = "CardEmphasis")
	val colors = JellyfinTheme.colorScheme

	Box(
		modifier = modifier
			.zIndex(if (isFocused) 1f else 0f)
			.graphicsLayer {
				scaleX = scale
				scaleY = scale
			}
			.shadow(FocusedGlow * emphasis, CardShape, ambientColor = colors.accent, spotColor = colors.accent)
			.onFocusChanged { if (it.isFocused) onFocused() }
			.clip(CardShape)
			.background(colors.surface)
			.border(FocusedRing * emphasis, lerp(colors.surface, colors.accent, emphasis), CardShape)
			.clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
	) {
		content(isFocused)
	}
}

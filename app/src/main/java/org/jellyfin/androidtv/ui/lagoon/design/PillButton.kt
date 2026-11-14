package org.jellyfin.androidtv.ui.lagoon.design

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text

private val PillShape = RoundedCornerShape(50)

enum class PillEmphasis { Primary, Secondary }

/**
 * The billboard and detail page action button. Primary is solid white like a play button on a poster,
 * secondary is frosted glass. Both turn accent coloured under focus.
 */
@Composable
fun PillButton(
	label: String,
	@DrawableRes icon: Int,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	emphasis: PillEmphasis = PillEmphasis.Secondary,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val colors = JellyfinTheme.colorScheme

	val restingBackground = if (emphasis == PillEmphasis.Primary) Color.White else Color.White.copy(alpha = 0.16f)
	val restingContent = if (emphasis == PillEmphasis.Primary) colors.onAccent else Color.White
	val background by animateColorAsState(if (isFocused) colors.accent else restingBackground, label = "PillBackground")
	val content by animateColorAsState(if (isFocused) colors.onAccent else restingContent, label = "PillContent")
	val scale by animateFloatAsState(if (isFocused) LagoonDimens.FocusedButtonScale else 1f, label = "PillScale")

	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(10.dp),
		modifier = modifier
			.graphicsLayer {
				scaleX = scale
				scaleY = scale
			}
			.background(background, PillShape)
			.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
			.padding(horizontal = 22.dp, vertical = 10.dp),
	) {
		Icon(
			imageVector = ImageVector.vectorResource(icon),
			contentDescription = null,
			tint = content,
			modifier = Modifier.size(20.dp),
		)
		Text(text = label, color = content, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
	}
}

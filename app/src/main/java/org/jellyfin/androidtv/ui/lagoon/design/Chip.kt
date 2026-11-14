package org.jellyfin.androidtv.ui.lagoon.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text

private val ChipShape = RoundedCornerShape(50)

/**
 * A compact pill for tabs, sort and filter options. Selected chips are outlined in the accent colour,
 * the focused chip is filled with it.
 */
@Composable
fun Chip(
	label: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	isSelected: Boolean = false,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val colors = JellyfinTheme.colorScheme

	val background by animateColorAsState(
		when {
			isFocused -> colors.accent
			isSelected -> colors.accent.copy(alpha = 0.16f)
			else -> Color.White.copy(alpha = 0.08f)
		},
		label = "ChipBackground",
	)
	val content = when {
		isFocused -> colors.onAccent
		isSelected -> colors.accent
		else -> Color.White.copy(alpha = 0.85f)
	}

	Text(
		text = label,
		fontSize = 14.sp,
		fontWeight = if (isSelected || isFocused) FontWeight.SemiBold else FontWeight.Normal,
		color = content,
		modifier = modifier
			.background(background, ChipShape)
			.border(1.dp, if (isSelected && !isFocused) colors.accent.copy(alpha = 0.6f) else Color.Transparent, ChipShape)
			.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
			.padding(horizontal = 16.dp, vertical = 7.dp),
	)
}

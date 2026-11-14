package org.jellyfin.androidtv.ui.lagoon.design

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text

data class RailDestination(
	val key: String,
	val label: String,
	@DrawableRes val icon: Int,
	val onClick: () -> Unit,
)

private val RailItemShape = RoundedCornerShape(12.dp)
private val RailItemHeight = 44.dp

/**
 * The left navigation rail. Collapsed it is a column of icons; once focus enters it widens to show labels
 * over a dark wash, so the content behind stays in place.
 */
@Composable
fun NavRail(
	profileName: String,
	profileImageUrl: String?,
	onProfileClick: () -> Unit,
	destinations: List<RailDestination>,
	settings: RailDestination,
	selectedKey: String,
	contentRequester: FocusRequester,
	modifier: Modifier = Modifier,
) {
	var isExpanded by remember { mutableStateOf(false) }
	val width by animateDpAsState(
		if (isExpanded) LagoonDimens.RailExpandedWidth else LagoonDimens.RailCollapsedWidth,
		label = "RailWidth",
	)
	val selectedRequester = remember { FocusRequester() }
	val background = JellyfinTheme.colorScheme.background

	Column(
		modifier = modifier
			.fillMaxHeight()
			.width(width)
			.background(
				if (isExpanded) Brush.horizontalGradient(0f to background, 0.85f to background.copy(alpha = 0.97f), 1f to background.copy(alpha = 0f))
				else Brush.horizontalGradient(0f to background.copy(alpha = 0.7f), 1f to Color.Transparent)
			)
			.onFocusChanged { isExpanded = it.hasFocus }
			// Only a press of Left may enter the rail. Android picks a starting focus on its own when a screen is
			// attached; that pick is sent on to the screen's content instead of landing here.
			.focusProperties {
				onEnter = { if (requestedFocusDirection != FocusDirection.Left) contentRequester.requestFocus() }
			}
			.focusRestorer(selectedRequester)
			.focusGroup()
			.padding(horizontal = 14.dp, vertical = 28.dp),
	) {
		RailProfile(profileName, profileImageUrl, isExpanded, onProfileClick)

		Spacer(Modifier.weight(1f))

		Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
			destinations.forEach { destination ->
				RailItem(
					destination = destination,
					isSelected = destination.key == selectedKey,
					isExpanded = isExpanded,
					modifier = if (destination.key == selectedKey) Modifier.focusRequester(selectedRequester) else Modifier,
				)
			}
		}

		Spacer(Modifier.weight(1f))

		RailItem(destination = settings, isSelected = false, isExpanded = isExpanded)
	}
}

@Composable
private fun RailProfile(name: String, imageUrl: String?, isExpanded: Boolean, onClick: () -> Unit) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val accent = JellyfinTheme.colorScheme.accent

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.clip(RailItemShape)
			.background(if (isFocused) accent.copy(alpha = 0.22f) else Color.Transparent)
			.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
			.padding(4.dp),
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.size(40.dp)
				.clip(CircleShape)
				.background(JellyfinTheme.colorScheme.surface),
		) {
			if (imageUrl != null) {
				AsyncImage(model = imageUrl, contentDescription = name, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp))
			} else {
				Icon(ImageVector.vectorResource(R.drawable.ic_user), contentDescription = name, tint = Color.White, modifier = Modifier.size(22.dp))
			}
		}

		AnimatedVisibility(visible = isExpanded, enter = fadeIn(), exit = fadeOut()) {
			Column(Modifier.padding(start = 12.dp)) {
				Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
				Text(
					text = stringResource(R.string.lbl_switch_user),
					fontSize = 12.sp,
					color = JellyfinTheme.colorScheme.listCaption,
					maxLines = 1,
				)
			}
		}
	}
}

@Composable
private fun RailItem(
	destination: RailDestination,
	isSelected: Boolean,
	isExpanded: Boolean,
	modifier: Modifier = Modifier,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val colors = JellyfinTheme.colorScheme
	val tint = when {
		isFocused -> colors.onAccent
		isSelected -> colors.accent
		else -> Color.White.copy(alpha = 0.78f)
	}

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.fillMaxWidth()
			.height(RailItemHeight)
			.clip(RailItemShape)
			.background(if (isFocused) colors.accent else Color.Transparent)
			.clickable(interactionSource = interactionSource, indication = null, onClick = destination.onClick)
			.padding(horizontal = 12.dp),
	) {
		Icon(
			imageVector = ImageVector.vectorResource(destination.icon),
			contentDescription = destination.label,
			tint = tint,
			modifier = Modifier.size(24.dp),
		)

		AnimatedVisibility(visible = isExpanded, enter = fadeIn(), exit = fadeOut()) {
			Text(
				text = destination.label,
				fontSize = 16.sp,
				fontWeight = if (isSelected || isFocused) FontWeight.SemiBold else FontWeight.Normal,
				color = tint,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(start = 16.dp),
			)
		}
	}
}

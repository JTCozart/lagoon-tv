package org.jellyfin.androidtv.ui.lagoon.browse

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.compose.koinInject

private const val BackdropFadeMillis = 600
private const val InfoFadeMillis = 250
private const val OverviewMaxLines = 3
private const val CompactOverviewMaxLines = 2
private const val BackdropWidthFraction = 0.78f
private const val BackdropHeightFraction = 0.72f
private val LogoMaxHeight = 96.dp
private val CompactLogoMaxHeight = 64.dp
private val LogoMaxWidth = 380.dp

/**
 * Full-bleed artwork for the title in the spotlight, anchored top right and faded into the background
 * on the left and bottom so text and rows stay readable.
 */
@Composable
fun SpotlightBackdrop(item: BaseItemDto?, modifier: Modifier = Modifier) {
	val api = koinInject<ApiClient>()
	val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
	val url = remember(item?.id, screenWidthPx) {
		item?.let(CardArtwork::backdrop)?.getUrl(api, maxWidth = screenWidthPx)
	}
	val background = JellyfinTheme.colorScheme.background

	Box(modifier.fillMaxSize()) {
		Crossfade(
			targetState = url,
			animationSpec = tween(BackdropFadeMillis),
			label = "SpotlightBackdrop",
			modifier = Modifier
				.align(Alignment.TopEnd)
				.fillMaxWidth(BackdropWidthFraction)
				.fillMaxHeight(BackdropHeightFraction),
		) { backdropUrl ->
			Box(Modifier.fillMaxSize()) {
				if (backdropUrl != null) {
					AsyncImage(
						model = backdropUrl,
						contentDescription = null,
						contentScale = ContentScale.Crop,
						alignment = Alignment.TopCenter,
						modifier = Modifier.fillMaxSize(),
					)
				}
			}
		}

		Box(
			Modifier
				.align(Alignment.TopEnd)
				.fillMaxWidth(BackdropWidthFraction)
				.fillMaxHeight(BackdropHeightFraction)
				.background(Brush.horizontalGradient(0f to background, 0.45f to background.copy(alpha = 0.55f), 1f to Color.Transparent))
				.background(Brush.verticalGradient(0.45f to Color.Transparent, 1f to background))
		)
	}
}

/**
 * Logo or title, episode line, details and synopsis for the title in the spotlight.
 * [isCompact] trims the logo and synopsis when a page header shares the space.
 */
@Composable
fun SpotlightInfo(item: BaseItemDto?, modifier: Modifier = Modifier, isCompact: Boolean = false) {
	AnimatedContent(
		targetState = item,
		contentKey = { it?.id },
		transitionSpec = { fadeIn(tween(InfoFadeMillis)) togetherWith fadeOut(tween(InfoFadeMillis)) },
		contentAlignment = Alignment.BottomStart,
		label = "SpotlightInfo",
		modifier = modifier,
	) { shownItem ->
		if (shownItem != null) SpotlightText(shownItem, isCompact)
	}
}

@Composable
private fun SpotlightText(item: BaseItemDto, isCompact: Boolean) {
	val api = koinInject<ApiClient>()
	val logoUrl = remember(item.id) { CardArtwork.logo(item)?.getUrl(api, maxHeight = 300) }
	val title = remember(item.id) { HeroMetadata.titleOf(item) }
	val subtitle = remember(item.id) { HeroMetadata.subtitleOf(item) }
	val details = remember(item.id) { HeroMetadata.detailsOf(item) }

	Column(verticalArrangement = Arrangement.Bottom) {
		if (logoUrl != null) {
			AsyncImage(
				model = logoUrl,
				contentDescription = title,
				contentScale = ContentScale.Fit,
				alignment = Alignment.BottomStart,
				modifier = Modifier
					.heightIn(max = if (isCompact) CompactLogoMaxHeight else LogoMaxHeight)
					.widthIn(max = LogoMaxWidth),
			)
		} else {
			Text(
				text = title,
				fontSize = 40.sp,
				lineHeight = 44.sp,
				fontWeight = FontWeight.Bold,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
				color = Color.White,
				modifier = Modifier.widthIn(max = 520.dp),
			)
		}

		if (subtitle != null) {
			Spacer(Modifier.height(10.dp))
			Text(subtitle, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White)
		}

		if (details.isNotEmpty()) {
			Spacer(Modifier.height(8.dp))
			DetailsLine(details)
		}

		val overview = item.overview
		if (!overview.isNullOrBlank()) {
			Spacer(Modifier.height(10.dp))
			Text(
				text = overview,
				fontSize = 14.sp,
				lineHeight = 20.sp,
				maxLines = if (isCompact) CompactOverviewMaxLines else OverviewMaxLines,
				overflow = TextOverflow.Ellipsis,
				color = Color.White.copy(alpha = 0.82f),
				modifier = Modifier.widthIn(max = 480.dp),
			)
		}
	}
}

@Composable
private fun DetailsLine(details: List<String>) {
	val accent = JellyfinTheme.colorScheme.accent
	Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
		details.forEachIndexed { index, detail ->
			if (index > 0) Box(Modifier.size(4.dp).background(accent, CircleShape))
			Text(
				text = detail,
				fontSize = 14.sp,
				fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
				color = if (index == 0) accent else Color.White,
			)
		}
	}
}

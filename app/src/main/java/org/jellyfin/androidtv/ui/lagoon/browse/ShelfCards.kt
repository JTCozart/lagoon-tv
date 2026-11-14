package org.jellyfin.androidtv.ui.lagoon.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.lagoon.design.FocusCard
import org.jellyfin.androidtv.ui.lagoon.design.LagoonDimens
import org.jellyfin.androidtv.util.apiclient.JellyfinImage
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.compose.koinInject

private const val LandscapeAspect = 16f / 9f
private const val PosterAspect = 2f / 3f
private const val PercentScale = 100f

@Composable
fun LandscapeCard(
	item: BaseItemDto,
	onClick: () -> Unit,
	onFocused: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val title = remember(item.id) { HeroMetadata.titleOf(item) }
	val subtitle = remember(item.id) { HeroMetadata.subtitleOf(item) }

	Column(Modifier.width(LagoonDimens.LandscapeCardWidth)) {
		FocusCard(
			onClick = onClick,
			onFocused = onFocused,
			modifier = modifier
				.fillMaxWidth()
				.aspectRatio(LandscapeAspect),
		) {
			CardImage(CardArtwork.landscape(item), title, LagoonDimens.LandscapeCardWidth)
			ProgressStripe(item.userData?.playedPercentage, Modifier.align(Alignment.BottomStart))
		}

		Text(
			text = title,
			fontSize = 14.sp,
			fontWeight = FontWeight.SemiBold,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			color = Color.White,
			modifier = Modifier.padding(top = 10.dp, start = 2.dp),
		)
		if (subtitle != null) {
			Text(
				text = subtitle,
				fontSize = 12.sp,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				color = JellyfinTheme.colorScheme.listCaption,
				modifier = Modifier.padding(start = 2.dp),
			)
		}
	}
}

@Composable
fun PosterCard(
	item: BaseItemDto,
	onClick: () -> Unit,
	onFocused: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val title = remember(item.id) { HeroMetadata.titleOf(item) }

	FocusCard(
		onClick = onClick,
		onFocused = onFocused,
		modifier = modifier
			.width(LagoonDimens.PosterCardWidth)
			.aspectRatio(PosterAspect),
	) {
		CardImage(CardArtwork.poster(item), title, LagoonDimens.PosterCardWidth)
		UnwatchedBadge(item.userData?.unplayedItemCount, Modifier.align(Alignment.TopEnd))
		ProgressStripe(item.userData?.playedPercentage, Modifier.align(Alignment.BottomStart))
	}
}

@Composable
fun CardImage(image: JellyfinImage?, title: String, width: Dp) {
	if (image == null) {
		TitlePlaceholder(title)
		return
	}

	val api = koinInject<ApiClient>()
	val widthPx = with(LocalDensity.current) { (width * LagoonDimens.FocusedCardScale).roundToPx() }
	val url = remember(image, widthPx) { image.getUrl(api, maxWidth = widthPx) }

	AsyncImage(
		model = url,
		contentDescription = title,
		contentScale = ContentScale.Crop,
		modifier = Modifier.fillMaxSize(),
	)
}

@Composable
private fun TitlePlaceholder(title: String) {
	val colors = JellyfinTheme.colorScheme
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(Brush.linearGradient(listOf(colors.surface, colors.accentSecondary.copy(alpha = 0.35f)))),
	) {
		Text(
			text = title,
			fontSize = 14.sp,
			fontWeight = FontWeight.SemiBold,
			color = Color.White,
			maxLines = 3,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(10.dp),
		)
	}
}

@Composable
fun ProgressStripe(playedPercentage: Double?, modifier: Modifier = Modifier) {
	val fraction = (playedPercentage ?: return).toFloat() / PercentScale
	if (fraction <= 0f) return

	Box(
		modifier
			.fillMaxWidth()
			.height(4.dp)
			.background(Color.Black.copy(alpha = 0.55f))
	) {
		Box(
			Modifier
				.fillMaxHeight()
				.fillMaxWidth(fraction.coerceIn(0f, 1f))
				.background(JellyfinTheme.colorScheme.accent)
		)
	}
}

@Composable
private fun UnwatchedBadge(count: Int?, modifier: Modifier = Modifier) {
	if (count == null || count <= 0) return

	Text(
		text = count.toString(),
		fontSize = 11.sp,
		fontWeight = FontWeight.Bold,
		color = JellyfinTheme.colorScheme.onAccent,
		modifier = modifier
			.padding(6.dp)
			.background(JellyfinTheme.colorScheme.accent, RoundedCornerShape(50))
			.padding(horizontal = 7.dp, vertical = 2.dp),
	)
}

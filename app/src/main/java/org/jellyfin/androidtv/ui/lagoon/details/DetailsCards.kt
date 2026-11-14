package org.jellyfin.androidtv.ui.lagoon.details

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.lagoon.browse.CardArtwork
import org.jellyfin.androidtv.ui.lagoon.browse.CardImage
import org.jellyfin.androidtv.ui.lagoon.browse.ProgressStripe
import org.jellyfin.androidtv.ui.lagoon.design.FocusCard
import org.jellyfin.androidtv.ui.lagoon.design.LagoonDimens
import org.jellyfin.androidtv.util.apiclient.JellyfinImage
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.primaryImage
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemPerson
import org.koin.compose.koinInject

private val EpisodeCardWidth = 300.dp
private val PersonPhotoSize = 92.dp
private const val LandscapeAspect = 16f / 9f
private const val TicksPerMinute = 600_000_000L

@Composable
fun EpisodeCard(
	episode: BaseItemDto,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val name = episode.name.orEmpty()
	val heading = episode.indexNumber?.let { "$it. $name" } ?: name
	val runtime = episode.runTimeTicks?.let { RuntimeText.of(it / TicksPerMinute) }

	Column(Modifier.width(EpisodeCardWidth)) {
		FocusCard(
			onClick = onClick,
			modifier = modifier
				.fillMaxWidth()
				.aspectRatio(LandscapeAspect),
		) {
			CardImage(CardArtwork.landscape(episode), name, EpisodeCardWidth)
			if (episode.userData?.played == true) WatchedMark(Modifier.align(Alignment.TopEnd))
			ProgressStripe(episode.userData?.playedPercentage, Modifier.align(Alignment.BottomStart))
		}

		Text(
			text = heading,
			fontSize = 15.sp,
			fontWeight = FontWeight.SemiBold,
			color = Color.White,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(top = 10.dp),
		)
		if (runtime != null) {
			Text(runtime, fontSize = 12.sp, color = JellyfinTheme.colorScheme.accent, modifier = Modifier.padding(top = 2.dp))
		}
		episode.overview?.takeIf { it.isNotBlank() }?.let { overview ->
			Text(
				text = overview,
				fontSize = 12.sp,
				lineHeight = 16.sp,
				color = JellyfinTheme.colorScheme.listCaption,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(top = 4.dp),
			)
		}
	}
}

@Composable
private fun WatchedMark(modifier: Modifier = Modifier) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.padding(8.dp)
			.size(24.dp)
			.background(JellyfinTheme.colorScheme.accent, CircleShape),
	) {
		Icon(
			imageVector = ImageVector.vectorResource(R.drawable.ic_check),
			contentDescription = null,
			tint = JellyfinTheme.colorScheme.onAccent,
			modifier = Modifier.size(16.dp),
		)
	}
}

@Composable
fun PersonCard(
	person: BaseItemPerson,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) = PersonCard(
	name = person.name.orEmpty(),
	caption = person.role?.takeIf { it.isNotBlank() } ?: person.type.serialName,
	image = person.primaryImage,
	onClick = onClick,
	modifier = modifier,
)

@Composable
fun PersonCard(
	name: String,
	caption: String?,
	image: JellyfinImage?,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val scale by animateFloatAsState(if (isFocused) LagoonDimens.FocusedCardScale else 1f, label = "PersonScale")
	val api = koinInject<ApiClient>()
	val photoPx = with(LocalDensity.current) { PersonPhotoSize.roundToPx() * 2 }
	val photoUrl = remember(image) { image?.getUrl(api, maxWidth = photoPx) }
	val colors = JellyfinTheme.colorScheme

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
			.width(PersonPhotoSize + 24.dp)
			.clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
				}
				.size(PersonPhotoSize)
				.clip(CircleShape)
				.background(colors.surface)
				.border(if (isFocused) 3.dp else 0.dp, if (isFocused) colors.accent else Color.Transparent, CircleShape),
		) {
			if (photoUrl != null) {
				AsyncImage(model = photoUrl, contentDescription = name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
			} else {
				Icon(ImageVector.vectorResource(R.drawable.ic_user), contentDescription = name, tint = Color.White, modifier = Modifier.size(36.dp))
			}
		}

		Text(
			text = name,
			fontSize = 13.sp,
			fontWeight = FontWeight.SemiBold,
			color = if (isFocused) colors.accent else Color.White,
			textAlign = TextAlign.Center,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(top = 10.dp),
		)
		if (caption != null) Text(
			text = caption,
			fontSize = 11.sp,
			color = colors.listCaption,
			textAlign = TextAlign.Center,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)
	}
}

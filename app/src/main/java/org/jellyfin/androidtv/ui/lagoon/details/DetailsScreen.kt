package org.jellyfin.androidtv.ui.lagoon.details

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.lagoon.browse.CardArtwork
import org.jellyfin.androidtv.ui.lagoon.browse.HeroMetadata
import org.jellyfin.androidtv.ui.lagoon.browse.LagoonActions
import org.jellyfin.androidtv.ui.lagoon.browse.PosterCard
import org.jellyfin.androidtv.ui.lagoon.design.Chip
import org.jellyfin.androidtv.ui.lagoon.design.LagoonDimens
import org.jellyfin.androidtv.ui.lagoon.design.PillButton
import org.jellyfin.androidtv.ui.lagoon.design.PillEmphasis
import org.jellyfin.androidtv.ui.lagoon.design.PinFocusedChild
import org.jellyfin.androidtv.ui.lagoon.design.RequestInitialFocus
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.PersonKind
import org.koin.compose.koinInject

private val PageStart = 64.dp
private val HeroHeight = 430.dp
private val SectionTitleAllowance = 48.dp
private const val OverviewMaxLines = 4
private const val StarringCount = 3

@Composable
fun DetailsScreen(viewModel: DetailsViewModel, actions: LagoonActions) {
	val state by viewModel.state.collectAsState()
	val colors = JellyfinTheme.colorScheme
	val title = state.title

	Box(
		Modifier
			.fillMaxSize()
			.background(colors.background)
	) {
		DetailsBackdrop(title)

		when {
			state.hasFailed -> Text(
				text = stringResource(R.string.lagoon_details_failed),
				fontSize = 18.sp,
				color = Color.White,
				modifier = Modifier
					.align(Alignment.CenterStart)
					.padding(start = PageStart),
			)

			title != null -> DetailsContent(state, title, viewModel, actions)
		}
	}
}

@Composable
private fun DetailsContent(state: DetailsUiState, title: BaseItemDto, viewModel: DetailsViewModel, actions: LagoonActions) {
	val playRequester = remember { FocusRequester() }
	val listState = rememberLazyListState()
	val scope = rememberCoroutineScope()
	val isFocusInsideContent = remember { mutableStateOf(false) }
	RequestInitialFocus(key = title.id, requester = { playRequester }, isFocusInsideContent = isFocusInsideContent)

	// Only scroll for off-screen targets, so the hero is never pushed up while its buttons have focus
	PinFocusedChild(leadingEdge = SectionTitleAllowance, pinVisibleChildren = false) {
		LazyColumn(
			state = listState,
			contentPadding = PaddingValues(bottom = 160.dp),
			modifier = Modifier
				.fillMaxSize()
				.onFocusChanged { isFocusInsideContent.value = it.hasFocus },
		) {
			val hasEpisodes = state.seasons.isNotEmpty()
			val hasCast = state.cast.isNotEmpty()
			val episodesIndex = 1
			val castIndex = if (hasEpisodes) 2 else 1
			val similarIndex = castIndex + if (hasCast) 1 else 0

			item(key = "hero") {
				DetailsHero(state, title, viewModel, actions, playRequester) {
					scope.launch { listState.animateScrollToItem(0) }
				}
			}

			if (hasEpisodes) {
				item(key = "episodes") {
					Box(Modifier.scrollToTopOnFocus(listState, scope, episodesIndex)) { SeasonsAndEpisodes(state, viewModel, actions) }
				}
			}

			if (hasCast) {
				item(key = "cast") {
					Section(stringResource(R.string.lagoon_cast_and_crew), Modifier.scrollToTopOnFocus(listState, scope, castIndex)) {
						LazyRow(
							contentPadding = PaddingValues(horizontal = PageStart, vertical = 10.dp),
							horizontalArrangement = Arrangement.spacedBy(18.dp),
							modifier = Modifier.focusRestorer().focusGroup(),
						) {
							items(state.cast, key = { "${it.id}-${it.type}-${it.role}" }) { person ->
								PersonCard(person = person, onClick = { actions.openPerson(person.id) })
							}
						}
					}
				}
			}

			if (state.similar.isNotEmpty()) {
				item(key = "similar") {
					Section(stringResource(R.string.lagoon_more_like_this), Modifier.scrollToTopOnFocus(listState, scope, similarIndex)) {
						PinFocusedChild(leadingEdge = PageStart) {
							LazyRow(
								contentPadding = PaddingValues(horizontal = PageStart, vertical = 10.dp),
								horizontalArrangement = Arrangement.spacedBy(LagoonDimens.CardGap),
								modifier = Modifier.focusRestorer().focusGroup(),
							) {
								items(state.similar, key = { it.id }) { item ->
									PosterCard(item = item, onClick = { actions.openDetails(item) }, onFocused = {})
								}
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun DetailsHero(
	state: DetailsUiState,
	title: BaseItemDto,
	viewModel: DetailsViewModel,
	actions: LagoonActions,
	playRequester: FocusRequester,
	onButtonsFocused: () -> Unit,
) {
	val api = koinInject<ApiClient>()
	val logoUrl = remember(title.id) { CardArtwork.logo(title)?.getUrl(api, maxHeight = 320) }
	val details = remember(title.id) { HeroMetadata.detailsOf(title) }
	val starring = remember(state.cast) {
		state.cast.filter { it.type == PersonKind.ACTOR }.take(StarringCount).mapNotNull { it.name }.joinToString(", ")
	}

	Column(
		verticalArrangement = Arrangement.Bottom,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(min = HeroHeight)
			.padding(start = PageStart, end = PageStart, top = 48.dp, bottom = 24.dp),
	) {
		if (logoUrl != null) {
			AsyncImage(
				model = logoUrl,
				contentDescription = title.name,
				contentScale = ContentScale.Fit,
				alignment = Alignment.BottomStart,
				modifier = Modifier
					.heightIn(max = 110.dp)
					.widthIn(max = 440.dp),
			)
		} else {
			Text(
				text = title.name.orEmpty(),
				fontSize = 44.sp,
				lineHeight = 48.sp,
				fontWeight = FontWeight.Bold,
				color = Color.White,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.widthIn(max = 600.dp),
			)
		}

		Spacer(Modifier.height(14.dp))
		Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			Text(
				text = details.joinToString("  •  "),
				fontSize = 15.sp,
				color = Color.White,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(end = 6.dp),
			)
			state.badges.forEach { badge -> Badge(badge) }
		}

		title.taglines?.firstOrNull()?.takeIf { it.isNotBlank() }?.let { tagline ->
			Spacer(Modifier.height(10.dp))
			Text(tagline, fontSize = 15.sp, fontStyle = FontStyle.Italic, color = colors().accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
		}

		title.overview?.takeIf { it.isNotBlank() }?.let { overview ->
			Spacer(Modifier.height(10.dp))
			Text(
				text = overview,
				fontSize = 15.sp,
				lineHeight = 22.sp,
				color = Color.White.copy(alpha = 0.85f),
				maxLines = OverviewMaxLines,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.widthIn(max = 560.dp),
			)
		}

		if (starring.isNotEmpty()) {
			Spacer(Modifier.height(8.dp))
			Text(
				text = stringResource(R.string.lagoon_starring, starring),
				fontSize = 13.sp,
				color = colors().listCaption,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.widthIn(max = 560.dp),
			)
		}

		Spacer(Modifier.height(22.dp))
		DetailsButtons(state, viewModel, actions, playRequester, onButtonsFocused)
	}
}

@Composable
private fun DetailsButtons(
	state: DetailsUiState,
	viewModel: DetailsViewModel,
	actions: LagoonActions,
	playRequester: FocusRequester,
	onFocused: () -> Unit,
) {
	val plan = state.playPlan

	Row(
		horizontalArrangement = Arrangement.spacedBy(14.dp),
		modifier = Modifier
			.onFocusChanged { if (it.hasFocus) onFocused() }
			.focusRestorer(playRequester)
			.focusGroup(),
	) {
		if (plan != null) {
			PillButton(
				label = playLabel(plan),
				icon = R.drawable.ic_play,
				emphasis = PillEmphasis.Primary,
				onClick = { actions.play(plan.target) },
				modifier = Modifier.focusRequester(playRequester),
			)
			if (plan.isResume) {
				PillButton(
					label = stringResource(R.string.lagoon_start_over),
					icon = R.drawable.ic_rewind,
					onClick = { actions.playFromStart(plan.target) },
				)
			}
		}
		PillButton(
			label = stringResource(if (state.isPlayed) R.string.lagoon_watched else R.string.lagoon_mark_watched),
			icon = if (state.isPlayed) R.drawable.ic_check else R.drawable.ic_watch,
			onClick = viewModel::togglePlayed,
			modifier = if (plan == null) Modifier.focusRequester(playRequester) else Modifier,
		)
		PillButton(
			label = stringResource(if (state.isFavorite) R.string.lagoon_favorited else R.string.lagoon_favorite),
			icon = R.drawable.ic_heart,
			onClick = viewModel::toggleFavorite,
		)
	}
}

@Composable
private fun playLabel(plan: PlayPlan): String {
	val base = when {
		plan.episodeLabel != null && plan.isResume -> stringResource(R.string.lagoon_resume_episode, plan.episodeLabel)
		plan.episodeLabel != null -> stringResource(R.string.lagoon_play_episode, plan.episodeLabel)
		plan.isResume -> stringResource(R.string.lagoon_resume)
		else -> stringResource(R.string.lbl_play)
	}
	val remaining = plan.remainingMinutes?.takeIf { it > 0 }?.let { stringResource(R.string.lagoon_time_left, RuntimeText.of(it)) }
	return listOfNotNull(base, remaining).joinToString("  ·  ")
}

@Composable
private fun SeasonsAndEpisodes(state: DetailsUiState, viewModel: DetailsViewModel, actions: LagoonActions) {
	val highlightedIndex = state.episodes.indexOfFirst { it.id == state.highlightedEpisodeId }.coerceAtLeast(0)
	val episodesState = rememberLazyListState(initialFirstVisibleItemIndex = highlightedIndex)
	LaunchedEffect(state.selectedSeasonId, state.episodes.size) {
		if (state.episodes.isNotEmpty()) episodesState.scrollToItem(highlightedIndex)
	}

	Section(stringResource(R.string.lagoon_episodes)) {
		if (state.seasons.size > 1) {
			LazyRow(
				contentPadding = PaddingValues(horizontal = PageStart),
				horizontalArrangement = Arrangement.spacedBy(10.dp),
				modifier = Modifier
					.padding(bottom = 6.dp)
					.focusRestorer()
					.focusGroup(),
			) {
				items(state.seasons, key = { it.id }) { season ->
					Chip(
						label = season.name.orEmpty(),
						isSelected = season.id == state.selectedSeasonId,
						onClick = { viewModel.selectSeason(season.id) },
					)
				}
			}
		}

		PinFocusedChild(leadingEdge = PageStart) {
			LazyRow(
				state = episodesState,
				contentPadding = PaddingValues(horizontal = PageStart, vertical = 12.dp),
				horizontalArrangement = Arrangement.spacedBy(18.dp),
				modifier = Modifier
					.focusRestorer()
					.focusGroup(),
			) {
				itemsIndexed(state.episodes, key = { _, episode -> episode.id }) { _, episode ->
					EpisodeCard(episode = episode, onClick = { actions.play(episode) })
				}
			}
		}
	}
}

/**
 * Brings a whole section, heading and all, to the top of the page as soon as focus enters it.
 * Scrolling only far enough to show the focused card would leave the episode titles below it cut off.
 */
private fun Modifier.scrollToTopOnFocus(listState: LazyListState, scope: CoroutineScope, itemIndex: Int): Modifier =
	onFocusChanged { focusState ->
		if (focusState.hasFocus) scope.launch { listState.animateScrollToItem(itemIndex) }
	}

@Composable
private fun Section(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
	Column(modifier.padding(top = 8.dp, bottom = 20.dp)) {
		Text(
			text = title,
			fontSize = 19.sp,
			fontWeight = FontWeight.SemiBold,
			color = Color.White,
			modifier = Modifier.padding(start = PageStart, bottom = LagoonDimens.ShelfTitleGap),
		)
		content()
	}
}

@Composable
private fun Badge(label: String) {
	Text(
		text = label,
		fontSize = 11.sp,
		fontWeight = FontWeight.Bold,
		color = Color.White,
		modifier = Modifier
			.border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
			.padding(horizontal = 6.dp, vertical = 2.dp),
	)
}

@Composable
private fun DetailsBackdrop(title: BaseItemDto?) {
	val api = koinInject<ApiClient>()
	val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
	val url = remember(title?.id, screenWidthPx) { title?.let(CardArtwork::backdrop)?.getUrl(api, maxWidth = screenWidthPx) }
	val background = colors().background

	Crossfade(targetState = url, animationSpec = tween(500), label = "DetailsBackdrop", modifier = Modifier.fillMaxSize()) { backdropUrl ->
		if (backdropUrl != null) {
			AsyncImage(
				model = backdropUrl,
				contentDescription = null,
				contentScale = ContentScale.Crop,
				alignment = Alignment.TopEnd,
				modifier = Modifier.fillMaxSize(),
			)
		}
	}
	Box(
		Modifier
			.fillMaxSize()
			.background(background.copy(alpha = 0.3f))
			.background(Brush.horizontalGradient(0f to background, 0.5f to background.copy(alpha = 0.7f), 1f to Color.Transparent))
			.background(Brush.verticalGradient(0.35f to Color.Transparent, 0.85f to background))
	)
}

@Composable
private fun colors() = JellyfinTheme.colorScheme

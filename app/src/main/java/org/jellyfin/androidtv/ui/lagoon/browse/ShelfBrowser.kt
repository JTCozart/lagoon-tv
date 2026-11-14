package org.jellyfin.androidtv.ui.lagoon.browse

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.lagoon.design.LagoonDimens
import org.jellyfin.androidtv.ui.lagoon.design.PillButton
import org.jellyfin.androidtv.ui.lagoon.design.PillEmphasis
import org.jellyfin.androidtv.ui.lagoon.design.PinFocusedChild
import org.jellyfin.androidtv.ui.lagoon.design.RequestInitialFocus
import org.jellyfin.sdk.model.api.BaseItemDto
import kotlin.time.Duration.Companion.seconds

private val BillboardRotation = 10.seconds
private val ShelfTitleAllowance = 48.dp
private val CardRemoteKeys = setOf(KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MENU)

/**
 * The streaming-app browse layout: a spotlight for the featured or focused title on top, with Play and
 * More info while the billboard has focus, and horizontally scrolling shelves below.
 * [header] sits above the spotlight text, for a page title or tabs.
 */
@Composable
fun ShelfBrowser(
	featured: List<BaseItemDto>,
	shelves: List<Shelf>,
	isLoading: Boolean,
	focusMemory: FocusMemory,
	actions: LagoonActions,
	modifier: Modifier = Modifier,
	spotlightHeight: Dp = LagoonDimens.SpotlightHeight,
	header: (@Composable () -> Unit)? = null,
) {
	var isBillboardFocused by remember { mutableStateOf(false) }
	var focusedCardItem by remember { mutableStateOf<BaseItemDto?>(null) }
	var featuredIndex by remember { mutableIntStateOf(0) }
	val featuredItem = featured.getOrNull(featuredIndex % featured.size.coerceAtLeast(1))
	val spotlightItem = if (isBillboardFocused || focusedCardItem == null) featuredItem ?: focusedCardItem else focusedCardItem

	val restoreFocus = remember { focusMemory.lastFocus }
	val shelvesState = rememberLazyListState(initialFirstVisibleItemIndex = restoreFocus?.shelfIndex ?: 0)
	val playRequester = remember { FocusRequester() }
	val firstCardRequester = remember { FocusRequester() }
	val restoreRequester = remember { FocusRequester() }
	val isFocusInsideContent = remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()

	LaunchedEffect(isBillboardFocused, featured.size) {
		while (isBillboardFocused && featured.size > 1) {
			delay(BillboardRotation)
			featuredIndex = (featuredIndex + 1) % featured.size
		}
	}

	val hasFeatured = featuredItem != null
	RequestInitialFocus(
		key = isLoading,
		requester = {
			when {
				restoreFocus != null -> restoreRequester
				hasFeatured -> playRequester
				else -> firstCardRequester
			}
		},
		isFocusInsideContent = isFocusInsideContent,
		isEnabled = !isLoading,
	)

	BackHandler(enabled = !isBillboardFocused && hasFeatured && focusedCardItem != null) {
		scope.launch { shelvesState.scrollToItem(0) }
		playRequester.requestFocus()
	}

	Box(
		modifier
			.fillMaxSize()
			.onFocusChanged { isFocusInsideContent.value = it.hasFocus }
	) {
		SpotlightBackdrop(spotlightItem)

		Column(Modifier.fillMaxSize()) {
			Box(
				Modifier
					.fillMaxWidth()
					.height(spotlightHeight)
					.padding(start = LagoonDimens.ContentStart, top = 28.dp, bottom = 12.dp)
			) {
				if (header != null) Box(Modifier.align(Alignment.TopStart)) { header() }

				Column(Modifier.align(Alignment.BottomStart)) {
					SpotlightInfo(spotlightItem, isCompact = header != null)

					if (featuredItem != null) {
						BillboardControls(
							featuredItem = featuredItem,
							featuredCount = featured.size,
							featuredIndex = featuredIndex % featured.size,
							isVisible = isBillboardFocused,
							playRequester = playRequester,
							onFocusChanged = { hasFocus ->
								isBillboardFocused = hasFocus
								if (hasFocus) focusMemory.lastFocus = null
							},
							actions = actions,
						)
					}
				}
			}

			// Pin the focused card a title's height below the top so its shelf title stays visible
			PinFocusedChild(leadingEdge = ShelfTitleAllowance) {
				LazyColumn(
					state = shelvesState,
					contentPadding = PaddingValues(bottom = 200.dp),
					modifier = Modifier.fillMaxSize(),
				) {
					itemsIndexed(shelves, key = { _, shelf -> shelf.key }) { shelfIndex, shelf ->
						val restoreIndex = restoreFocus?.takeIf { it.shelfIndex == shelfIndex }?.itemIndex
						ShelfRow(
							shelf = shelf,
							restoreItemIndex = restoreIndex,
							restoreRequester = restoreRequester,
							firstCardRequester = if (shelfIndex == 0) firstCardRequester else null,
							onItemFocused = { itemIndex, item ->
								focusedCardItem = item
								focusMemory.lastFocus = BrowseFocus(shelfIndex, itemIndex)
							},
							actions = actions,
						)
					}
				}
			}
		}
	}
}

@Composable
private fun BillboardControls(
	featuredItem: BaseItemDto,
	featuredCount: Int,
	featuredIndex: Int,
	isVisible: Boolean,
	playRequester: FocusRequester,
	onFocusChanged: (Boolean) -> Unit,
	actions: LagoonActions,
) {
	// Kept composed while hidden so Up from the first shelf still has somewhere to land
	val alpha by animateFloatAsState(if (isVisible) 1f else 0f, label = "BillboardControls")
	val isResumable = (featuredItem.userData?.playbackPositionTicks ?: 0L) > 0L

	Column(
		Modifier
			.padding(top = 16.dp)
			.graphicsLayer { this.alpha = alpha }
	) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(14.dp),
			modifier = Modifier
				.onFocusChanged { onFocusChanged(it.hasFocus) }
				.focusRestorer(playRequester)
				.focusGroup(),
		) {
			PillButton(
				label = stringResource(if (isResumable) R.string.lagoon_resume else R.string.lbl_play),
				icon = R.drawable.ic_play,
				emphasis = PillEmphasis.Primary,
				onClick = { actions.play(featuredItem) },
				modifier = Modifier.focusRequester(playRequester),
			)
			PillButton(
				label = stringResource(R.string.lagoon_more_info),
				icon = R.drawable.ic_info,
				onClick = { actions.openDetails(featuredItem) },
			)
		}

		if (featuredCount > 1) {
			Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 14.dp, start = 4.dp)) {
				repeat(featuredCount) { index ->
					Box(
						Modifier
							.size(width = if (index == featuredIndex) 18.dp else 6.dp, height = 6.dp)
							.background(
								if (index == featuredIndex) JellyfinTheme.colorScheme.accent else Color.White.copy(alpha = 0.35f),
								CircleShape,
							)
					)
				}
			}
		}
	}
}

@Composable
private fun ShelfRow(
	shelf: Shelf,
	restoreItemIndex: Int?,
	restoreRequester: FocusRequester,
	firstCardRequester: FocusRequester?,
	onItemFocused: (Int, BaseItemDto) -> Unit,
	actions: LagoonActions,
) {
	val rowState = rememberLazyListState(initialFirstVisibleItemIndex = restoreItemIndex ?: 0)

	Column(Modifier.padding(bottom = 18.dp)) {
		Text(
			text = shelf.title,
			fontSize = 19.sp,
			fontWeight = FontWeight.SemiBold,
			color = Color.White,
			modifier = Modifier.padding(start = LagoonDimens.ContentStart, bottom = LagoonDimens.ShelfTitleGap),
		)

		PinFocusedChild(leadingEdge = LagoonDimens.ContentStart) {
			LazyRow(
				state = rowState,
				contentPadding = PaddingValues(horizontal = LagoonDimens.ContentStart, vertical = 10.dp),
				horizontalArrangement = Arrangement.spacedBy(LagoonDimens.CardGap),
				modifier = Modifier
					.fillMaxWidth()
					.focusRestorer()
					.focusGroup(),
			) {
				itemsIndexed(shelf.items, key = { _, item -> item.id }) { itemIndex, item ->
					val requesterModifier = when {
						itemIndex == restoreItemIndex -> Modifier.focusRequester(restoreRequester)
						itemIndex == 0 && firstCardRequester != null -> Modifier.focusRequester(firstCardRequester)
						else -> Modifier
					}
					val cardModifier = requesterModifier.onKeyEvent { event ->
						val keyCode = event.key.nativeKeyCode
						event.type == KeyEventType.KeyUp && keyCode in CardRemoteKeys && actions.handleRemoteKey(item, keyCode)
					}

					when (shelf.cardStyle) {
						ShelfCardStyle.Landscape -> LandscapeCard(
							item = item,
							onClick = { actions.openDetails(item) },
							onFocused = { onItemFocused(itemIndex, item) },
							modifier = cardModifier,
						)

						ShelfCardStyle.Poster -> PosterCard(
							item = item,
							onClick = { actions.openDetails(item) },
							onFocused = { onItemFocused(itemIndex, item) },
							modifier = cardModifier,
						)
					}
				}
			}
		}
	}
}

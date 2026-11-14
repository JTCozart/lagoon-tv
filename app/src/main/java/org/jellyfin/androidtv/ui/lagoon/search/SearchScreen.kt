package org.jellyfin.androidtv.ui.lagoon.search

import android.view.KeyEvent
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.lagoon.browse.LagoonActions
import org.jellyfin.androidtv.ui.lagoon.browse.LagoonChrome
import org.jellyfin.androidtv.ui.lagoon.browse.LandscapeCard
import org.jellyfin.androidtv.ui.lagoon.browse.PosterCard
import org.jellyfin.androidtv.ui.lagoon.browse.SearchRailKey
import org.jellyfin.androidtv.ui.lagoon.design.LagoonDimens
import org.jellyfin.androidtv.ui.lagoon.design.PinFocusedChild
import org.jellyfin.androidtv.ui.lagoon.design.RequestInitialFocus
import org.jellyfin.sdk.model.api.BaseItemDto
import kotlin.time.Duration.Companion.milliseconds

private const val GridColumns = 4
private val ResultsStart = 32.dp
private val SectionTitleAllowance = 48.dp

@Composable
fun SearchScreen(
	viewModel: LagoonSearchViewModel,
	actions: LagoonActions,
	profileName: String,
	profileImageUrl: String?,
	startInResults: Boolean,
) {
	val state by viewModel.state.collectAsState()
	val firstKeyRequester = remember { FocusRequester() }
	val firstResultRequester = remember { FocusRequester() }
	val isFocusInsideContent = remember { mutableStateOf(false) }

	RequestInitialFocus(key = Unit, requester = { firstKeyRequester }, isFocusInsideContent = isFocusInsideContent)

	// A search started from the system (voice remote, Assistant) has its query already, so focus moves to the
	// results once, when they first arrive. Later result changes leave focus where the user put it.
	var hasMovedToResults by remember { mutableStateOf(!startInResults) }
	LaunchedEffect(state.hasResults) {
		if (hasMovedToResults || !state.hasResults) return@LaunchedEffect
		hasMovedToResults = true
		withFrameNanos { }
		runCatching { firstResultRequester.requestFocus() }
	}

	LagoonChrome(
		selectedKey = SearchRailKey,
		libraries = state.libraries,
		profileName = profileName,
		profileImageUrl = profileImageUrl,
		actions = actions,
	) {
		Row(
			Modifier
				.fillMaxSize()
				.padding(start = LagoonDimens.ContentStart, top = 36.dp)
				.onFocusChanged { isFocusInsideContent.value = it.hasFocus }
				.onKeyEvent { event -> event.type == KeyEventType.KeyDown && typeFromKeyboard(event, viewModel) }
		) {
			SearchKeyboard(
				query = state.query,
				onType = viewModel::type,
				onDelete = viewModel::deleteLast,
				onClear = viewModel::clear,
				onVoiceQuery = { query, isFinal -> viewModel.setQuery(query, if (isFinal) 0.milliseconds else 350.milliseconds) },
				firstKeyRequester = firstKeyRequester,
			)

			SearchResults(
				state = state,
				actions = actions,
				firstResultRequester = firstResultRequester,
				modifier = Modifier
					.weight(1f)
					.padding(start = ResultsStart),
			)
		}
	}
}

/** Lets a hardware keyboard or a remote with letter keys type straight into the query. */
private fun typeFromKeyboard(event: androidx.compose.ui.input.key.KeyEvent, viewModel: LagoonSearchViewModel): Boolean {
	if (event.key.nativeKeyCode == KeyEvent.KEYCODE_DEL) {
		viewModel.deleteLast()
		return true
	}
	val character = event.utf16CodePoint.toChar()
	if (!character.isLetterOrDigit() && character != ' ') return false
	viewModel.type(character.lowercaseChar())
	return true
}

@Composable
private fun SearchResults(
	state: SearchUiState,
	actions: LagoonActions,
	firstResultRequester: FocusRequester,
	modifier: Modifier = Modifier,
) {
	PinFocusedChild(leadingEdge = SectionTitleAllowance, pinVisibleChildren = false) {
		LazyColumn(
			contentPadding = PaddingValues(bottom = 120.dp, end = LagoonDimens.ContentStart),
			modifier = modifier.fillMaxSize(),
		) {
			when {
				state.query.isBlank() -> posterGrid(
					title = { stringResource(R.string.lagoon_shelf_recently_added) },
					items = state.suggestions,
					keyPrefix = "suggestion",
					actions = actions,
					firstResultRequester = firstResultRequester,
				)

				!state.hasResults -> item(key = "status") {
					Text(
						text = if (state.isSearching || state.searchedQuery != state.query.trim()) stringResource(R.string.lagoon_search_working)
						else stringResource(R.string.lagoon_search_none, state.query.trim()),
						fontSize = 17.sp,
						color = JellyfinTheme.colorScheme.listCaption,
						modifier = Modifier.padding(top = 8.dp),
					)
				}

				else -> {
					posterGrid(
						title = { stringResource(R.string.lagoon_search_titles) },
						items = state.titles,
						keyPrefix = "title",
						actions = actions,
						firstResultRequester = firstResultRequester,
					)
					if (state.episodes.isNotEmpty()) item(key = "episodes") {
						ResultSection(stringResource(R.string.lagoon_episodes)) {
							LazyRow(
								horizontalArrangement = Arrangement.spacedBy(LagoonDimens.CardGap),
								contentPadding = PaddingValues(vertical = 10.dp),
								modifier = Modifier.focusRestorer().focusGroup(),
							) {
								itemsIndexed(state.episodes, key = { _, episode -> episode.id }) { index, episode ->
									// With no titles found, the first episode is where a system search lands
									val isFirstResult = index == 0 && state.titles.isEmpty()
									LandscapeCard(
										item = episode,
										onClick = { actions.openDetails(episode) },
										onFocused = {},
										modifier = if (isFirstResult) Modifier.focusRequester(firstResultRequester) else Modifier,
									)
								}
							}
						}
					}
				}
			}
		}
	}
}

private fun LazyListScope.posterGrid(
	title: @Composable () -> String,
	items: List<BaseItemDto>,
	keyPrefix: String,
	actions: LagoonActions,
	firstResultRequester: FocusRequester,
) {
	if (items.isEmpty()) return
	item(key = "$keyPrefix-title") {
		Text(
			text = title(),
			fontSize = 19.sp,
			fontWeight = FontWeight.SemiBold,
			color = Color.White,
			modifier = Modifier.padding(bottom = LagoonDimens.ShelfTitleGap),
		)
	}
	items.chunked(GridColumns).forEachIndexed { rowIndex, row ->
		item(key = "$keyPrefix-row-$rowIndex") {
			Row(
				horizontalArrangement = Arrangement.spacedBy(LagoonDimens.CardGap),
				modifier = Modifier.padding(vertical = 8.dp),
			) {
				row.forEachIndexed { columnIndex, item ->
					val isFirst = rowIndex == 0 && columnIndex == 0
					PosterCard(
						item = item,
						onClick = { actions.openDetails(item) },
						onFocused = {},
						modifier = (if (isFirst) Modifier.focusRequester(firstResultRequester) else Modifier).weight(1f),
					)
				}
				repeat(GridColumns - row.size) { Spacer(Modifier.weight(1f)) }
			}
		}
	}
}

@Composable
private fun ResultSection(title: String, content: @Composable () -> Unit) {
	Column(Modifier.padding(top = 18.dp)) {
		Text(
			text = title,
			fontSize = 19.sp,
			fontWeight = FontWeight.SemiBold,
			color = Color.White,
			modifier = Modifier.padding(bottom = LagoonDimens.ShelfTitleGap),
		)
		content()
	}
}

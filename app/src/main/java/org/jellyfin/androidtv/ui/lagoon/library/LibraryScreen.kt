package org.jellyfin.androidtv.ui.lagoon.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.lagoon.browse.LagoonActions
import org.jellyfin.androidtv.ui.lagoon.browse.LagoonChrome
import org.jellyfin.androidtv.ui.lagoon.browse.PosterCard
import org.jellyfin.androidtv.ui.lagoon.browse.ShelfBrowser
import org.jellyfin.androidtv.ui.lagoon.browse.railKeyOf
import org.jellyfin.androidtv.ui.lagoon.design.Chip
import org.jellyfin.androidtv.ui.lagoon.design.LagoonDimens
import org.jellyfin.androidtv.ui.lagoon.design.PinFocusedChild
import org.jellyfin.androidtv.ui.lagoon.design.RequestInitialFocus
import org.jellyfin.sdk.model.api.BaseItemDto

private const val GridColumns = 6
private val LibrarySpotlightHeight = 330.dp

@Composable
fun LibraryScreen(
	viewModel: LibraryViewModel,
	actions: LagoonActions,
	profileName: String,
	profileImageUrl: String?,
) {
	val state by viewModel.state.collectAsState()

	LagoonChrome(
		selectedKey = railKeyOf(viewModel.library),
		libraries = state.libraries,
		profileName = profileName,
		profileImageUrl = profileImageUrl,
		actions = actions,
	) {
		when (state.mode) {
			LibraryMode.Browse -> ShelfBrowser(
				featured = state.featured,
				shelves = state.shelves,
				isLoading = state.isLoading,
				focusMemory = viewModel,
				actions = actions,
				spotlightHeight = LibrarySpotlightHeight,
				header = { LibraryHeader(viewModel.library, state, viewModel) },
			)

			LibraryMode.AllTitles -> AllTitles(viewModel, state, actions)
		}
	}
}

@Composable
private fun LibraryHeader(library: BaseItemDto, state: LibraryUiState, viewModel: LibraryViewModel) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(10.dp),
		modifier = Modifier
			.focusRestorer()
			.focusGroup(),
	) {
		Text(
			text = library.name.orEmpty(),
			fontSize = 26.sp,
			fontWeight = FontWeight.Bold,
			color = Color.White,
			modifier = Modifier.padding(end = 14.dp),
		)
		Chip(
			label = stringResource(R.string.lagoon_tab_browse),
			isSelected = state.mode == LibraryMode.Browse,
			onClick = { viewModel.showMode(LibraryMode.Browse) },
		)
		Chip(
			label = stringResource(R.string.lagoon_tab_all_titles),
			isSelected = state.mode == LibraryMode.AllTitles,
			onClick = { viewModel.showMode(LibraryMode.AllTitles) },
		)

		if (state.mode == LibraryMode.AllTitles) {
			Spacer(Modifier.width(18.dp))
			Chip(label = stringResource(state.sort.label), onClick = viewModel::cycleSort)
			Chip(
				label = stringResource(R.string.lagoon_filter_unwatched),
				isSelected = state.unwatchedOnly,
				onClick = viewModel::toggleUnwatched,
			)
			if (state.totalTitles > 0) {
				Text(
					text = pluralStringResource(R.plurals.lagoon_title_count, state.totalTitles, state.totalTitles),
					fontSize = 14.sp,
					color = JellyfinTheme.colorScheme.listCaption,
					modifier = Modifier.padding(start = 8.dp),
				)
			}
		}
	}
}

@Composable
private fun AllTitles(viewModel: LibraryViewModel, state: LibraryUiState, actions: LagoonActions) {
	val restoreIndex = remember { viewModel.lastGridFocus }
	val gridState = rememberLazyGridState(initialFirstVisibleItemIndex = restoreIndex ?: 0)
	val restoreRequester = remember { FocusRequester() }
	val firstRequester = remember { FocusRequester() }

	BackHandler { viewModel.showMode(LibraryMode.Browse) }

	val hasTitles = state.titles.isNotEmpty()
	val isFocusInsideContent = remember { mutableStateOf(false) }
	// Keyed on arrival only, so re-sorting leaves focus on the chip that was pressed
	RequestInitialFocus(
		key = Unit,
		requester = { if (restoreIndex != null) restoreRequester else firstRequester },
		isFocusInsideContent = isFocusInsideContent,
		isEnabled = hasTitles,
	)

	Column(
		Modifier
			.fillMaxSize()
			.onFocusChanged { isFocusInsideContent.value = it.hasFocus }
	) {
		Box(Modifier.padding(start = LagoonDimens.ContentStart, top = 28.dp, bottom = 8.dp)) {
			LibraryHeader(viewModel.library, state, viewModel)
		}

		if (!hasTitles && !state.isLoadingTitles) {
			Text(
				text = stringResource(R.string.lagoon_library_empty),
				fontSize = 16.sp,
				color = JellyfinTheme.colorScheme.listCaption,
				modifier = Modifier.padding(start = LagoonDimens.ContentStart, top = 24.dp),
			)
			return@Column
		}

		PinFocusedChild(leadingEdge = 16.dp) {
			LazyVerticalGrid(
				columns = GridCells.Fixed(GridColumns),
				state = gridState,
				contentPadding = PaddingValues(start = LagoonDimens.ContentStart, end = LagoonDimens.ContentStart, top = 16.dp, bottom = 120.dp),
				horizontalArrangement = Arrangement.spacedBy(LagoonDimens.CardGap),
				verticalArrangement = Arrangement.spacedBy(20.dp),
				modifier = Modifier
					.fillMaxSize()
					.focusRestorer()
					.focusGroup(),
			) {
				itemsIndexed(state.titles, key = { _, item -> item.id }) { index, item ->
					val requester = when {
						index == restoreIndex -> Modifier.focusRequester(restoreRequester)
						index == 0 -> Modifier.focusRequester(firstRequester)
						else -> Modifier
					}
					TitleTile(
						item = item,
						onClick = { actions.openDetails(item) },
						onFocused = { viewModel.onTitleFocused(index) },
						cardModifier = requester,
					)
				}
			}
		}
	}
}

@Composable
private fun TitleTile(
	item: BaseItemDto,
	onClick: () -> Unit,
	onFocused: () -> Unit,
	cardModifier: Modifier,
) {
	Column {
		PosterCard(
			item = item,
			onClick = onClick,
			onFocused = onFocused,
			modifier = cardModifier.fillMaxWidth(),
		)
		Text(
			text = item.name.orEmpty(),
			fontSize = 13.sp,
			fontWeight = FontWeight.SemiBold,
			color = Color.White,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(top = 8.dp, start = 2.dp),
		)
		item.productionYear?.let { year ->
			Text(
				text = year.toString(),
				fontSize = 12.sp,
				color = JellyfinTheme.colorScheme.listCaption,
				modifier = Modifier.padding(start = 2.dp),
			)
		}
	}
}

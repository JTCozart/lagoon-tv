package org.jellyfin.androidtv.ui.lagoon.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jellyfin.androidtv.ui.lagoon.browse.HomeRailKey
import org.jellyfin.androidtv.ui.lagoon.browse.LagoonActions
import org.jellyfin.androidtv.ui.lagoon.browse.LagoonChrome
import org.jellyfin.androidtv.ui.lagoon.browse.ShelfBrowser

@Composable
fun HomeScreen(
	viewModel: HomeViewModel,
	actions: LagoonActions,
	profileName: String,
	profileImageUrl: String?,
) {
	val state by viewModel.state.collectAsState()

	LagoonChrome(
		selectedKey = HomeRailKey,
		libraries = state.libraries,
		profileName = profileName,
		profileImageUrl = profileImageUrl,
		actions = actions,
	) {
		ShelfBrowser(
			featured = state.featured,
			shelves = state.shelves,
			isLoading = state.isLoading,
			focusMemory = viewModel,
			actions = actions,
		)
	}
}

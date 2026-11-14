package org.jellyfin.androidtv.ui.lagoon.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.res.stringResource
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.lagoon.design.LagoonDimens
import org.jellyfin.androidtv.ui.lagoon.design.NavRail
import org.jellyfin.androidtv.ui.lagoon.design.RailDestination
import org.jellyfin.sdk.model.api.BaseItemDto

const val HomeRailKey = "home"
const val SearchRailKey = "search"
private const val SettingsRailKey = "settings"

fun railKeyOf(library: BaseItemDto) = library.id.toString()

/**
 * The frame every Lagoon screen sits in: the navigation rail on the left and the screen beside it.
 */
@Composable
fun LagoonChrome(
	selectedKey: String,
	libraries: List<BaseItemDto>,
	profileName: String,
	profileImageUrl: String?,
	actions: LagoonActions,
	content: @Composable () -> Unit,
) {
	val contentRequester = remember { FocusRequester() }

	Box(
		Modifier
			.fillMaxSize()
			.background(JellyfinTheme.colorScheme.background)
	) {
		Box(
			Modifier
				.fillMaxSize()
				.padding(start = LagoonDimens.RailCollapsedWidth)
				.focusRequester(contentRequester)
				.focusRestorer()
				.focusGroup()
		) {
			content()
		}

		NavRail(
			profileName = profileName,
			profileImageUrl = profileImageUrl,
			onProfileClick = actions::switchUser,
			destinations = railDestinations(libraries, actions),
			settings = RailDestination(SettingsRailKey, stringResource(R.string.lbl_settings), R.drawable.ic_settings, actions::openSettings),
			selectedKey = selectedKey,
			contentRequester = contentRequester,
		)
	}
}

@Composable
private fun railDestinations(libraries: List<BaseItemDto>, actions: LagoonActions): List<RailDestination> = buildList {
	add(RailDestination(SearchRailKey, stringResource(R.string.lbl_search), R.drawable.ic_search, actions::openSearch))
	add(RailDestination(HomeRailKey, stringResource(R.string.lbl_home), R.drawable.ic_house, actions::openHome))
	libraries.forEach { library ->
		add(RailDestination(railKeyOf(library), library.name.orEmpty(), LibraryIcons.iconFor(library.collectionType)) { actions.openLibrary(library) })
	}
}

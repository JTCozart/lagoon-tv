package org.jellyfin.androidtv.ui.lagoon

import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.constant.Extras
import org.jellyfin.androidtv.ui.lagoon.details.DetailsFragment
import org.jellyfin.androidtv.ui.lagoon.library.LibraryFragment
import org.jellyfin.androidtv.ui.navigation.fragmentDestination
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

/** Screens Lagoon adds on top of the upstream ones in `Destinations`. */
object LagoonDestinations {
	/** Item kinds that open the Lagoon details page; everything else keeps the upstream one. */
	val detailKinds = setOf(BaseItemKind.MOVIE, BaseItemKind.SERIES, BaseItemKind.SEASON, BaseItemKind.EPISODE)

	fun details(itemId: UUID) = fragmentDestination<DetailsFragment> {
		putString(DetailsFragment.ArgumentItemId, itemId.toString())
	}

	fun library(library: BaseItemDto) = fragmentDestination<LibraryFragment> {
		putString(Extras.Folder, Json.encodeToString(library))
	}
}

package org.jellyfin.androidtv.ui.lagoon.browse

import org.jellyfin.sdk.model.api.BaseItemDto
import java.util.UUID

/**
 * Everything the home screen can ask the rest of the app to do. The screen stays free of navigation
 * and playback details, which belong to the upstream app.
 */
interface LagoonActions {
	fun openDetails(item: BaseItemDto)
	fun play(item: BaseItemDto)
	fun playFromStart(item: BaseItemDto)
	fun openPerson(personId: UUID)
	fun openHome()
	fun openLibrary(library: BaseItemDto)
	fun openSearch()
	fun openSettings()
	fun switchUser()

	/** Lets the remote's play, pause and menu keys act on the focused item. Returns true when handled. */
	fun handleRemoteKey(item: BaseItemDto, keyCode: Int): Boolean
}

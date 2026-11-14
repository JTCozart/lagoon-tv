package org.jellyfin.androidtv.ui.lagoon.browse

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.lagoon.LagoonDestinations
import org.jellyfin.androidtv.ui.lagoon.library.LibraryContent
import org.jellyfin.androidtv.ui.navigation.ActivityDestinations
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.settings.compat.SettingsViewModel
import org.jellyfin.androidtv.util.KeyProcessor
import org.jellyfin.androidtv.util.PlaybackHelper
import org.jellyfin.sdk.model.api.BaseItemDto
import java.util.UUID
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

/**
 * Carries out Lagoon screen requests with the upstream app's own navigation, playback and session code.
 */
class UpstreamLagoonActions(
	private val activity: FragmentActivity,
	private val isInsideLibrary: Boolean,
	private val navigationRepository: NavigationRepository,
	private val itemLauncher: ItemLauncher,
	private val playbackHelper: PlaybackHelper,
	private val playTargetResolver: PlayTargetResolver,
	private val sessionRepository: SessionRepository,
	private val mediaManager: MediaManager,
	private val keyProcessor: KeyProcessor,
	private val settingsViewModel: SettingsViewModel,
) : LagoonActions {
	override fun openDetails(item: BaseItemDto) {
		val destination = if (item.type in LagoonDestinations.detailKinds) LagoonDestinations.details(item.id) else Destinations.itemDetails(item.id)
		navigationRepository.navigate(destination)
	}

	override fun play(item: BaseItemDto) {
		activity.lifecycleScope.launch {
			val target = playTargetResolver.resolve(item)
			playbackHelper.retrieveAndPlay(target, false, activity)
		}
	}

	override fun playFromStart(item: BaseItemDto) {
		playbackHelper.retrieveAndPlay(listOf(item.id), false, 0L, null, activity)
	}

	override fun openPerson(personId: UUID) {
		navigationRepository.navigate(Destinations.itemDetails(personId))
	}

	override fun openHome() {
		if (isInsideLibrary) navigationRepository.reset(Destinations.home, clearHistory = true)
	}

	override fun openLibrary(library: BaseItemDto) {
		if (LibraryContent.itemKindsFor(library.collectionType) == null) {
			itemLauncher.launchUserView(library)
			return
		}

		// Moving between libraries swaps the page rather than stacking them, so Back still returns home
		navigationRepository.navigate(LagoonDestinations.library(library), replace = isInsideLibrary)
	}

	override fun openSearch() {
		navigationRepository.navigate(Destinations.search())
	}

	override fun openSettings() {
		settingsViewModel.show()
	}

	override fun switchUser() {
		mediaManager.clearAudioQueue()
		sessionRepository.destroyCurrentSession()
		activity.startActivity(ActivityDestinations.startup(activity))
		activity.finishAfterTransition()
	}

	override fun handleRemoteKey(item: BaseItemDto, keyCode: Int): Boolean =
		keyProcessor.handleKey(keyCode, BaseItemDtoBaseRowItem(item), activity)
}

@Composable
fun rememberLagoonActions(isInsideLibrary: Boolean): LagoonActions {
	val activity = LocalActivity.current as FragmentActivity
	val settingsViewModel = koinActivityViewModel<SettingsViewModel>()
	val navigationRepository = koinInject<NavigationRepository>()
	val itemLauncher = koinInject<ItemLauncher>()
	val playbackHelper = koinInject<PlaybackHelper>()
	val playTargetResolver = koinInject<PlayTargetResolver>()
	val sessionRepository = koinInject<SessionRepository>()
	val mediaManager = koinInject<MediaManager>()
	val keyProcessor = koinInject<KeyProcessor>()

	return remember(activity, isInsideLibrary, settingsViewModel) {
		UpstreamLagoonActions(
			activity = activity,
			isInsideLibrary = isInsideLibrary,
			navigationRepository = navigationRepository,
			itemLauncher = itemLauncher,
			playbackHelper = playbackHelper,
			playTargetResolver = playTargetResolver,
			sessionRepository = sessionRepository,
			mediaManager = mediaManager,
			keyProcessor = keyProcessor,
			settingsViewModel = settingsViewModel,
		)
	}
}

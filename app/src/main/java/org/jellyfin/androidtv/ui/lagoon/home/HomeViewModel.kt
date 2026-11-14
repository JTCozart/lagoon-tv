package org.jellyfin.androidtv.ui.lagoon.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.ui.lagoon.browse.BrowseFocus
import org.jellyfin.androidtv.ui.lagoon.browse.FeaturedPicker
import org.jellyfin.androidtv.ui.lagoon.browse.FocusMemory
import org.jellyfin.androidtv.ui.lagoon.browse.Shelf
import org.jellyfin.androidtv.ui.lagoon.browse.ShelfCardStyle
import org.jellyfin.sdk.api.client.exception.ApiClientException
import org.jellyfin.sdk.model.api.BaseItemDto
import timber.log.Timber

data class HomeUiState(
	val isLoading: Boolean = true,
	val featured: List<BaseItemDto> = emptyList(),
	val shelves: List<Shelf> = emptyList(),
	val libraries: List<BaseItemDto> = emptyList(),
)

class HomeViewModel(
	private val userViewsRepository: UserViewsRepository,
	private val userRepository: UserRepository,
	private val userSettingPreferences: UserSettingPreferences,
	private val shelfLoader: ShelfLoader,
) : ViewModel(), FocusMemory {
	private val _state = MutableStateFlow(HomeUiState())
	val state: StateFlow<HomeUiState> = _state.asStateFlow()

	override var lastFocus: BrowseFocus? = null

	private var loadJob: Job? = null

	fun refresh() {
		loadJob?.cancel()
		loadJob = viewModelScope.launch {
			val libraries = userViewsRepository.views.first().toList()
			val plan = HomeShelfPlanner.plan(
				sections = userSettingPreferences.activeHomesections,
				libraries = libraries,
				excludedFromLatest = userRepository.currentUser.value?.configuration?.latestItemsExcludes.orEmpty().toSet(),
			)

			val shelves = plan
				.map { shelfPlan -> async { loadOrNull(shelfPlan) } }
				.awaitAll()
				.filterNotNull()
				.filter { it.items.isNotEmpty() }

			val latestShelves = shelves.filter { it.cardStyle == ShelfCardStyle.Poster }.map { it.items }

			_state.update {
				HomeUiState(
					isLoading = false,
					featured = FeaturedPicker.pick(latestShelves),
					shelves = shelves,
					libraries = libraries,
				)
			}
		}
	}

	private suspend fun loadOrNull(plan: ShelfPlan): Shelf? = try {
		shelfLoader.load(plan)
	} catch (error: ApiClientException) {
		// One failing shelf should not blank the whole home screen
		Timber.w(error, "Home shelf failed to load plan=%s", plan)
		null
	}
}

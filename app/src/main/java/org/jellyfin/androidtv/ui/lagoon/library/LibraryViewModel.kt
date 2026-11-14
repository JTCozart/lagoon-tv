package org.jellyfin.androidtv.ui.lagoon.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import org.jellyfin.androidtv.ui.lagoon.browse.BrowseFocus
import org.jellyfin.androidtv.ui.lagoon.browse.FeaturedPicker
import org.jellyfin.androidtv.ui.lagoon.browse.FocusMemory
import org.jellyfin.androidtv.ui.lagoon.browse.Shelf
import org.jellyfin.androidtv.ui.lagoon.browse.ShelfCardStyle
import org.jellyfin.sdk.api.client.exception.ApiClientException
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import timber.log.Timber
import java.util.UUID

class LibraryViewModel(
	val library: BaseItemDto,
	private val loader: LibraryLoader,
	private val userViewsRepository: UserViewsRepository,
) : ViewModel(), FocusMemory {
	private val _state = MutableStateFlow(LibraryUiState())
	val state: StateFlow<LibraryUiState> = _state.asStateFlow()

	private val itemKind = LibraryContent.itemKindsFor(library.collectionType)?.first() ?: BaseItemKind.MOVIE

	override var lastFocus: BrowseFocus? = null
	var lastGridFocus: Int? = null

	private var browseJob: Job? = null
	private var titlesJob: Job? = null

	fun refresh() {
		browseJob?.cancel()
		browseJob = viewModelScope.launch {
			val libraries = userViewsRepository.views.first().toList()
			val shelves = loadOrEmpty { loader.browseShelves(library, itemKind) }
			val featured = FeaturedPicker.pick(shelves.filter { it.cardStyle == ShelfCardStyle.Poster }.map { it.items })

			_state.update { it.copy(isLoading = false, libraries = libraries, shelves = shelves, featured = featured) }
		}
		if (_state.value.mode == LibraryMode.AllTitles) reloadTitles()
	}

	fun showMode(mode: LibraryMode) {
		if (_state.value.mode == mode) return
		_state.update { it.copy(mode = mode) }
		if (mode == LibraryMode.AllTitles && _state.value.titles.isEmpty()) reloadTitles()
		if (mode == LibraryMode.AllTitles && _state.value.categories.isEmpty()) loadCategories()
	}

	/** Narrows All titles to one genre; null shows every title again. */
	fun selectCategory(categoryId: UUID?) {
		if (_state.value.selectedCategoryId == categoryId) return
		_state.update { it.copy(selectedCategoryId = categoryId) }
		reloadTitles()
	}

	fun cycleSort() {
		_state.update { it.copy(sort = it.sort.next()) }
		reloadTitles()
	}

	fun toggleUnwatched() {
		_state.update { it.copy(unwatchedOnly = !it.unwatchedOnly) }
		reloadTitles()
	}

	fun onTitleFocused(index: Int) {
		lastGridFocus = index
		val current = _state.value
		if (TitlePaging.shouldLoadMore(index, current.titles.size, current.totalTitles, current.isLoadingTitles)) loadNextPage()
	}

	private fun reloadTitles() {
		lastGridFocus = null
		titlesJob?.cancel()
		_state.update { it.copy(titles = emptyList(), totalTitles = 0) }
		loadNextPage()
	}

	private fun loadNextPage() {
		titlesJob = viewModelScope.launch {
			_state.update { it.copy(isLoadingTitles = true) }
			val current = _state.value
			val page = try {
				loader.page(
					library = library,
					itemKind = itemKind,
					sort = current.sort,
					unwatchedOnly = current.unwatchedOnly,
					categoryId = current.selectedCategoryId,
					startIndex = current.titles.size,
				)
			} catch (error: ApiClientException) {
				Timber.w(error, "Library page failed to load library=%s", library.id)
				null
			}

			_state.update {
				if (page == null) it.copy(isLoadingTitles = false)
				else it.copy(titles = it.titles + page.items, totalTitles = page.totalCount, isLoadingTitles = false)
			}
		}
	}

	private fun loadCategories() {
		viewModelScope.launch {
			val categories = try {
				loader.categories(library, itemKind)
			} catch (error: ApiClientException) {
				// Without categories the grid still works; it just cannot be narrowed
				Timber.w(error, "Library categories failed to load library=%s", library.id)
				emptyList()
			}
			_state.update { it.copy(categories = categories) }
		}
	}

	private suspend fun loadOrEmpty(load: suspend () -> List<Shelf>): List<Shelf> = try {
		load()
	} catch (error: ApiClientException) {
		Timber.w(error, "Library browse failed to load library=%s", library.id)
		emptyList()
	}
}

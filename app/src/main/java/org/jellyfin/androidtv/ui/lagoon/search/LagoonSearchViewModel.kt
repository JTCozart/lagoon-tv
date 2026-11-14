package org.jellyfin.androidtv.ui.lagoon.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import org.jellyfin.androidtv.ui.search.SearchRepository
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.exception.ApiClientException
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemKind
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class LagoonSearchViewModel(
	private val searchRepository: SearchRepository,
	private val userViewsRepository: UserViewsRepository,
	private val api: ApiClient,
) : ViewModel() {
	private companion object {
		val TypingPause = 350.milliseconds
		const val SuggestionLimit = 20
		val titleKinds = setOf(BaseItemKind.MOVIE, BaseItemKind.SERIES, BaseItemKind.BOX_SET)
		val episodeKinds = setOf(BaseItemKind.EPISODE)
	}

	private val _state = MutableStateFlow(SearchUiState())
	val state: StateFlow<SearchUiState> = _state.asStateFlow()

	private var searchJob: Job? = null

	init {
		viewModelScope.launch {
			val libraries = userViewsRepository.views.first().toList()
			_state.update { it.copy(libraries = libraries) }
		}
		viewModelScope.launch { loadSuggestions() }
	}

	fun type(character: Char) = setQuery(QueryEditor.type(_state.value.query, character))
	fun deleteLast() = setQuery(QueryEditor.deleteLast(_state.value.query))
	fun clear() = setQuery("")

	fun setQuery(query: String, pause: Duration = TypingPause) {
		if (query == _state.value.query) return
		_state.update { it.copy(query = query) }

		searchJob?.cancel()
		val trimmed = query.trim()
		if (trimmed.isEmpty()) {
			_state.update { it.copy(isSearching = false, searchedQuery = "", titles = emptyList(), episodes = emptyList()) }
			return
		}

		searchJob = viewModelScope.launch {
			delay(pause)
			_state.update { it.copy(isSearching = true) }

			val titles = async { searchRepository.search(trimmed, titleKinds).getOrNull().orEmpty() }
			val episodes = async { searchRepository.search(trimmed, episodeKinds).getOrNull().orEmpty() }

			_state.update {
				it.copy(
					isSearching = false,
					searchedQuery = trimmed,
					titles = SearchRanker.rank(titles.await(), trimmed),
					episodes = episodes.await(),
				)
			}
		}
	}

	private suspend fun loadSuggestions() {
		val suggestions = try {
			withContext(Dispatchers.IO) {
				api.userLibraryApi.getLatestMedia(
					fields = ItemRepository.browseFields,
					includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
					groupItems = true,
					limit = SuggestionLimit,
				).content
			}
		} catch (error: ApiClientException) {
			Timber.w(error, "Search suggestions failed to load")
			emptyList()
		}
		_state.update { it.copy(suggestions = suggestions) }
	}
}

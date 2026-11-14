package org.jellyfin.androidtv.ui.lagoon.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.data.repository.ItemMutationRepository
import org.jellyfin.sdk.api.client.exception.ApiClientException
import org.jellyfin.sdk.model.api.BaseItemKind
import timber.log.Timber
import java.util.UUID

class DetailsViewModel(
	private val itemId: UUID,
	private val loader: DetailsLoader,
	private val itemMutationRepository: ItemMutationRepository,
) : ViewModel() {
	private val _state = MutableStateFlow(DetailsUiState())
	val state: StateFlow<DetailsUiState> = _state.asStateFlow()

	private var loadJob: Job? = null
	private var episodesJob: Job? = null

	fun refresh() {
		loadJob?.cancel()
		loadJob = viewModelScope.launch {
			try {
				load()
			} catch (error: ApiClientException) {
				Timber.w(error, "Details failed to load item=%s", itemId)
				_state.update { it.copy(isLoading = false, hasFailed = it.title == null) }
			}
		}
	}

	fun selectSeason(seasonId: UUID) {
		val seriesId = _state.value.title?.id ?: return
		if (seasonId == _state.value.selectedSeasonId) return
		_state.update { it.copy(selectedSeasonId = seasonId, episodes = emptyList(), highlightedEpisodeId = null) }

		episodesJob?.cancel()
		episodesJob = viewModelScope.launch {
			val episodes = orEmpty { loader.episodes(seriesId, seasonId) }
			_state.update { it.copy(episodes = episodes) }
		}
	}

	fun toggleFavorite() {
		val title = _state.value.title ?: return
		val favorite = !_state.value.isFavorite
		_state.update { it.copy(isFavorite = favorite) }
		viewModelScope.launch {
			runMutation { itemMutationRepository.setFavorite(title.id, favorite) }
		}
	}

	fun togglePlayed() {
		val title = _state.value.title ?: return
		val played = !_state.value.isPlayed
		_state.update { it.copy(isPlayed = played) }
		viewModelScope.launch {
			runMutation { itemMutationRepository.setPlayed(title.id, played) }
			refresh()
		}
	}

	private suspend fun load() {
		val opened = loader.item(itemId)
		val titleId = SeriesPageEntry.titleIdOf(opened)
		val title = if (titleId == opened.id) opened else loader.item(titleId)
		val isSeries = title.type == BaseItemKind.SERIES

		viewModelScope.launch {
			val similar = orEmpty { loader.similar(title.id) }
			_state.update { it.copy(similar = similar) }
		}

		if (!isSeries) {
			_state.update {
				it.copy(
					isLoading = false,
					title = title,
					playPlan = PlayPlan.of(title),
					badges = MediaBadges.of(title),
					isFavorite = title.userData?.isFavorite == true,
					isPlayed = title.userData?.played == true,
					cast = CastList.of(title.people.orEmpty()),
				)
			}
			return
		}

		val seasons = viewModelScope.async { orEmpty { loader.seasons(title.id) } }
		val nextUp = if (opened.type == BaseItemKind.EPISODE) null else orNull { loader.nextEpisode(title.id) }
		val startingEpisode = SeriesPageEntry.startingEpisode(opened, nextUp)
		val seasonList = seasons.await()
		val seasonId = SeriesPageEntry.seasonToShow(opened, startingEpisode, seasonList, _state.value.selectedSeasonId)
		val episodes = seasonId?.let { orEmpty { loader.episodes(title.id, it) } }.orEmpty()
		val playTarget = SeriesPageEntry.playTarget(startingEpisode, episodes)

		_state.update {
			it.copy(
				isLoading = false,
				title = title,
				playPlan = playTarget?.let(PlayPlan::of),
				badges = playTarget?.let(MediaBadges::of).orEmpty(),
				isFavorite = title.userData?.isFavorite == true,
				isPlayed = title.userData?.played == true,
				seasons = seasonList,
				selectedSeasonId = seasonId,
				episodes = episodes,
				highlightedEpisodeId = playTarget?.id,
				cast = CastList.of(title.people.orEmpty()),
			)
		}
	}

	private suspend fun runMutation(mutation: suspend () -> Unit) {
		try {
			mutation()
		} catch (error: ApiClientException) {
			Timber.w(error, "Details update failed item=%s", itemId)
		}
	}

	private suspend fun <T> orEmpty(load: suspend () -> List<T>): List<T> = orNull(load).orEmpty()

	private suspend fun <T> orNull(load: suspend () -> T): T? = try {
		load()
	} catch (error: ApiClientException) {
		Timber.w(error, "Details section failed to load item=%s", itemId)
		null
	}
}

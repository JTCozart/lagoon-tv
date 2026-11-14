package org.jellyfin.androidtv.ui.lagoon.home

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.lagoon.browse.Shelf
import org.jellyfin.androidtv.ui.lagoon.browse.ShelfCardStyle
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaType
import java.time.Clock
import java.time.LocalDateTime

class JellyfinShelfLoader(
	private val context: Context,
	private val api: ApiClient,
	private val userPreferences: UserPreferences,
	private val clock: Clock,
) : ShelfLoader {
	private companion object {
		const val ContinueWatchingLimit = 30
		const val NextUpLimit = 30
		const val LatestLimit = 30
	}

	override suspend fun load(plan: ShelfPlan): Shelf = withContext(Dispatchers.IO) {
		when (plan) {
			ShelfPlan.ContinueWatching -> continueWatching()
			ShelfPlan.NextUp -> nextUp()
			is ShelfPlan.LatestIn -> latestIn(plan)
		}
	}

	private suspend fun continueWatching(): Shelf {
		val items = api.itemsApi.getResumeItems(
			fields = ItemRepository.browseFields,
			limit = ContinueWatchingLimit,
			mediaTypes = listOf(MediaType.VIDEO),
			excludeItemTypes = listOf(BaseItemKind.AUDIO_BOOK),
			enableTotalRecordCount = false,
		).content.items

		return Shelf("continue", context.getString(R.string.lbl_continue_watching), ShelfCardStyle.Landscape, items)
	}

	private suspend fun nextUp(): Shelf {
		val maxDays = userPreferences[UserPreferences.homeNextUpMaxDays]
		val items = api.tvShowsApi.getNextUp(
			fields = ItemRepository.browseFields,
			limit = NextUpLimit,
			enableResumable = false,
			nextUpDateCutoff = maxDays.takeIf { it > 0 }?.let { LocalDateTime.now(clock).minusDays(it.toLong()) },
		).content.items

		return Shelf("nextup", context.getString(R.string.lbl_next_up), ShelfCardStyle.Landscape, items)
	}

	private suspend fun latestIn(plan: ShelfPlan.LatestIn): Shelf {
		val items = api.userLibraryApi.getLatestMedia(
			fields = ItemRepository.browseFields,
			parentId = plan.library.id,
			groupItems = true,
			limit = LatestLimit,
		).content

		return Shelf(
			key = "latest-${plan.library.id}",
			title = context.getString(R.string.lbl_latest_in, plan.library.name),
			cardStyle = ShelfCardStyle.Poster,
			items = items,
		)
	}
}

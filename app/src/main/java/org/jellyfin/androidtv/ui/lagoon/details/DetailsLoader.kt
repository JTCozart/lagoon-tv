package org.jellyfin.androidtv.ui.lagoon.details

import org.jellyfin.sdk.model.api.BaseItemDto
import java.util.UUID

interface DetailsLoader {
	suspend fun item(id: UUID): BaseItemDto
	suspend fun seasons(seriesId: UUID): List<BaseItemDto>
	suspend fun episodes(seriesId: UUID, seasonId: UUID): List<BaseItemDto>
	suspend fun nextEpisode(seriesId: UUID): BaseItemDto?
	suspend fun similar(id: UUID): List<BaseItemDto>
}

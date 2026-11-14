package org.jellyfin.androidtv.ui.lagoon.browse

import androidx.annotation.DrawableRes
import org.jellyfin.androidtv.R
import org.jellyfin.sdk.model.api.CollectionType

object LibraryIcons {
	@DrawableRes
	fun iconFor(collectionType: CollectionType?): Int = when (collectionType) {
		CollectionType.MOVIES -> R.drawable.ic_movie
		CollectionType.TVSHOWS -> R.drawable.ic_tv
		CollectionType.MUSIC, CollectionType.MUSICVIDEOS -> R.drawable.ic_music_album
		CollectionType.LIVETV -> R.drawable.ic_tv_guide
		CollectionType.PHOTOS, CollectionType.HOMEVIDEOS -> R.drawable.ic_photos
		CollectionType.PLAYLISTS -> R.drawable.ic_grid
		else -> R.drawable.ic_folder
	}
}

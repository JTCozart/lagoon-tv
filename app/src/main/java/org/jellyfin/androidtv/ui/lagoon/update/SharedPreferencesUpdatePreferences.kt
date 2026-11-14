package org.jellyfin.androidtv.ui.lagoon.update

import android.content.Context
import androidx.core.content.edit
import java.time.Instant

class SharedPreferencesUpdatePreferences(context: Context) : UpdatePreferences {
	private companion object {
		const val FileName = "lagoon_updates"
		const val LastCheckKey = "last_check_epoch_ms"
		const val DismissedVersionKey = "dismissed_version"
	}

	private val preferences = context.getSharedPreferences(FileName, Context.MODE_PRIVATE)

	override var lastCheck: Instant?
		get() = preferences.getLong(LastCheckKey, -1L).takeIf { it >= 0 }?.let(Instant::ofEpochMilli)
		set(value) = preferences.edit {
			if (value == null) remove(LastCheckKey) else putLong(LastCheckKey, value.toEpochMilli())
		}

	override var dismissedVersion: AppVersion?
		get() = preferences.getString(DismissedVersionKey, null)?.let(AppVersion::parse)
		set(value) = preferences.edit {
			if (value == null) remove(DismissedVersionKey) else putString(DismissedVersionKey, value.toString())
		}
}

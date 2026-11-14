package org.jellyfin.androidtv.ui.lagoon.update

import java.time.Instant

/** What the update check remembers between launches. */
interface UpdatePreferences {
	var lastCheck: Instant?
	var dismissedVersion: AppVersion?
}

package org.jellyfin.androidtv.ui.lagoon.update

import java.time.Duration
import java.time.Instant

/** When Lagoon looks for a new release, and when it offers one. */
object UpdateSchedule {
	private val CheckInterval = Duration.ofHours(12)

	fun isCheckDue(now: Instant, lastCheck: Instant?, installed: AppVersion): Boolean {
		if (installed.isDevelopment) return false
		return lastCheck == null || Duration.between(lastCheck, now) >= CheckInterval
	}

	fun shouldOffer(latest: AppVersion, installed: AppVersion, dismissed: AppVersion?): Boolean =
		latest > installed && (dismissed == null || latest > dismissed)
}

package org.jellyfin.androidtv.ui.lagoon.update

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.time.Clock

/**
 * Steps an update through check, offer, download and install. [installedVersion] is null when the running build's
 * version cannot be read, and then nothing is ever offered.
 */
class UpdateProcess(
	private val source: UpdateSource,
	private val preferences: UpdatePreferences,
	private val clock: Clock,
	private val installedVersion: AppVersion?,
	private val downloadFolder: File,
	private val scope: CoroutineScope,
) {
	private companion object {
		const val DownloadName = "lagoon-tv.apk"
	}

	private val _state = MutableStateFlow<UpdateState>(UpdateState.Hidden)
	val state: StateFlow<UpdateState> = _state.asStateFlow()

	private var downloadJob: Job? = null

	fun checkIfDue() {
		val installed = installedVersion ?: return
		val now = clock.instant()
		if (_state.value != UpdateState.Hidden) return
		if (!UpdateSchedule.isCheckDue(now, preferences.lastCheck, installed)) return

		preferences.lastCheck = now
		scope.launch {
			val latest = source.latestRelease() ?: return@launch
			if (UpdateSchedule.shouldOffer(latest.version, installed, preferences.dismissedVersion)) {
				_state.value = UpdateState.Offered(latest)
			}
		}
	}

	fun notNow() {
		val release = currentRelease() ?: return
		downloadJob?.cancel()
		preferences.dismissedVersion = release.version
		_state.value = UpdateState.Hidden
	}

	fun startUpdate() {
		val release = currentRelease() ?: return
		downloadJob?.cancel()
		downloadJob = scope.launch {
			_state.value = UpdateState.Downloading(release, 0f)
			val target = File(downloadFolder, DownloadName)
			_state.value = try {
				source.downloadApk(release, target) { progress -> _state.value = UpdateState.Downloading(release, progress) }
				UpdateState.ReadyToInstall(release, target)
			} catch (error: UpdateDownloadException) {
				Timber.w(error, "Update download failed version=%s", release.version)
				UpdateState.Failed(release)
			}
		}
	}

	/** The installer has taken over; if the viewer backs out of it, the offer is still there. */
	fun installerOpened() {
		val release = currentRelease() ?: return
		_state.value = UpdateState.Offered(release)
	}

	private fun currentRelease(): ReleaseInfo? = when (val state = _state.value) {
		UpdateState.Hidden -> null
		is UpdateState.Offered -> state.release
		is UpdateState.Downloading -> state.release
		is UpdateState.ReadyToInstall -> state.release
		is UpdateState.Failed -> state.release
	}
}

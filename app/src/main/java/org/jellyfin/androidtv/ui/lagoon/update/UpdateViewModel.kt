package org.jellyfin.androidtv.ui.lagoon.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.time.Clock

/** Keeps an [UpdateProcess] alive for as long as the activity, so a download survives moving between screens. */
class UpdateViewModel(
	source: UpdateSource,
	preferences: UpdatePreferences,
	clock: Clock,
	installedVersion: AppVersion?,
	downloadFolder: File,
) : ViewModel() {
	private val process = UpdateProcess(source, preferences, clock, installedVersion, downloadFolder, viewModelScope)

	val state: StateFlow<UpdateState> = process.state

	fun checkIfDue() = process.checkIfDue()
	fun notNow() = process.notNow()
	fun startUpdate() = process.startUpdate()
	fun installerOpened() = process.installerOpened()
}

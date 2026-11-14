package org.jellyfin.androidtv.ui.lagoon.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Keeps an [UpdateProcess] alive for as long as the activity, so a download survives moving between screens and
 * a new activity (a fresh launch) checks again.
 */
class UpdateViewModel(
	source: UpdateSource,
	installedVersion: AppVersion?,
	downloadFolder: File,
) : ViewModel() {
	private val process = UpdateProcess(source, installedVersion, downloadFolder, viewModelScope)

	val state: StateFlow<UpdateState> = process.state

	fun checkOnLaunch() = process.checkOnLaunch()
	fun checkNow() = process.checkNow()
	fun notNow() = process.notNow()
	fun startUpdate() = process.startUpdate()
	fun installerOpened() = process.installerOpened()
}

package org.jellyfin.androidtv.ui.lagoon.update

import java.io.File

/** Where Lagoon finds and fetches new releases. */
interface UpdateSource {
	/** The newest stable release, or null when there is none or it cannot be reached. */
	suspend fun latestRelease(): ReleaseInfo?

	/**
	 * Downloads the release's APK to [target] and confirms it matches the published checksum.
	 * Throws [UpdateDownloadException] when the download fails or the file does not match.
	 */
	suspend fun downloadApk(release: ReleaseInfo, target: File, onProgress: (fraction: Float) -> Unit)
}

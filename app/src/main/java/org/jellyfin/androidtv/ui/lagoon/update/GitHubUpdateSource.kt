package org.jellyfin.androidtv.ui.lagoon.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/**
 * Reads releases from the public GitHub repository. Only the latest stable release is considered, because
 * GitHub's "latest" leaves out pre-releases.
 */
class GitHubUpdateSource(private val client: OkHttpClient) : UpdateSource {
	private companion object {
		const val LatestReleaseUrl = "https://api.github.com/repos/JTCozart/lagoon-tv/releases/latest"
		const val UserAgent = "Lagoon-TV-update-check"
		const val BufferSize = 64 * 1024
		const val HttpNotFound = 404
	}

	override suspend fun latestRelease(): ReleaseLookup = withContext(Dispatchers.IO) {
		val request = Request.Builder()
			.url(LatestReleaseUrl)
			.header("Accept", "application/vnd.github+json")
			.header("User-Agent", UserAgent)
			.build()

		try {
			client.newCall(request).execute().use { response ->
				when {
					response.code == HttpNotFound -> ReleaseLookup.NoRelease
					!response.isSuccessful -> {
						Timber.i("Update check refused status=%d", response.code)
						ReleaseLookup.Unreachable
					}
					else -> response.body?.string()?.let(ReleaseParser::parse)?.let(ReleaseLookup::Found) ?: ReleaseLookup.NoRelease
				}
			}
		} catch (error: IOException) {
			// Best effort: being offline or rate limited only matters when the viewer asked for the check
			Timber.w(error, "Update check failed")
			ReleaseLookup.Unreachable
		}
	}

	override suspend fun downloadApk(release: ReleaseInfo, target: File, onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
		val checksumsUrl = release.checksumsUrl ?: throw UpdateDownloadException("The release has no checksum file")
		val expectedChecksum = ChecksumList.parse(fetchText(checksumsUrl))[ReleaseParser.ApkAssetName]
			?: throw UpdateDownloadException("The checksum file does not list ${ReleaseParser.ApkAssetName}")

		target.parentFile?.mkdirs()
		val partial = File(target.parentFile, target.name + ".part")
		val digest = MessageDigest.getInstance("SHA-256")

		try {
			val request = Request.Builder().url(release.apkUrl).header("User-Agent", UserAgent).build()
			client.newCall(request).execute().use { response ->
				if (!response.isSuccessful) throw UpdateDownloadException("Download failed with status ${response.code}")
				val body = response.body ?: throw UpdateDownloadException("The download was empty")
				val totalBytes = body.contentLength().takeIf { it > 0 } ?: release.apkSizeBytes
				var readBytes = 0L

				body.byteStream().use { input ->
					partial.outputStream().use { output ->
						val buffer = ByteArray(BufferSize)
						while (true) {
							ensureActive()
							val count = input.read(buffer)
							if (count < 0) break
							output.write(buffer, 0, count)
							digest.update(buffer, 0, count)
							readBytes += count
							if (totalBytes > 0) onProgress((readBytes.toFloat() / totalBytes).coerceIn(0f, 1f))
						}
					}
				}
			}
		} catch (error: IOException) {
			partial.delete()
			throw UpdateDownloadException("Download failed", error)
		}

		val actualChecksum = digest.digest().joinToString("") { "%02x".format(it) }
		if (actualChecksum != expectedChecksum) {
			partial.delete()
			throw UpdateDownloadException("The download does not match the published checksum")
		}

		target.delete()
		if (!partial.renameTo(target)) throw UpdateDownloadException("The download could not be saved")
	}

	private fun fetchText(url: String): String = try {
		client.newCall(Request.Builder().url(url).header("User-Agent", UserAgent).build()).execute().use { response ->
			if (!response.isSuccessful) throw UpdateDownloadException("Checksum download failed with status ${response.code}")
			response.body?.string() ?: throw UpdateDownloadException("The checksum file was empty")
		}
	} catch (error: IOException) {
		throw UpdateDownloadException("Checksum download failed", error)
	}
}

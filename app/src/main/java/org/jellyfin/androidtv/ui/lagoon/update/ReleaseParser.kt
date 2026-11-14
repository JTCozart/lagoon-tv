package org.jellyfin.androidtv.ui.lagoon.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Reads the GitHub "latest release" response. The APK is the fixed-name asset the release workflow publishes, so
 * the same link works for every version.
 */
object ReleaseParser {
	const val ApkAssetName = "lagoon-tv.apk"
	private const val ChecksumsAssetName = "SHA256SUMS"

	private val json = Json { ignoreUnknownKeys = true }

	fun parse(body: String): ReleaseInfo? {
		val release = try {
			json.decodeFromString<GitHubRelease>(body)
		} catch (error: SerializationException) {
			return null
		} catch (error: IllegalArgumentException) {
			return null
		}

		val version = release.tagName?.let(AppVersion::parse) ?: return null
		val apk = release.assets.firstOrNull { it.name == ApkAssetName } ?: return null

		return ReleaseInfo(
			version = version,
			apkUrl = apk.downloadUrl,
			apkSizeBytes = apk.size,
			checksumsUrl = release.assets.firstOrNull { it.name == ChecksumsAssetName }?.downloadUrl,
			notes = release.body.orEmpty(),
			pageUrl = release.pageUrl.orEmpty(),
		)
	}

	@Serializable
	private data class GitHubRelease(
		@SerialName("tag_name") val tagName: String? = null,
		@SerialName("html_url") val pageUrl: String? = null,
		val body: String? = null,
		val assets: List<GitHubAsset> = emptyList(),
	)

	@Serializable
	private data class GitHubAsset(
		val name: String,
		@SerialName("browser_download_url") val downloadUrl: String,
		val size: Long = 0,
	)
}

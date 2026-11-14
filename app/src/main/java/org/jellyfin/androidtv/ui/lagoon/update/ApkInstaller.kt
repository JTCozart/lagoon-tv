package org.jellyfin.androidtv.ui.lagoon.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File

/** Hands a downloaded APK to Android's package installer, which shows its own confirmation. */
class ApkInstaller(private val context: Context) {
	enum class Outcome { InstallerOpened, PermissionNeeded, Unavailable }

	private companion object {
		const val ApkMimeType = "application/vnd.android.package-archive"
		const val ProviderSuffix = ".updates"
	}

	fun install(apk: File): Outcome {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
			openInstallPermissionSettings()
			return Outcome.PermissionNeeded
		}

		val uri = FileProvider.getUriForFile(context, context.packageName + ProviderSuffix, apk)
		val intent = Intent(Intent.ACTION_VIEW)
			.setDataAndType(uri, ApkMimeType)
			.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)

		return try {
			context.startActivity(intent)
			Outcome.InstallerOpened
		} catch (error: ActivityNotFoundException) {
			Timber.w(error, "No package installer available")
			Outcome.Unavailable
		}
	}

	private fun openInstallPermissionSettings() {
		val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + context.packageName))
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		try {
			context.startActivity(intent)
		} catch (error: ActivityNotFoundException) {
			// Some TVs hide this screen; the dialog's hint tells the viewer where to find the setting instead
			Timber.w(error, "Install permission screen not available")
		}
	}
}

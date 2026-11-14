package org.jellyfin.androidtv.ui.settings.screen.about

import android.content.ClipData
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.jellyfin.androidtv.BuildConfig
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.lagoon.update.UpdateState
import org.jellyfin.androidtv.ui.lagoon.update.UpdateViewModel
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.navigation.focus.focusKey
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn
import org.jellyfin.androidtv.ui.settings.util.copyAction
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
fun SettingsAboutScreen(launchedFromLogin: Boolean = false) {
	val router = LocalRouter.current

	SettingsColumn {
		if (launchedFromLogin) item {
			ListSection(
				overlineContent = { Text(stringResource(R.string.pref_login).uppercase()) },
				headingContent = { Text(stringResource(R.string.pref_about_title)) },
			)
		} else item {
			ListSection(
				overlineContent = { Text(stringResource(R.string.settings).uppercase()) },
				headingContent = { Text(stringResource(R.string.pref_about_title)) },
			)
		}

		item {
			val heading = stringResource(R.string.lagoon_about_version)
			val caption = "Lagoon ${BuildConfig.VERSION_NAME} ${BuildConfig.BUILD_TYPE}"
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_info), contentDescription = null) },
				headingContent = { Text(heading) },
				captionContent = { Text(caption) },
				onClick = copyAction(ClipData.newPlainText(heading, caption)),
				modifier = Modifier.focusKey("version")
			)
		}

		// The update dialog lives in the main activity, so the sign-in screens have nowhere to show it
		if (!launchedFromLogin) item {
			CheckForUpdatesButton()
		}

		item {
			val heading = stringResource(R.string.pref_device_model)
			val caption = "${Build.MANUFACTURER} ${Build.MODEL}"
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_tv), contentDescription = null) },
				headingContent = { Text(heading) },
				captionContent = { Text(caption) },
				onClick = copyAction(ClipData.newPlainText(heading, caption)),
				modifier = Modifier.focusKey("device_model")
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_guide), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.licenses_link)) },
				onClick = { router.push(Routes.LICENSES) },
				modifier = Modifier.focusKey(Routes.LICENSES)
			)
		}

		if (!launchedFromLogin) item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_flask), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_developer_link)) },
				onClick = { router.push(Routes.DEVELOPER) },
				modifier = Modifier.focusKey(Routes.DEVELOPER)
			)
		}
	}
}

@Composable
private fun CheckForUpdatesButton() {
	val updateViewModel = koinActivityViewModel<UpdateViewModel>()
	val state by updateViewModel.state.collectAsState()
	val caption = when (val shown = state) {
		UpdateState.Checking -> stringResource(R.string.lagoon_update_checking)
		UpdateState.UpToDate -> stringResource(R.string.lagoon_update_up_to_date)
		UpdateState.CheckFailed -> stringResource(R.string.lagoon_update_check_failed)
		is UpdateState.Offered -> stringResource(R.string.lagoon_update_found, shown.release.version.toString())
		else -> stringResource(R.string.lagoon_update_check_caption)
	}

	ListButton(
		leadingContent = { Icon(painterResource(R.drawable.lagoon_ic_download), contentDescription = null) },
		headingContent = { Text(stringResource(R.string.lagoon_update_check)) },
		captionContent = { Text(caption) },
		onClick = updateViewModel::checkNow,
		modifier = Modifier.focusKey("check_for_updates"),
	)
}

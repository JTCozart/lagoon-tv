package org.jellyfin.androidtv.ui.lagoon.update

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jellyfin.androidtv.BuildConfig
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.lagoon.design.PillButton
import org.jellyfin.androidtv.ui.lagoon.design.PillEmphasis

private const val PercentScale = 100
private const val NotesMaxLines = 5
private val PanelShape = RoundedCornerShape(18.dp)

/**
 * Offers a newer Lagoon release over whatever screen is showing, and walks through download and install.
 */
@Composable
fun UpdatePrompt(viewModel: UpdateViewModel) {
	val state by viewModel.state.collectAsState()
	val context = LocalContext.current
	val installer = remember(context) { ApkInstaller(context.applicationContext) }
	var needsInstallPermission by remember { mutableStateOf(false) }

	val shown = state
	if (shown is UpdateState.ReadyToInstall) {
		LaunchedEffect(shown.apk) {
			needsInstallPermission = installer.install(shown.apk) == ApkInstaller.Outcome.PermissionNeeded
			viewModel.installerOpened()
		}
	}

	val release = when (shown) {
		UpdateState.Hidden -> return
		is UpdateState.Offered -> shown.release
		is UpdateState.Downloading -> shown.release
		is UpdateState.ReadyToInstall -> shown.release
		is UpdateState.Failed -> shown.release
	}

	Dialog(
		onDismissRequest = viewModel::notNow,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		UpdatePanel(
			state = shown,
			release = release,
			needsInstallPermission = needsInstallPermission,
			onUpdate = {
				needsInstallPermission = false
				viewModel.startUpdate()
			},
			onNotNow = viewModel::notNow,
		)
	}
}

@Composable
private fun UpdatePanel(
	state: UpdateState,
	release: ReleaseInfo,
	needsInstallPermission: Boolean,
	onUpdate: () -> Unit,
	onNotNow: () -> Unit,
) {
	val colors = JellyfinTheme.colorScheme
	val primaryRequester = remember { FocusRequester() }
	LaunchedEffect(state::class) {
		withFrameNanos { }
		runCatching { primaryRequester.requestFocus() }
	}

	Column(
		modifier = Modifier
			.width(560.dp)
			.background(colors.surface, PanelShape)
			.border(1.dp, colors.accent.copy(alpha = 0.35f), PanelShape)
			.padding(32.dp),
	) {
		Text(
			text = stringResource(R.string.lagoon_update_eyebrow).uppercase(),
			fontSize = 13.sp,
			fontWeight = FontWeight.Bold,
			color = colors.accent,
		)
		Text(
			text = stringResource(R.string.lagoon_update_title, release.version.toString()),
			fontSize = 26.sp,
			fontWeight = FontWeight.Bold,
			color = Color.White,
			modifier = Modifier.padding(top = 6.dp),
		)
		Text(
			text = stringResource(R.string.lagoon_update_installed, BuildConfig.VERSION_NAME),
			fontSize = 15.sp,
			color = colors.listCaption,
			modifier = Modifier.padding(top = 4.dp),
		)

		release.notes.trim().takeIf { it.isNotEmpty() }?.let { notes ->
			Text(
				text = notes,
				fontSize = 14.sp,
				lineHeight = 20.sp,
				color = Color.White.copy(alpha = 0.85f),
				maxLines = NotesMaxLines,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(top = 16.dp),
			)
		}

		val message = when {
			state is UpdateState.Failed -> stringResource(R.string.lagoon_update_failed)
			needsInstallPermission -> stringResource(R.string.lagoon_update_permission)
			else -> null
		}
		if (message != null) {
			Text(message, fontSize = 14.sp, color = colors.accent, modifier = Modifier.padding(top = 16.dp))
		}

		Spacer(Modifier.height(24.dp))

		if (state is UpdateState.Downloading) {
			DownloadProgress(state.progress)
			Spacer(Modifier.height(20.dp))
		}

		Row(
			horizontalArrangement = Arrangement.spacedBy(14.dp),
			modifier = Modifier.focusGroup(),
		) {
			if (state !is UpdateState.Downloading) {
				PillButton(
					label = stringResource(if (state is UpdateState.Failed) R.string.lagoon_update_try_again else R.string.lagoon_update_now),
					icon = R.drawable.lagoon_ic_download,
					emphasis = PillEmphasis.Primary,
					onClick = onUpdate,
					modifier = Modifier.focusRequester(primaryRequester),
				)
			}
			PillButton(
				label = stringResource(R.string.lagoon_update_not_now),
				icon = R.drawable.ic_time,
				onClick = onNotNow,
				modifier = if (state is UpdateState.Downloading) Modifier.focusRequester(primaryRequester) else Modifier,
			)
		}
	}
}

@Composable
private fun DownloadProgress(progress: Float) {
	val colors = JellyfinTheme.colorScheme
	Column {
		Box(
			Modifier
				.fillMaxWidth()
				.height(6.dp)
				.background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
		) {
			Box(
				Modifier
					.fillMaxHeight()
					.fillMaxWidth(progress.coerceIn(0f, 1f))
					.background(colors.accent, RoundedCornerShape(50))
			)
		}
		Text(
			text = stringResource(R.string.lagoon_update_downloading, (progress * PercentScale).toInt()),
			fontSize = 14.sp,
			color = colors.listCaption,
			modifier = Modifier.padding(top = 8.dp),
		)
	}
}

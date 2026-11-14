package org.jellyfin.androidtv.ui.lagoon.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.getQualityProfiles
import org.jellyfin.androidtv.preference.constant.ZoomMode
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.playback.VideoSpeedController
import java.util.Locale

private const val TicksPerMilli = 10_000L
private const val MillisPerHour = 3_600_000L
private val NarrowPanel = 380.dp
private val WidePanel = 620.dp

private data class PanelOption(val key: String, val label: String, val detail: String? = null)

/**
 * The side sheet that opens from the player's buttons for tracks, speed, quality, picture size and chapters.
 */
@Composable
fun PlayerPanelSheet(
	panel: PlayerPanel,
	snapshot: PlayerSnapshot,
	state: PlayerOverlayState,
	commands: PlayerCommands,
	onChanged: () -> Unit,
) {
	Box(
		Modifier
			.fillMaxSize()
			.background(Brush.horizontalGradient(0.35f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.85f)))
	) {
		Box(
			Modifier
				.align(Alignment.CenterEnd)
				.fillMaxHeight()
				.width(if (panel == PlayerPanel.AudioAndSubtitles) WidePanel else NarrowPanel)
				.background(JellyfinTheme.colorScheme.surface.copy(alpha = 0.96f))
				.padding(horizontal = 28.dp, vertical = 40.dp)
		) {
			when (panel) {
				PlayerPanel.AudioAndSubtitles -> AudioAndSubtitles(snapshot, commands, onChanged)
				PlayerPanel.Speed -> SpeedPanel(snapshot, commands, state, onChanged)
				PlayerPanel.Quality -> QualityPanel(commands, state, onChanged)
				PlayerPanel.Zoom -> ZoomPanel(snapshot, commands, state, onChanged)
				PlayerPanel.Chapters -> ChaptersPanel(snapshot, commands, state, onChanged)
			}
		}
	}
}

@Composable
private fun AudioAndSubtitles(snapshot: PlayerSnapshot, commands: PlayerCommands, onChanged: () -> Unit) {
	val subtitlesOff = stringResource(R.string.lagoon_player_subtitles_off)
	Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
		OptionColumn(
			title = stringResource(R.string.lagoon_player_audio),
			options = snapshot.audioTracks.map { PanelOption(it.index.toString(), it.label) },
			selectedKey = snapshot.selectedAudioIndex.toString(),
			width = 270.dp,
			takesFocus = true,
			onSelect = { key ->
				commands.selectAudio(key.toInt())
				onChanged()
			},
		)
		OptionColumn(
			title = stringResource(R.string.lagoon_player_subtitles),
			options = listOf(PanelOption("-1", subtitlesOff)) + snapshot.subtitleTracks.map { PanelOption(it.index.toString(), it.label) },
			selectedKey = snapshot.selectedSubtitleIndex.toString(),
			width = 270.dp,
			takesFocus = snapshot.audioTracks.isEmpty(),
			onSelect = { key ->
				commands.selectSubtitle(key.toInt())
				onChanged()
			},
		)
	}
}

@Composable
private fun SpeedPanel(snapshot: PlayerSnapshot, commands: PlayerCommands, state: PlayerOverlayState, onChanged: () -> Unit) {
	val normal = stringResource(R.string.lagoon_player_normal_speed)
	val steps = VideoSpeedController.SpeedSteps.entries
	OptionColumn(
		title = stringResource(R.string.lagoon_player_speed),
		options = steps.map { step ->
			val label = String.format(Locale.US, "%sx", step.speed.toString().removeSuffix(".0"))
			PanelOption(step.name, label, normal.takeIf { step == VideoSpeedController.SpeedSteps.SPEED_1_00 })
		},
		selectedKey = steps.firstOrNull { it.speed == snapshot.speed }?.name,
		takesFocus = true,
		onSelect = { key ->
			commands.setSpeed(VideoSpeedController.SpeedSteps.valueOf(key))
			onChanged()
			state.closePanel()
		},
	)
}

@Composable
private fun QualityPanel(commands: PlayerCommands, state: PlayerOverlayState, onChanged: () -> Unit) {
	val context = LocalContext.current
	val profiles = remember(context) { getQualityProfiles(context) }
	OptionColumn(
		title = stringResource(R.string.lagoon_player_quality),
		options = profiles.map { (key, label) -> PanelOption(key, label) },
		selectedKey = commands.currentQuality(),
		takesFocus = true,
		onSelect = { key ->
			commands.setQuality(key)
			onChanged()
			state.closePanel()
		},
	)
}

@Composable
private fun ZoomPanel(snapshot: PlayerSnapshot, commands: PlayerCommands, state: PlayerOverlayState, onChanged: () -> Unit) {
	val context = LocalContext.current
	OptionColumn(
		title = stringResource(R.string.lagoon_player_zoom),
		options = ZoomMode.entries.map { PanelOption(it.name, context.getString(it.nameRes)) },
		selectedKey = snapshot.zoom.name,
		takesFocus = true,
		onSelect = { key ->
			commands.setZoom(ZoomMode.valueOf(key))
			onChanged()
			state.closePanel()
		},
	)
}

@Composable
private fun ChaptersPanel(snapshot: PlayerSnapshot, commands: PlayerCommands, state: PlayerOverlayState, onChanged: () -> Unit) {
	val includeHours = snapshot.durationMs >= MillisPerHour
	val currentChapter = snapshot.chapters.lastOrNull { it.startPositionTicks / TicksPerMilli <= snapshot.positionMs }
	OptionColumn(
		title = stringResource(R.string.lagoon_player_chapters),
		options = snapshot.chapters.mapIndexed { index, chapter ->
			val startMs = chapter.startPositionTicks / TicksPerMilli
			PanelOption(startMs.toString(), chapter.name ?: "#${index + 1}", PlayerTime.clock(startMs, includeHours))
		},
		selectedKey = currentChapter?.let { (it.startPositionTicks / TicksPerMilli).toString() },
		takesFocus = true,
		onSelect = { key ->
			commands.seek(key.toLong())
			onChanged()
			state.closePanel()
		},
	)
}

@Composable
private fun OptionColumn(
	title: String,
	options: List<PanelOption>,
	selectedKey: String?,
	takesFocus: Boolean,
	onSelect: (String) -> Unit,
	width: Dp = NarrowPanel,
) {
	val selectedRequester = remember { FocusRequester() }
	val firstRequester = remember { FocusRequester() }
	val hasSelected = options.any { it.key == selectedKey }

	LaunchedEffect(Unit) {
		if (!takesFocus) return@LaunchedEffect
		withFrameNanos { }
		runCatching { if (hasSelected) selectedRequester.requestFocus() else firstRequester.requestFocus() }
	}

	Column(Modifier.width(width)) {
		Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
		LazyColumn(
			verticalArrangement = Arrangement.spacedBy(4.dp),
			modifier = Modifier
				.fillMaxWidth()
				.focusRestorer()
				.focusGroup(),
		) {
			items(options, key = { it.key }) { option ->
				val requester = when {
					option.key == selectedKey -> Modifier.focusRequester(selectedRequester)
					option == options.first() -> Modifier.focusRequester(firstRequester)
					else -> Modifier
				}
				OptionRow(option, isSelected = option.key == selectedKey, onClick = { onSelect(option.key) }, modifier = requester)
			}
		}
	}
}

@Composable
private fun OptionRow(option: PanelOption, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val colors = JellyfinTheme.colorScheme
	val background by animateColorAsState(if (isFocused) colors.accent else Color.Transparent, label = "OptionBackground")
	val content = if (isFocused) colors.onAccent else Color.White

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.fillMaxWidth()
			.height(44.dp)
			.background(background, RoundedCornerShape(10.dp))
			.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
			.padding(horizontal = 12.dp),
	) {
		Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
			if (isSelected) {
				Icon(
					ImageVector.vectorResource(R.drawable.ic_check),
					contentDescription = null,
					tint = if (isFocused) colors.onAccent else colors.accent,
					modifier = Modifier.size(20.dp),
				)
			}
		}
		Text(
			text = option.label,
			fontSize = 16.sp,
			fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
			color = content,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.weight(1f)
				.padding(start = 10.dp),
		)
		if (option.detail != null) {
			Text(option.detail, fontSize = 13.sp, color = if (isFocused) colors.onAccent else colors.listCaption)
		}
	}
}

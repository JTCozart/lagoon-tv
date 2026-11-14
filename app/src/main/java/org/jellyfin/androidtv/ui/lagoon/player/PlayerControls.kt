package org.jellyfin.androidtv.ui.lagoon.player

import android.view.KeyEvent
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.lagoon.browse.CardArtwork
import org.jellyfin.androidtv.ui.lagoon.browse.HeroMetadata
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.koin.compose.koinInject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.seconds

private val SidePadding = 56.dp
private val ScrubSettleDelay = 0.8.seconds
private val ClockRefresh = 15.seconds
private const val MillisPerHour = 3_600_000L

/**
 * The controls drawn over the video: title and clock along the top, the seek bar and buttons along the bottom.
 */
@Composable
fun PlayerControls(
	state: PlayerOverlayState,
	snapshot: PlayerSnapshot,
	scrubTargetMs: Long?,
	entry: ControlsEntry,
	commands: PlayerCommands,
	now: () -> Instant,
) {
	val playRequester = remember { FocusRequester() }
	val seekRequester = remember { FocusRequester() }

	LaunchedEffect(entry) {
		withFrameNanos { }
		runCatching { if (entry == ControlsEntry.SeekBar) seekRequester.requestFocus() else playRequester.requestFocus() }
	}

	// A scrub seeks once the presses stop, so holding Left does not fire a seek for every step
	LaunchedEffect(scrubTargetMs) {
		val target = scrubTargetMs ?: return@LaunchedEffect
		delay(ScrubSettleDelay)
		commands.seek(target)
		state.scrubTo(null)
	}

	Box(Modifier.fillMaxSize()) {
		Box(
			Modifier
				.fillMaxWidth()
				.height(170.dp)
				.background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)))
		)
		Box(
			Modifier
				.align(Alignment.BottomStart)
				.fillMaxWidth()
				.height(300.dp)
				.background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))))
		)

		PlayerHeader(snapshot, now, Modifier.padding(horizontal = SidePadding, vertical = 36.dp))

		Column(
			Modifier
				.align(Alignment.BottomStart)
				.fillMaxWidth()
				.padding(horizontal = SidePadding, vertical = 34.dp)
		) {
			SeekBar(
				snapshot = snapshot,
				scrubTargetMs = scrubTargetMs,
				onScrub = { direction, repeat ->
					val from = scrubTargetMs ?: snapshot.positionMs
					state.scrubTo(ScrubStepper.move(from, direction, repeat, snapshot.durationMs))
				},
				onSelect = {
					if (scrubTargetMs != null) {
						commands.seek(scrubTargetMs)
						state.scrubTo(null)
					} else commands.playPause()
				},
				modifier = Modifier.focusRequester(seekRequester),
			)

			Spacer(Modifier.height(20.dp))

			ButtonRow(snapshot, state, commands, playRequester)
		}
	}
}

@Composable
private fun PlayerHeader(snapshot: PlayerSnapshot, now: () -> Instant, modifier: Modifier = Modifier) {
	val item = snapshot.item ?: return
	val api = koinInject<ApiClient>()
	val logoUrl = remember(item.id) { CardArtwork.logo(item)?.getUrl(api, maxHeight = 200) }
	val title = remember(item.id) { HeroMetadata.titleOf(item) }
	val subtitle = remember(item.id) { HeroMetadata.subtitleOf(item) }
	val clock by produceState(now()) {
		while (true) {
			delay(ClockRefresh)
			value = now()
		}
	}
	val locale = LocalConfiguration.current.locales[0]
	val zone = ZoneId.systemDefault()

	Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
		Column(Modifier.weight(1f)) {
			if (logoUrl != null) {
				AsyncImage(
					model = logoUrl,
					contentDescription = title,
					contentScale = ContentScale.Fit,
					alignment = Alignment.CenterStart,
					modifier = Modifier
						.heightIn(max = 64.dp)
						.widthIn(max = 320.dp),
				)
			} else {
				Text(title, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
			}
			if (subtitle != null) {
				Text(
					text = subtitle,
					fontSize = 17.sp,
					color = Color.White.copy(alpha = 0.85f),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.padding(top = 8.dp),
				)
			}
		}

		Column(horizontalAlignment = Alignment.End) {
			Text(
				text = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale).format(clock.atZone(zone)),
				fontSize = 22.sp,
				fontWeight = FontWeight.SemiBold,
				color = Color.White,
			)
			if (snapshot.durationMs > 0) {
				val endsAt = PlayerTime.endsAt(clock, zone, snapshot.durationMs - snapshot.positionMs, snapshot.speed, locale)
				Text(
					text = stringResource(R.string.lagoon_player_ends_at, endsAt),
					fontSize = 14.sp,
					color = JellyfinTheme.colorScheme.accent,
					modifier = Modifier.padding(top = 2.dp),
				)
			}
		}
	}
}

@Composable
private fun SeekBar(
	snapshot: PlayerSnapshot,
	scrubTargetMs: Long?,
	onScrub: (direction: Int, repeat: Int) -> Unit,
	onSelect: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val colors = JellyfinTheme.colorScheme
	val includeHours = snapshot.durationMs >= MillisPerHour
	val shownPosition = scrubTargetMs ?: snapshot.positionMs
	val duration = snapshot.durationMs.coerceAtLeast(1L)
	val barHeight by animateFloatAsState(if (isFocused) 8f else 5f, label = "SeekBarHeight")
	// Only a Select pressed while the bar has focus counts; a release left over from a closed panel must not pause
	var isSelectPressed by remember { mutableStateOf(false) }

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.fillMaxWidth()
			.onKeyEvent { event ->
				if (!snapshot.canSeek) return@onKeyEvent false
				val keyCode = event.key.nativeKeyCode
				when {
					event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
						onScrub(-1, event.nativeKeyEvent.repeatCount); true
					}

					event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> {
						onScrub(1, event.nativeKeyEvent.repeatCount); true
					}

					keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER -> when (event.type) {
						KeyEventType.KeyDown -> {
							isSelectPressed = true; true
						}

						KeyEventType.KeyUp -> {
							if (isSelectPressed) onSelect()
							isSelectPressed = false
							true
						}

						else -> false
					}

					else -> false
				}
			}
			.focusable(interactionSource = interactionSource),
	) {
		Text(
			text = PlayerTime.clock(shownPosition, includeHours),
			fontSize = 15.sp,
			fontWeight = FontWeight.SemiBold,
			color = if (scrubTargetMs != null) colors.accent else Color.White,
			modifier = Modifier.width(if (includeHours) 76.dp else 56.dp),
		)

		BoxWithConstraints(
			contentAlignment = Alignment.CenterStart,
			modifier = Modifier
				.weight(1f)
				.height(24.dp)
				.padding(horizontal = 14.dp),
		) {
			val playedFraction = (snapshot.positionMs.toFloat() / duration).coerceIn(0f, 1f)
			val bufferedFraction = (snapshot.bufferedMs.toFloat() / duration).coerceIn(0f, 1f)
			val targetFraction = (shownPosition.toFloat() / duration).coerceIn(0f, 1f)

			Box(
				Modifier
					.fillMaxWidth()
					.height(barHeight.dp)
					.background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
			)
			Box(
				Modifier
					.fillMaxWidth(bufferedFraction)
					.height(barHeight.dp)
					.background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
			)
			Box(
				Modifier
					.fillMaxWidth(if (scrubTargetMs != null) targetFraction else playedFraction)
					.height(barHeight.dp)
					.background(colors.accent, RoundedCornerShape(50))
			)
			if (isFocused) {
				Box(
					Modifier
						.offset(x = maxWidth * targetFraction - 9.dp)
						.size(18.dp)
						.background(Color.White, CircleShape)
				)
			}
		}

		Text(
			text = PlayerTime.remaining(shownPosition, snapshot.durationMs),
			fontSize = 15.sp,
			color = Color.White.copy(alpha = 0.85f),
			modifier = Modifier.width(if (includeHours) 84.dp else 62.dp),
		)
	}
}

@Composable
private fun ButtonRow(
	snapshot: PlayerSnapshot,
	state: PlayerOverlayState,
	commands: PlayerCommands,
	playRequester: FocusRequester,
) {
	var focusedLabel by remember { mutableStateOf<String?>(null) }

	Column {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier
				.fillMaxWidth()
				.focusRestorer(playRequester)
				.focusGroup(),
		) {
			if (snapshot.hasPrevious) {
				ControlButton(R.drawable.ic_previous_episode, stringResource(R.string.lagoon_player_previous), commands::previous, { focusedLabel = it })
			}
			if (snapshot.canSeek) {
				ControlButton(R.drawable.ic_rewind, stringResource(R.string.lagoon_player_rewind), commands::rewind, { focusedLabel = it })
			}
			ControlButton(
				icon = if (snapshot.isPaused) R.drawable.ic_play else R.drawable.ic_pause,
				label = stringResource(if (snapshot.isPaused) R.string.lagoon_player_play else R.string.lagoon_player_pause),
				onClick = commands::playPause,
				onFocused = { focusedLabel = it },
				isPrimary = true,
				modifier = Modifier.focusRequester(playRequester),
			)
			if (snapshot.canSeek) {
				ControlButton(R.drawable.ic_fast_forward, stringResource(R.string.lagoon_player_fast_forward), commands::fastForward, { focusedLabel = it })
			}
			if (snapshot.hasNext) {
				ControlButton(R.drawable.ic_next, stringResource(R.string.lagoon_player_next), commands::next, { focusedLabel = it })
			}

			Spacer(Modifier.weight(1f))

			if (snapshot.audioTracks.size > 1 || snapshot.subtitleTracks.isNotEmpty()) {
				ControlButton(R.drawable.ic_subtitles, stringResource(R.string.lagoon_player_audio_subtitles), { state.openPanel(PlayerPanel.AudioAndSubtitles) }, { focusedLabel = it })
			}
			if (snapshot.chapters.isNotEmpty()) {
				ControlButton(R.drawable.ic_select_chapter, stringResource(R.string.lagoon_player_chapters), { state.openPanel(PlayerPanel.Chapters) }, { focusedLabel = it })
			}
			if (snapshot.canSeek) {
				ControlButton(R.drawable.ic_playback_speed, stringResource(R.string.lagoon_player_speed), { state.openPanel(PlayerPanel.Speed) }, { focusedLabel = it })
			}
			ControlButton(R.drawable.ic_select_quality, stringResource(R.string.lagoon_player_quality), { state.openPanel(PlayerPanel.Quality) }, { focusedLabel = it })
			ControlButton(R.drawable.ic_aspect_ratio, stringResource(R.string.lagoon_player_zoom), { state.openPanel(PlayerPanel.Zoom) }, { focusedLabel = it })
		}

		Text(
			text = focusedLabel.orEmpty(),
			fontSize = 14.sp,
			color = Color.White.copy(alpha = 0.85f),
			modifier = Modifier
				.padding(top = 10.dp)
				.height(20.dp),
		)
	}
}

@Composable
private fun ControlButton(
	@DrawableRes icon: Int,
	label: String,
	onClick: () -> Unit,
	onFocused: (String?) -> Unit,
	modifier: Modifier = Modifier,
	isPrimary: Boolean = false,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val colors = JellyfinTheme.colorScheme
	val background by animateColorAsState(
		when {
			isFocused -> colors.accent
			isPrimary -> Color.White.copy(alpha = 0.22f)
			else -> Color.Transparent
		},
		label = "ControlBackground",
	)
	val scale by animateFloatAsState(if (isFocused) 1.1f else 1f, label = "ControlScale")
	val size = if (isPrimary) 60.dp else 48.dp

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.onFocusChanged { if (it.isFocused) onFocused(label) }
			.graphicsLayer {
				scaleX = scale
				scaleY = scale
			}
			.size(size)
			.background(background, CircleShape)
			.clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
	) {
		Icon(
			imageVector = ImageVector.vectorResource(icon),
			contentDescription = label,
			tint = if (isFocused) colors.onAccent else Color.White,
			modifier = Modifier.size(if (isPrimary) 30.dp else 24.dp),
		)
	}
}

/** Shared fade for the controls and panels. */
@Composable
fun FadingLayer(isVisible: Boolean, content: @Composable () -> Unit) {
	AnimatedVisibility(visible = isVisible, enter = fadeIn(), exit = fadeOut()) {
		Box(Modifier.fillMaxSize()) { content() }
	}
}

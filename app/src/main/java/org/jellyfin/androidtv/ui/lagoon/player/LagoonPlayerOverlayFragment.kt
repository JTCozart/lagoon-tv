package org.jellyfin.androidtv.ui.lagoon.player

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.playback.CustomPlaybackOverlayFragment
import org.jellyfin.androidtv.ui.playback.PlaybackControllerContainer
import org.koin.android.ext.android.inject
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val AutoHideDelay = 5.seconds
private val SettleBeforeRefresh = 400.milliseconds

/**
 * Lagoon's player controls, standing in for upstream's leanback overlay over the same playback engine.
 */
class LagoonPlayerOverlayFragment : Fragment(), PlayerControlsOverlay {
	private val playbackControllerContainer by inject<PlaybackControllerContainer>()
	private val userPreferences by inject<UserPreferences>()

	private val state = PlayerOverlayState()
	private val commands by lazy { UpstreamPlayerCommands({ playbackControllerContainer.playbackController }, userPreferences) }

	private var host: CustomPlaybackOverlayFragment? = null
	private var keyInterceptListener: View.OnKeyListener? = null
	private var shouldShowOverlay = true
	private var keyLayout: KeyInterceptLayout? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
		KeyInterceptLayout(requireContext(), ::handleKey).apply {
			// Holds focus itself while the controls are hidden, so the remote's keys keep arriving
			isFocusable = true
			isFocusableInTouchMode = true
			descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
			addView(ComposeView(context).apply { setContent { Overlay() } })
			keyLayout = this
		}

	override fun onResume() {
		super.onResume()
		if (!state.isVisible.value) keyLayout?.requestFocus()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		keyLayout = null
	}

	@Composable
	private fun Overlay() {
		val isVisible by state.isVisible.collectAsState()
		val snapshot by state.snapshot.collectAsState()
		val panel by state.panel.collectAsState()
		val scrubTarget by state.scrubTargetMs.collectAsState()
		val entry by state.entry.collectAsState()
		val interaction by state.interaction.collectAsState()

		LaunchedEffect(isVisible, snapshot.isPaused, panel, scrubTarget, interaction) {
			if (!isVisible || snapshot.isPaused || panel != null || scrubTarget != null) return@LaunchedEffect
			delay(AutoHideDelay)
			hideControls()
		}

		Box(Modifier.fillMaxSize()) {
			FadingLayer(isVisible && panel == null) {
				PlayerControls(state, snapshot, scrubTarget, entry, commands, now = Instant::now)
			}
			FadingLayer(isVisible && panel != null) {
				panel?.let { PlayerPanelSheet(it, snapshot, state, commands, onChanged = ::refreshSoon) }
			}
		}
	}

	private fun handleKey(event: KeyEvent): Boolean {
		val keyCode = event.keyCode
		val isDown = event.action == KeyEvent.ACTION_DOWN

		// The controller turns the overlay off when playback starts and upstream's key listener turns it back on
		// with any key press. Lagoon's routing runs before that listener, so it applies the same rule first.
		if (isDown) shouldShowOverlay = true

		when (val route = PlayerKeyRouter.beforePlayer(keyCode, keyContext())) {
			KeyRoute.ClosePanel -> if (isDown) state.closePanel()
			KeyRoute.HideControls -> if (isDown) hideControls()
			is KeyRoute.Scrub -> if (isDown) scrub(route.direction, event.repeatCount)
			else -> return askPlayerThenControls(keyCode, event, isDown)
		}
		return true
	}

	private fun askPlayerThenControls(keyCode: Int, event: KeyEvent, isDown: Boolean): Boolean {
		if (keyInterceptListener?.onKey(keyLayout, keyCode, event) == true) {
			refresh()
			return true
		}

		if (PlayerKeyRouter.afterPlayerDeclined(keyCode, keyContext()) == KeyRoute.ShowControls) {
			if (isDown) showControls(ControlsEntry.PlayButton)
			return true
		}

		if (state.isVisible.value) state.touch()
		return false
	}

	private fun keyContext() = OverlayKeyContext(
		isVisible = state.isVisible.value,
		isPanelOpen = state.panel.value != null,
		isScrubbing = state.scrubTargetMs.value != null,
		canSeek = state.snapshot.value.canSeek,
		canShowControls = shouldShowOverlay,
	)

	private fun scrub(direction: Int, repeat: Int) {
		if (!state.isVisible.value) showControls(ControlsEntry.SeekBar)
		val snapshot = state.snapshot.value
		val from = state.scrubTargetMs.value ?: snapshot.positionMs
		state.scrubTo(ScrubStepper.move(from, direction, repeat, snapshot.durationMs))
	}

	private fun showControls(entry: ControlsEntry) {
		if (!shouldShowOverlay) return
		keyLayout?.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
		refresh()
		state.show(entry)
		host?.setFadingEnabled(false)
		host?.show()
	}

	private fun hideControls() {
		state.hide()
		host?.hide()
		// Compose drops focus once the faded-out buttons leave, which would leave no view focused and the remote
		// silent. Blocking the children keeps focus on this layout instead.
		keyLayout?.apply {
			descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
			requestFocus()
		}
	}

	private fun refresh() {
		val controller = playbackControllerContainer.playbackController ?: return
		val snapshot = PlayerSnapshot.read(controller)
		val wasPaused = state.snapshot.value.isPaused
		state.publish(snapshot)

		if (ControlsOnPause.shouldShow(wasPaused, snapshot.isPaused, state.isVisible.value)) showControls(ControlsEntry.PlayButton)
	}

	private fun refreshSoon() {
		viewLifecycleOwner.lifecycleScope.launch {
			delay(SettleBeforeRefresh)
			refresh()
		}
	}

	override fun initFromView(host: CustomPlaybackOverlayFragment) {
		this.host = host
	}

	override fun mediaInfoChanged() = refresh()
	override fun updatePlayState() = refresh()
	override fun updateCurrentPosition() = refresh()
	override fun recordingStateChanged() = Unit
	override fun onFullyInitialized() = refresh()
	override fun hideOverlay() = hideControls()
	override fun isControlsOverlayVisible(): Boolean = state.isVisible.value

	override fun setOnKeyInterceptListener(listener: View.OnKeyListener?) {
		keyInterceptListener = listener
	}

	override fun setShouldShowOverlay(shouldShowOverlay: Boolean) {
		this.shouldShowOverlay = shouldShowOverlay
	}

	// The controls run their own auto-hide timer, and the seek bar handles Left and Right itself
	override fun setFading(fadingEnabled: Boolean) = Unit
	override fun onSeekKey() = Unit
}

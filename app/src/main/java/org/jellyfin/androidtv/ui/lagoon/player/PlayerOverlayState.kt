package org.jellyfin.androidtv.ui.lagoon.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The overlay's own state: whether the controls are up, which panel is open and where a scrub is heading.
 * The video's state lives in [PlayerSnapshot].
 */
class PlayerOverlayState {
	private val _snapshot = MutableStateFlow(PlayerSnapshot())
	val snapshot: StateFlow<PlayerSnapshot> = _snapshot.asStateFlow()

	private val _isVisible = MutableStateFlow(false)
	val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

	private val _entry = MutableStateFlow(ControlsEntry.PlayButton)
	val entry: StateFlow<ControlsEntry> = _entry.asStateFlow()

	private val _panel = MutableStateFlow<PlayerPanel?>(null)
	val panel: StateFlow<PlayerPanel?> = _panel.asStateFlow()

	/** Where a scrub with Left and Right will seek to when it settles; null when not scrubbing. */
	private val _scrubTargetMs = MutableStateFlow<Long?>(null)
	val scrubTargetMs: StateFlow<Long?> = _scrubTargetMs.asStateFlow()

	/** Bumped on every key press so the auto-hide countdown starts over. */
	private val _interaction = MutableStateFlow(0)
	val interaction: StateFlow<Int> = _interaction.asStateFlow()

	fun publish(snapshot: PlayerSnapshot) {
		_snapshot.value = snapshot
	}

	fun show(entry: ControlsEntry) {
		_entry.value = entry
		_isVisible.value = true
		touch()
	}

	fun hide() {
		_isVisible.value = false
		_panel.value = null
		_scrubTargetMs.value = null
	}

	fun openPanel(panel: PlayerPanel) {
		_panel.value = panel
		touch()
	}

	fun closePanel() {
		_panel.value = null
		// Back to the buttons, where the panel was opened from
		_entry.value = ControlsEntry.PlayButton
		touch()
	}

	fun scrubTo(targetMs: Long?) {
		_scrubTargetMs.value = targetMs
		touch()
	}

	fun touch() = _interaction.update { it + 1 }
}

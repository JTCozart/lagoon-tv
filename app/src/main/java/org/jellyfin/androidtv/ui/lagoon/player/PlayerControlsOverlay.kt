package org.jellyfin.androidtv.ui.lagoon.player

import android.view.View
import org.jellyfin.androidtv.ui.playback.CustomPlaybackOverlayFragment

/**
 * What the video player fragment needs from the controls drawn over the video. Implemented by upstream's leanback
 * overlay (kept for Live TV) and by Lagoon's own overlay, so the player can use either without knowing which.
 */
interface PlayerControlsOverlay {
	fun initFromView(host: CustomPlaybackOverlayFragment)
	fun mediaInfoChanged()
	fun setOnKeyInterceptListener(listener: View.OnKeyListener?)
	fun updatePlayState()
	fun updateCurrentPosition()
	fun hideOverlay()
	fun setShouldShowOverlay(shouldShowOverlay: Boolean)
	fun isControlsOverlayVisible(): Boolean
	fun recordingStateChanged()
	fun onFullyInitialized()
	fun setFading(fadingEnabled: Boolean)

	/** Left or Right was pressed while the player handled the key itself. */
	fun onSeekKey()
}

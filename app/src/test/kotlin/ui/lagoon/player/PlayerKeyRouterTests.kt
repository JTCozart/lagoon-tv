package org.jellyfin.androidtv.ui.lagoon.player

import android.view.KeyEvent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private fun context(
	isVisible: Boolean = false,
	isPanelOpen: Boolean = false,
	isScrubbing: Boolean = false,
	canSeek: Boolean = true,
	canShowControls: Boolean = true,
) = OverlayKeyContext(isVisible, isPanelOpen, isScrubbing, canSeek, canShowControls)

class PlayerKeyRouterTests : FunSpec({
	test("Back with a panel open closes the panel") {
		PlayerKeyRouter.beforePlayer(KeyEvent.KEYCODE_BACK, context(isVisible = true, isPanelOpen = true)) shouldBe KeyRoute.ClosePanel
	}

	test("Back with the controls up hides them instead of leaving the player") {
		PlayerKeyRouter.beforePlayer(KeyEvent.KEYCODE_BACK, context(isVisible = true)) shouldBe KeyRoute.HideControls
	}

	test("Back with the controls hidden goes to the player, which leaves playback") {
		PlayerKeyRouter.beforePlayer(KeyEvent.KEYCODE_BACK, context()) shouldBe KeyRoute.AskPlayer
	}

	test("Left and Right with the controls hidden start a scrub in that direction") {
		PlayerKeyRouter.beforePlayer(KeyEvent.KEYCODE_DPAD_RIGHT, context()) shouldBe KeyRoute.Scrub(1)
		PlayerKeyRouter.beforePlayer(KeyEvent.KEYCODE_DPAD_LEFT, context()) shouldBe KeyRoute.Scrub(-1)
	}

	test("Further presses keep scrubbing while the controls are still taking focus") {
		PlayerKeyRouter.beforePlayer(KeyEvent.KEYCODE_DPAD_RIGHT, context(isVisible = true, isScrubbing = true)) shouldBe KeyRoute.Scrub(1)
	}

	test("Left and Right move between buttons when the controls are up and nothing is scrubbing") {
		PlayerKeyRouter.beforePlayer(KeyEvent.KEYCODE_DPAD_RIGHT, context(isVisible = true)) shouldBe KeyRoute.AskPlayer
	}

	test("Live TV and other unseekable streams never scrub") {
		PlayerKeyRouter.beforePlayer(KeyEvent.KEYCODE_DPAD_RIGHT, context(canSeek = false)) shouldBe KeyRoute.AskPlayer
	}

	test("Nothing scrubs while the player has said not to show the controls") {
		PlayerKeyRouter.beforePlayer(KeyEvent.KEYCODE_DPAD_RIGHT, context(canShowControls = false)) shouldBe KeyRoute.AskPlayer
	}

	test("A panel takes Left and Right for itself even mid scrub") {
		PlayerKeyRouter.beforePlayer(
			KeyEvent.KEYCODE_DPAD_RIGHT,
			context(isVisible = true, isScrubbing = true, isPanelOpen = true),
		) shouldBe KeyRoute.AskPlayer
	}

	test("Up, Down, Select and Menu bring the controls up when the player has not used the key") {
		listOf(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_MENU).forEach { key ->
			PlayerKeyRouter.afterPlayerDeclined(key, context()) shouldBe KeyRoute.ShowControls
		}
	}

	test("Once the controls are up, keys go to the buttons") {
		PlayerKeyRouter.afterPlayerDeclined(KeyEvent.KEYCODE_DPAD_DOWN, context(isVisible = true)) shouldBe KeyRoute.PassToControls
	}

	test("Other keys while hidden are left alone") {
		PlayerKeyRouter.afterPlayerDeclined(KeyEvent.KEYCODE_VOLUME_UP, context()) shouldBe KeyRoute.PassToControls
	}
})

class ControlsOnPauseTests : FunSpec({
	test("Pausing while the controls are hidden brings them up") {
		ControlsOnPause.shouldShow(wasPaused = false, isPaused = true, isVisible = false) shouldBe true
	}

	test("Staying paused does not reopen controls the viewer just hid") {
		ControlsOnPause.shouldShow(wasPaused = true, isPaused = true, isVisible = false) shouldBe false
	}

	test("Resuming leaves the controls as they are") {
		ControlsOnPause.shouldShow(wasPaused = true, isPaused = false, isVisible = false) shouldBe false
	}
})

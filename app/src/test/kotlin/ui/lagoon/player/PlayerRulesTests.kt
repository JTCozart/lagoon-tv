package org.jellyfin.androidtv.ui.lagoon.player

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

private const val Second = 1_000L
private const val Minute = 60 * Second
private const val Hour = 60 * Minute

class PlayerTimeTests : FunSpec({
	test("Positions under an hour show minutes and seconds") {
		PlayerTime.clock(83 * Second, includeHours = false) shouldBe "1:23"
	}

	test("When the title runs an hour or more every position shows hours, so the width never jumps") {
		PlayerTime.clock(83 * Second, includeHours = true) shouldBe "0:01:23"
		PlayerTime.clock(Hour + 2 * Minute + 5 * Second, includeHours = true) shouldBe "1:02:05"
	}

	test("Remaining time is shown with a leading minus") {
		PlayerTime.remaining(positionMs = 10 * Minute, durationMs = 52 * Minute) shouldBe "-42:00"
	}

	test("Remaining time never goes below zero") {
		PlayerTime.remaining(positionMs = 60 * Minute, durationMs = 52 * Minute) shouldBe "-0:00"
	}

	test("The end time is now plus what is left") {
		val now = Instant.parse("2026-11-13T21:00:00Z")

		PlayerTime.endsAt(now, ZoneOffset.UTC, remainingMs = 42 * Minute, speed = 1f, locale = Locale.US) shouldBe "9:42 PM"
	}

	test("Faster playback brings the end time forward") {
		val now = Instant.parse("2026-11-13T21:00:00Z")

		PlayerTime.endsAt(now, ZoneId.of("UTC"), remainingMs = 60 * Minute, speed = 2f, locale = Locale.US) shouldBe "9:30 PM"
	}
})

class ScrubStepperTests : FunSpec({
	test("The first presses move ten seconds") {
		ScrubStepper.stepFor(repeat = 0) shouldBe 10 * Second
		ScrubStepper.stepFor(repeat = 2) shouldBe 10 * Second
	}

	test("Holding the button speeds the scrub up") {
		ScrubStepper.stepFor(repeat = 6) shouldBe 30 * Second
		ScrubStepper.stepFor(repeat = 20) shouldBe 2 * Minute
	}

	test("Scrubbing stays inside the title") {
		ScrubStepper.move(from = 5 * Second, direction = -1, repeat = 0, durationMs = Hour) shouldBe 0L
		ScrubStepper.move(from = Hour - 5 * Second, direction = 1, repeat = 0, durationMs = Hour) shouldBe Hour
	}

	test("A forward step adds the step size") {
		ScrubStepper.move(from = Minute, direction = 1, repeat = 0, durationMs = Hour) shouldBe Minute + 10 * Second
	}
})

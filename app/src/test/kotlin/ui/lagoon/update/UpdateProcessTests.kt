package org.jellyfin.androidtv.ui.lagoon.update

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

private val now = Instant.parse("2026-11-14T20:00:00Z")
private val fixedClock = Clock.fixed(now, ZoneOffset.UTC)
private val folder = File("updates")

private fun release(version: String) = ReleaseInfo(
	version = AppVersion.parse(version)!!,
	apkUrl = "https://example.com/lagoon-tv.apk",
	apkSizeBytes = 100,
	checksumsUrl = "https://example.com/SHA256SUMS",
	notes = "",
	pageUrl = "",
)

private class FakeUpdateSource(
	var latest: ReleaseInfo? = null,
	var failDownload: Boolean = false,
) : UpdateSource {
	var checks = 0

	override suspend fun latestRelease(): ReleaseInfo? {
		checks++
		return latest
	}

	override suspend fun downloadApk(release: ReleaseInfo, target: File, onProgress: (Float) -> Unit) {
		if (failDownload) throw UpdateDownloadException("Checksum mismatch")
		onProgress(0.5f)
		onProgress(1f)
	}
}

private class FakeUpdatePreferences(
	override var lastCheck: Instant? = null,
	override var dismissedVersion: AppVersion? = null,
) : UpdatePreferences

private fun process(
	source: UpdateSource,
	preferences: UpdatePreferences = FakeUpdatePreferences(),
	installed: String = "1.0.0",
) = UpdateProcess(source, preferences, fixedClock, AppVersion.parse(installed), folder, CoroutineScope(Dispatchers.Unconfined))

class UpdateProcessTests : FunSpec({
	test("A newer release is offered after a check") {
		val update = process(FakeUpdateSource(latest = release("1.1.0")))

		update.checkIfDue()

		update.state.value shouldBe UpdateState.Offered(release("1.1.0"))
	}

	test("A check records when it happened") {
		val preferences = FakeUpdatePreferences()

		process(FakeUpdateSource(latest = release("1.1.0")), preferences).checkIfDue()

		preferences.lastCheck shouldBe now
	}

	test("No check is made when the last one was recent") {
		val source = FakeUpdateSource(latest = release("1.1.0"))

		process(source, FakeUpdatePreferences(lastCheck = now.minus(Duration.ofHours(1)))).checkIfDue()

		source.checks shouldBe 0
	}

	test("A build from source never checks") {
		val source = FakeUpdateSource(latest = release("1.1.0"))

		process(source, installed = "0.0.0-dev.1").checkIfDue()

		source.checks shouldBe 0
	}

	test("Not now hides the offer and remembers the version") {
		val preferences = FakeUpdatePreferences()
		val update = process(FakeUpdateSource(latest = release("1.1.0")), preferences)
		update.checkIfDue()

		update.notNow()

		update.state.value shouldBe UpdateState.Hidden
		preferences.dismissedVersion shouldBe AppVersion.parse("1.1.0")
	}

	test("A finished download is ready to install") {
		val update = process(FakeUpdateSource(latest = release("1.1.0")))
		update.checkIfDue()

		update.startUpdate()

		update.state.value.shouldBeInstanceOf<UpdateState.ReadyToInstall>()
	}

	test("A download that fails or does not match its checksum is reported, not installed") {
		val update = process(FakeUpdateSource(latest = release("1.1.0"), failDownload = true))
		update.checkIfDue()

		update.startUpdate()

		update.state.value shouldBe UpdateState.Failed(release("1.1.0"))
	}

	test("Backing out of the installer brings the offer back") {
		val update = process(FakeUpdateSource(latest = release("1.1.0")))
		update.checkIfDue()
		update.startUpdate()

		update.installerOpened()

		update.state.value shouldBe UpdateState.Offered(release("1.1.0"))
	}
})

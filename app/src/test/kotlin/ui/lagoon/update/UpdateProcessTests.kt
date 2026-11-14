package org.jellyfin.androidtv.ui.lagoon.update

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File

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
	var lookup: ReleaseLookup = ReleaseLookup.NoRelease,
	var failDownload: Boolean = false,
) : UpdateSource {
	var checks = 0

	override suspend fun latestRelease(): ReleaseLookup {
		checks++
		return lookup
	}

	override suspend fun downloadApk(release: ReleaseInfo, target: File, onProgress: (Float) -> Unit) {
		if (failDownload) throw UpdateDownloadException("Checksum mismatch")
		onProgress(0.5f)
		onProgress(1f)
	}
}

private fun found(version: String) = ReleaseLookup.Found(release(version))

private fun process(source: UpdateSource, installed: String = "1.0.0") =
	UpdateProcess(source, AppVersion.parse(installed), folder, CoroutineScope(Dispatchers.Unconfined))

class UpdateProcessTests : FunSpec({
	test("A newer release is offered when the app starts") {
		val update = process(FakeUpdateSource(found("1.1.0")))

		update.checkOnLaunch()

		update.state.value shouldBe UpdateState.Offered(release("1.1.0"))
	}

	test("Each launch checks once, however often the home screen comes back") {
		val source = FakeUpdateSource(found("1.0.0"))
		val update = process(source)

		update.checkOnLaunch()
		update.checkOnLaunch()

		source.checks shouldBe 1
	}

	test("A build from source never checks on launch") {
		val source = FakeUpdateSource(found("1.1.0"))

		process(source, installed = "0.0.0-dev.1").checkOnLaunch()

		source.checks shouldBe 0
	}

	test("Not now hides the offer for this launch only") {
		val source = FakeUpdateSource(found("1.1.0"))
		val update = process(source)
		update.checkOnLaunch()

		update.notNow()

		update.state.value shouldBe UpdateState.Hidden
		val nextLaunch = process(source)
		nextLaunch.checkOnLaunch()
		nextLaunch.state.value shouldBe UpdateState.Offered(release("1.1.0"))
	}

	test("A manual check offers a newer release even after Not now") {
		val update = process(FakeUpdateSource(found("1.1.0")))
		update.checkOnLaunch()
		update.notNow()

		update.checkNow()

		update.state.value shouldBe UpdateState.Offered(release("1.1.0"))
	}

	test("A manual check says when the installed version is the newest") {
		val update = process(FakeUpdateSource(found("1.0.0")))

		update.checkNow()

		update.state.value shouldBe UpdateState.UpToDate
	}

	test("A manual check with no published release also reads as up to date") {
		val update = process(FakeUpdateSource(ReleaseLookup.NoRelease))

		update.checkNow()

		update.state.value shouldBe UpdateState.UpToDate
	}

	test("A manual check that cannot reach GitHub says so") {
		val update = process(FakeUpdateSource(ReleaseLookup.Unreachable))

		update.checkNow()

		update.state.value shouldBe UpdateState.CheckFailed
	}

	test("A manual check works on a build from source, so the update dialog can be tried") {
		val update = process(FakeUpdateSource(found("1.1.0")), installed = "0.0.0-dev.1")

		update.checkNow()

		update.state.value shouldBe UpdateState.Offered(release("1.1.0"))
	}

	test("A finished download is ready to install") {
		val update = process(FakeUpdateSource(found("1.1.0")))
		update.checkOnLaunch()

		update.startUpdate()

		update.state.value.shouldBeInstanceOf<UpdateState.ReadyToInstall>()
	}

	test("A download that fails or does not match its checksum is reported, not installed") {
		val update = process(FakeUpdateSource(found("1.1.0"), failDownload = true))
		update.checkOnLaunch()

		update.startUpdate()

		update.state.value shouldBe UpdateState.Failed(release("1.1.0"))
	}

	test("Backing out of the installer brings the offer back") {
		val update = process(FakeUpdateSource(found("1.1.0")))
		update.checkOnLaunch()
		update.startUpdate()

		update.installerOpened()

		update.state.value shouldBe UpdateState.Offered(release("1.1.0"))
	}
})

package org.jellyfin.androidtv.ui.lagoon.update

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AppVersionTests : FunSpec({
	test("A leading v is ignored") {
		AppVersion.parse("v1.2.3") shouldBe AppVersion.parse("1.2.3")
	}

	test("Versions compare part by part, not as text") {
		(AppVersion.parse("1.10.0")!! > AppVersion.parse("1.9.4")!!) shouldBe true
	}

	test("A release is newer than its own release candidates") {
		(AppVersion.parse("1.1.0")!! > AppVersion.parse("1.1.0-rc.2")!!) shouldBe true
		(AppVersion.parse("1.1.0-rc.2")!! > AppVersion.parse("1.1.0-rc.1")!!) shouldBe true
	}

	test("Builds made from source are development builds") {
		AppVersion.parse("0.0.0-dev.1")!!.isDevelopment shouldBe true
		AppVersion.parse("1.0.0")!!.isDevelopment shouldBe false
	}

	test("Text that is not a version is rejected") {
		AppVersion.parse("latest") shouldBe null
	}
})

class ReleaseParserTests : FunSpec({
	val json = """
		{
		  "tag_name": "v1.1.0",
		  "html_url": "https://github.com/JTCozart/lagoon-tv/releases/tag/v1.1.0",
		  "body": "Adds an update check.",
		  "prerelease": false,
		  "assets": [
		    { "name": "lagoon-tv-v1.1.0.apk", "browser_download_url": "https://example.com/versioned.apk", "size": 10 },
		    { "name": "lagoon-tv.apk", "browser_download_url": "https://example.com/lagoon-tv.apk", "size": 52000000 },
		    { "name": "SHA256SUMS", "browser_download_url": "https://example.com/SHA256SUMS", "size": 200 }
		  ]
		}
	""".trimIndent()

	test("The fixed-name APK, its size, the checksum file and the notes are read from a release") {
		ReleaseParser.parse(json) shouldBe ReleaseInfo(
			version = AppVersion.parse("1.1.0")!!,
			apkUrl = "https://example.com/lagoon-tv.apk",
			apkSizeBytes = 52_000_000,
			checksumsUrl = "https://example.com/SHA256SUMS",
			notes = "Adds an update check.",
			pageUrl = "https://github.com/JTCozart/lagoon-tv/releases/tag/v1.1.0",
		)
	}

	test("A release without the APK offers nothing") {
		ReleaseParser.parse("""{ "tag_name": "v1.1.0", "html_url": "x", "assets": [] }""") shouldBe null
	}

	test("A response that is not a release offers nothing") {
		ReleaseParser.parse("""{ "message": "API rate limit exceeded" }""") shouldBe null
	}
})

class ChecksumListTests : FunSpec({
	test("The checksum for a file is found by name") {
		val sums = """
			aaaa1111  lagoon-tv-v1.1.0.apk
			BBBB2222  lagoon-tv.apk
		""".trimIndent()

		ChecksumList.parse(sums)["lagoon-tv.apk"] shouldBe "bbbb2222"
	}

	test("Binary mode markers and blank lines are tolerated") {
		ChecksumList.parse("cccc3333 *lagoon-tv.apk\n\n")["lagoon-tv.apk"] shouldBe "cccc3333"
	}
})

class UpdateScheduleTests : FunSpec({
	val installed = AppVersion.parse("1.0.0")!!

	test("Installed builds check on launch") {
		UpdateSchedule.checksOnLaunch(installed) shouldBe true
	}

	test("Development builds never check on launch, so a build from source is not replaced") {
		UpdateSchedule.checksOnLaunch(AppVersion.parse("0.0.0-dev.1")!!) shouldBe false
	}

	test("A newer release is offered") {
		UpdateSchedule.shouldOffer(latest = AppVersion.parse("1.1.0")!!, installed) shouldBe true
	}

	test("The same or an older release is not offered") {
		UpdateSchedule.shouldOffer(latest = installed, installed) shouldBe false
		UpdateSchedule.shouldOffer(latest = AppVersion.parse("0.9.0")!!, installed) shouldBe false
	}
})

package org.jellyfin.androidtv.ui.base

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.shouldBe
import org.jellyfin.androidtv.preference.constant.AppTheme

class AccentPalettesTests : FunSpec({
	test("The default theme keeps the Lagoon teal") {
		AccentPalettes.forTheme(AppTheme.DARK) shouldBe AccentPalettes.Lagoon
	}

	test("Every theme has its own accent") {
		AppTheme.entries.map { AccentPalettes.forTheme(it).accent }.shouldBeUnique()
	}

	test("Focused controls and badges take the theme's accent") {
		val scheme = colorScheme(AccentPalettes.Emerald)

		scheme.accent shouldBe AccentPalettes.Emerald.accent
		scheme.buttonFocused shouldBe AccentPalettes.Emerald.accent
		scheme.inputFocused shouldBe AccentPalettes.Emerald.accent
		scheme.rangeControlFill shouldBe AccentPalettes.Emerald.accent
		scheme.listOverline shouldBe AccentPalettes.Emerald.accent
		scheme.badge shouldBe AccentPalettes.Emerald.accentDeep
	}
})

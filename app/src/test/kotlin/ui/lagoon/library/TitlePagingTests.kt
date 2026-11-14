package org.jellyfin.androidtv.ui.lagoon.library

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TitlePagingTests : FunSpec({
	test("Focus near the end of the loaded titles fetches the next page") {
		TitlePaging.shouldLoadMore(focusedIndex = 40, loadedCount = 60, totalCount = 417, isLoading = false) shouldBe true
	}

	test("Focus well before the end fetches nothing yet") {
		TitlePaging.shouldLoadMore(focusedIndex = 10, loadedCount = 60, totalCount = 417, isLoading = false) shouldBe false
	}

	test("Nothing more is fetched once every title is loaded") {
		TitlePaging.shouldLoadMore(focusedIndex = 416, loadedCount = 417, totalCount = 417, isLoading = false) shouldBe false
	}

	test("A page already on its way is not requested twice") {
		TitlePaging.shouldLoadMore(focusedIndex = 59, loadedCount = 60, totalCount = 417, isLoading = true) shouldBe false
	}
})

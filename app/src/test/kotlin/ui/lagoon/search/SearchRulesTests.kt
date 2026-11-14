package org.jellyfin.androidtv.ui.lagoon.search

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

private fun title(name: String) = BaseItemDto(id = UUID.randomUUID(), type = BaseItemKind.MOVIE, name = name)

class QueryEditorTests : FunSpec({
	test("Typing appends a character") {
		QueryEditor.type("ali", 'e') shouldBe "alie"
	}

	test("A space at the very start is ignored") {
		QueryEditor.type("", ' ') shouldBe ""
	}

	test("Two spaces in a row become one") {
		QueryEditor.type("star ", ' ') shouldBe "star "
	}

	test("The query stops growing at its maximum length") {
		val full = "a".repeat(QueryEditor.MaximumLength)

		QueryEditor.type(full, 'b') shouldBe full
	}

	test("Delete removes the last character and does nothing on an empty query") {
		QueryEditor.deleteLast("alien") shouldBe "alie"
		QueryEditor.deleteLast("") shouldBe ""
	}
})

class SearchRankerTests : FunSpec({
	test("An exact match comes first, then titles starting with the query, then a later word, then anywhere") {
		val anywhere = title("Xenomorphalien")
		val laterWord = title("Predator: Alien Hunt")
		val prefix = title("Aliens")
		val exact = title("Alien")

		SearchRanker.rank(listOf(anywhere, laterWord, prefix, exact), "alien") shouldContainExactly
			listOf(exact, prefix, laterWord, anywhere)
	}

	test("Matching ignores case and a leading article") {
		val withArticle = title("The Matrix")
		val other = title("Matrix Reloaded")

		SearchRanker.rank(listOf(other, withArticle), "MATRIX") shouldContainExactly listOf(withArticle, other)
	}

	test("Titles that tie keep alphabetical order") {
		val b = title("Star Wars")
		val a = title("Star Trek")

		SearchRanker.rank(listOf(b, a), "star") shouldContainExactly listOf(a, b)
	}
})

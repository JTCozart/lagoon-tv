package org.jellyfin.androidtv.ui.lagoon.design

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private const val LeadingEdge = 100f
private const val DeadZone = 20f
private const val ContainerSize = 1000f
private const val ChildSize = 200f

private val pinning = PinnedBringIntoViewSpec(LeadingEdge, pinVisibleChildren = true, deadZonePx = DeadZone)

class PinnedBringIntoViewSpecTests : FunSpec({
	test("A child far from the leading edge is scrolled onto it") {
		pinning.calculateScrollDistance(offset = 400f, size = ChildSize, containerSize = ContainerSize) shouldBe 300f
	}

	test("A child that is a few pixels off the leading edge does not move the list") {
		pinning.calculateScrollDistance(offset = 110f, size = ChildSize, containerSize = ContainerSize) shouldBe 0f
		pinning.calculateScrollDistance(offset = 85f, size = ChildSize, containerSize = ContainerSize) shouldBe 0f
	}

	test("A child scrolled out of view is brought back even when it is close to the edge") {
		pinning.calculateScrollDistance(offset = -30f, size = ChildSize, containerSize = ContainerSize) shouldBe -130f
	}

	test("A fully visible child leaves the list alone when visible children are not pinned") {
		val relaxed = PinnedBringIntoViewSpec(LeadingEdge, pinVisibleChildren = false, deadZonePx = DeadZone)
		relaxed.calculateScrollDistance(offset = 400f, size = ChildSize, containerSize = ContainerSize) shouldBe 0f
	}
})

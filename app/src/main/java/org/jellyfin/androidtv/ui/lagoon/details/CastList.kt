package org.jellyfin.androidtv.ui.lagoon.details

import org.jellyfin.sdk.model.api.BaseItemPerson
import org.jellyfin.sdk.model.api.PersonKind

/**
 * The people shown on a details page: on-screen cast first, then the director and writers.
 */
object CastList {
	private const val Maximum = 24

	private val rankedKinds = listOf(PersonKind.ACTOR, PersonKind.GUEST_STAR, PersonKind.DIRECTOR, PersonKind.CREATOR, PersonKind.WRITER)

	fun of(people: List<BaseItemPerson>): List<BaseItemPerson> =
		people
			.filter { it.type in rankedKinds }
			.sortedBy { rankedKinds.indexOf(it.type) }
			.take(Maximum)
}

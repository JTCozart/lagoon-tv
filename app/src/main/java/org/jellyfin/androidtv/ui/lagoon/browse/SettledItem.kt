package org.jellyfin.androidtv.ui.lagoon.browse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import org.jellyfin.sdk.model.api.BaseItemDto
import kotlin.time.Duration.Companion.milliseconds

private val SettleDelay = 220.milliseconds

/**
 * Follows [item], but only once focus has rested on it briefly. Holding a direction key then glides past
 * titles without restarting the spotlight's artwork and text for each one.
 */
@Composable
fun rememberSettledItem(item: BaseItemDto?): BaseItemDto? {
	val settled: State<BaseItemDto?> = produceState(initialValue = item, item?.id) {
		if (value != null) delay(SettleDelay)
		value = item
	}
	val shown by settled
	return shown
}

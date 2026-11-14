package org.jellyfin.androidtv.ui.lagoon.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.lagoon.design.PinFocusedChild
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

private val ListWidth = 196.dp
private val RowHeight = 38.dp
private val RowShape = RoundedCornerShape(10.dp)
private val SelectionSettle = 300.milliseconds

/** One row of the list: a genre, or every title when [id] is null. */
private class CategoryRow(val id: UUID?, val name: String)

/**
 * The genre list beside the All titles grid. Moving through it narrows the grid to the genre under the
 * focus once the focus rests there, so a category is a few presses away instead of a long scroll.
 */
@Composable
fun CategoryList(
	categories: List<LibraryCategory>,
	selectedId: UUID?,
	onSelect: (UUID?) -> Unit,
	modifier: Modifier = Modifier,
) {
	val allLabel = stringResource(R.string.lagoon_category_all)
	val rows = remember(categories, allLabel) {
		listOf(CategoryRow(null, allLabel)) + categories.map { CategoryRow(it.id, it.name) }
	}
	val selectedRequester = remember { FocusRequester() }
	val currentOnSelect by rememberUpdatedState(onSelect)
	var restingRow by remember { mutableStateOf<CategoryRow?>(null) }

	LaunchedEffect(restingRow) {
		val row = restingRow ?: return@LaunchedEffect
		delay(SelectionSettle)
		currentOnSelect(row.id)
	}

	// When the selection changes from outside the list (Back widening to All), the focus follows it
	var previousSelectedId by remember { mutableStateOf(selectedId) }
	LaunchedEffect(selectedId) {
		val isOutsideChange = selectedId != previousSelectedId && restingRow?.id != selectedId
		previousSelectedId = selectedId
		if (!isOutsideChange) return@LaunchedEffect
		withFrameNanos { }
		runCatching { selectedRequester.requestFocus() }
	}

	PinFocusedChild(leadingEdge = RowHeight * 2, pinVisibleChildren = false) {
		LazyColumn(
			verticalArrangement = Arrangement.spacedBy(4.dp),
			contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
			modifier = modifier
				.width(ListWidth)
				.fillMaxHeight()
				.focusRestorer(selectedRequester)
				.focusGroup(),
		) {
			items(rows, key = { it.id ?: "all" }) { row ->
				CategoryRowItem(
					name = row.name,
					isSelected = row.id == selectedId,
					onFocused = { restingRow = row },
					onClick = { currentOnSelect(row.id) },
					modifier = if (row.id == selectedId) Modifier.focusRequester(selectedRequester) else Modifier,
				)
			}
		}
	}
}

@Composable
private fun CategoryRowItem(
	name: String,
	isSelected: Boolean,
	onFocused: () -> Unit,
	onClick: () -> Unit,
	modifier: Modifier,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val colors = JellyfinTheme.colorScheme
	LaunchedEffect(isFocused) { if (isFocused) onFocused() }

	val background by animateColorAsState(
		when {
			isFocused -> colors.accent
			isSelected -> colors.accent.copy(alpha = 0.16f)
			else -> Color.Transparent
		},
		label = "CategoryBackground",
	)

	Box(
		contentAlignment = Alignment.CenterStart,
		modifier = modifier
			.fillMaxWidth()
			.height(RowHeight)
			.background(background, RowShape)
			.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
			.padding(horizontal = 14.dp),
	) {
		Text(
			text = name,
			fontSize = 15.sp,
			fontWeight = if (isSelected || isFocused) FontWeight.SemiBold else FontWeight.Normal,
			color = when {
				isFocused -> colors.onAccent
				isSelected -> colors.accent
				else -> Color.White.copy(alpha = 0.78f)
			},
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)
	}
}

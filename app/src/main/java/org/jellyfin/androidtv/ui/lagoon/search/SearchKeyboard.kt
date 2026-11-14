package org.jellyfin.androidtv.ui.lagoon.search

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.util.speech.SpeechRecognizerStatus
import org.jellyfin.androidtv.util.speech.rememberSpeechRecognizer

private val KeyRows = listOf("abcdef", "ghijkl", "mnopqr", "stuvwx", "yz1234", "567890")
private val KeySize = 42.dp
private val KeyGap = 6.dp
private val KeyShape = RoundedCornerShape(8.dp)
val KeyboardWidth: Dp = KeySize * 6 + KeyGap * 5

/**
 * The search box and the on-screen keyboard beside the results, laid out for a remote: letters and digits in a
 * six-wide grid, with voice, space, delete and clear above it.
 */
@Composable
fun SearchKeyboard(
	query: String,
	onType: (Char) -> Unit,
	onDelete: () -> Unit,
	onClear: () -> Unit,
	onVoiceQuery: (query: String, isFinal: Boolean) -> Unit,
	firstKeyRequester: FocusRequester,
	modifier: Modifier = Modifier,
) {
	Column(modifier.width(KeyboardWidth)) {
		QueryLine(query)

		Column(
			verticalArrangement = Arrangement.spacedBy(KeyGap),
			modifier = Modifier
				.padding(top = 18.dp)
				.focusRestorer(firstKeyRequester)
				.focusGroup(),
		) {
			Row(horizontalArrangement = Arrangement.spacedBy(KeyGap)) {
				VoiceKey(onVoiceQuery)
				Key(label = stringResource(R.string.lagoon_key_space), width = KeySize * 2 + KeyGap, onClick = { onType(' ') })
				Key(icon = R.drawable.lagoon_ic_backspace, label = stringResource(R.string.lagoon_key_delete), width = KeySize, onClick = onDelete)
				Key(label = stringResource(R.string.lagoon_key_clear), width = KeySize * 2 + KeyGap, onClick = onClear)
			}

			KeyRows.forEachIndexed { rowIndex, row ->
				Row(horizontalArrangement = Arrangement.spacedBy(KeyGap)) {
					row.forEachIndexed { columnIndex, character ->
						Key(
							label = character.toString(),
							width = KeySize,
							onClick = { onType(character) },
							modifier = if (rowIndex == 0 && columnIndex == 0) Modifier.focusRequester(firstKeyRequester) else Modifier,
						)
					}
				}
			}
		}
	}
}

@Composable
private fun QueryLine(query: String) {
	val colors = JellyfinTheme.colorScheme
	Column {
		Text(
			text = query.ifEmpty { stringResource(R.string.lagoon_search_hint) },
			fontSize = if (query.isEmpty()) 15.sp else 24.sp,
			fontWeight = if (query.isEmpty()) FontWeight.Normal else FontWeight.SemiBold,
			color = if (query.isEmpty()) colors.listCaption else Color.White,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.height(34.dp)
				.padding(top = if (query.isEmpty()) 8.dp else 0.dp),
		)
		Box(
			Modifier
				.padding(top = 6.dp)
				.fillMaxWidth()
				.height(2.dp)
				.background(colors.accent)
		)
	}
}

@Composable
private fun Key(
	width: Dp,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	label: String? = null,
	@DrawableRes icon: Int? = null,
	isActive: Boolean = false,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val colors = JellyfinTheme.colorScheme
	val background by animateColorAsState(
		when {
			isFocused -> colors.accent
			isActive -> colors.recording
			else -> Color.White.copy(alpha = 0.08f)
		},
		label = "KeyBackground",
	)
	val content = if (isFocused) colors.onAccent else Color.White

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.size(width = width, height = KeySize)
			.background(background, KeyShape)
			.clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
	) {
		if (icon != null) {
			Icon(ImageVector.vectorResource(icon), contentDescription = label, tint = content, modifier = Modifier.size(20.dp))
		} else if (label != null) {
			Text(
				text = label,
				fontSize = if (label.length == 1) 18.sp else 13.sp,
				fontWeight = FontWeight.SemiBold,
				color = content,
			)
		}
	}
}

@Composable
private fun VoiceKey(onVoiceQuery: (query: String, isFinal: Boolean) -> Unit) {
	val context = LocalContext.current
	val recognizer = rememberSpeechRecognizer(
		onResult = { query -> onVoiceQuery(query, true) },
		onPartialResult = { query -> onVoiceQuery(query, false) },
		onError = { status ->
			val message = when (status) {
				is SpeechRecognizerStatus.Error -> R.string.speech_error_unknown
				is SpeechRecognizerStatus.PermissionDenied -> if (!status.canRequest) R.string.speech_error_no_permission else null
				SpeechRecognizerStatus.Unavailable -> R.string.speech_error_unavailable
				else -> null
			}
			if (message != null) Toast.makeText(context, context.getString(message), Toast.LENGTH_LONG).show()
		},
	)
	val isListening = recognizer.status is SpeechRecognizerStatus.Listening

	Key(
		icon = R.drawable.ic_microphone,
		label = stringResource(R.string.lagoon_key_voice),
		width = KeySize,
		isActive = isListening,
		onClick = { if (isListening) recognizer.stopListening() else recognizer.startListening() },
	)
}

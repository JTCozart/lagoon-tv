package org.jellyfin.androidtv.ui.lagoon.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.lagoon.browse.rememberLagoonActions
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

class DetailsFragment : Fragment() {
	companion object {
		const val ArgumentItemId = "lagoon_item_id"
	}

	private val backgroundService by inject<BackgroundService>()

	private val itemId by lazy { UUID.fromString(requireArguments().getString(ArgumentItemId)) }
	private val viewModel by viewModel<DetailsViewModel> { parametersOf(itemId) }

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = content {
		DetailsScreen(viewModel = viewModel, actions = rememberLagoonActions(isInsideLibrary = false))
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		// Reload on every return so progress, watched state and the next episode reflect what was just played
		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
				backgroundService.clearBackgrounds()
				viewModel.refresh()
			}
		}
	}
}

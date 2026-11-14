package org.jellyfin.androidtv.ui.lagoon.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.constant.Extras
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.lagoon.browse.rememberLagoonActions
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.primaryImage
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class LibraryFragment : Fragment() {
	private val userRepository by inject<UserRepository>()
	private val backgroundService by inject<BackgroundService>()
	private val api by inject<ApiClient>()

	private val library by lazy {
		Json.decodeFromString<BaseItemDto>(requireArguments().getString(Extras.Folder)!!)
	}

	// Upstream navigation detaches this fragment rather than destroying it, so the page and focus survive a trip to details
	private val viewModel by viewModel<LibraryViewModel> { parametersOf(library) }

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = content {
		val actions = rememberLagoonActions(isInsideLibrary = true)
		val user by userRepository.currentUser.collectAsState()

		LibraryScreen(
			viewModel = viewModel,
			actions = actions,
			profileName = user?.name.orEmpty(),
			profileImageUrl = user?.primaryImage?.getUrl(api),
		)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
				backgroundService.clearBackgrounds()
				viewModel.refresh()
			}
		}
	}
}

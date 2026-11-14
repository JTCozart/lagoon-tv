package org.jellyfin.androidtv.ui.lagoon.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.lagoon.browse.rememberLagoonActions
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.primaryImage
import org.jellyfin.sdk.api.client.ApiClient
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.time.Duration.Companion.milliseconds

class LagoonSearchFragment : Fragment() {
	companion object {
		/** Same key as upstream's search screen, so system search intents carry their query over unchanged. */
		const val ArgumentQuery = "query"
	}

	private val userRepository by inject<UserRepository>()
	private val backgroundService by inject<BackgroundService>()
	private val api by inject<ApiClient>()
	private val viewModel by viewModel<LagoonSearchViewModel>()

	private val initialQuery by lazy { arguments?.getString(ArgumentQuery)?.takeIf { it.isNotBlank() } }

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = content {
		val user by userRepository.currentUser.collectAsState()

		SearchScreen(
			viewModel = viewModel,
			actions = rememberLagoonActions(isInsideLibrary = true),
			profileName = user?.name.orEmpty(),
			profileImageUrl = user?.primaryImage?.getUrl(api),
			startInResults = initialQuery != null,
		)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		backgroundService.clearBackgrounds()
		if (savedInstanceState == null) initialQuery?.let { viewModel.setQuery(it, 0.milliseconds) }
	}
}

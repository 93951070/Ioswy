package me.wcy.music.search

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.SearchScreen
import me.wcy.music.consts.RoutePath
import me.wcy.music.service.PlayerController
import me.wcy.music.utils.toMediaItem
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route
import javax.inject.Inject

/**
 * Created by wangchenyan.top on 2023/9/20.
 */
@Route(RoutePath.SEARCH)
@AndroidEntryPoint
class SearchFragment : BaseMusicFragment() {
    private val viewModel by activityViewModels<SearchViewModel> {
        viewModelFactory {
            initializer {
                SearchViewModel(searchHistoryStore)
            }
        }
    }
    private var composeView: ComposeView? = null

    @Inject
    lateinit var playerController: PlayerController

    private val searchHistoryStore: SearchHistoryStore = object : SearchHistoryStore {
        override fun loadHistory(): List<String> {
            return SearchPreference.historyKeywords ?: emptyList()
        }

        override fun saveHistory(keywords: List<String>) {
            SearchPreference.historyKeywords = keywords
        }
    }

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    SearchScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onOpenPlaylistDetail = { id ->
                            CRouter.with(requireActivity())
                                .url(RoutePath.PLAYLIST_DETAIL)
                                .extra("id", id)
                                .start()
                        },
                        onPlayAll = { songs ->
                            val items = songs.map { it.toMediaItem() }
                            if (items.isNotEmpty()) {
                                playerController.replaceAll(items, items.first())
                                CRouter.with(requireContext()).url(RoutePath.PLAYING).start()
                            }
                        },
                        onPlaySong = { song ->
                            playerController.addAndPlay(song.toMediaItem())
                            CRouter.with(requireContext()).url(RoutePath.PLAYING).start()
                        }
                    )
                }
            }
            composeView = view
        }
    }

    override fun onInterceptBackEvent(): Boolean {
        if (viewModel.showResult.value) {
            viewModel.showHistory()
            return true
        }
        return super.onInterceptBackEvent()
    }
}

package me.wcy.music.discover.artist

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.artist.list.ArtistListScreen
import me.wcy.music.artist.list.viewmodel.ArtistListViewModel
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.consts.RoutePath
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route

/**
 * 歌手列表页
 */
@Route(RoutePath.ARTIST_LIST)
@AndroidEntryPoint
class ArtistListFragment : BaseMusicFragment() {
    private val viewModel by viewModels<ArtistListViewModel>()
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    ArtistListScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onOpenArtist = { id ->
                            CRouter.with(requireActivity())
                                .url(RoutePath.ARTIST_DETAIL)
                                .extra("id", id)
                                .start()
                        }
                    )
                }
            }
            composeView = view
        }
    }
}

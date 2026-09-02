package me.wcy.music.mine.extra

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.consts.RoutePath
import me.wcy.music.mine.extra.sub.SubListScreen
import me.wcy.music.mine.extra.sub.SubListViewModel
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route

@Route(RoutePath.SUB_LIST)
@AndroidEntryPoint
class SubListFragment : BaseMusicFragment() {
    private val viewModel by viewModels<SubListViewModel>()
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    SubListScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onOpenArtist = { id ->
                            CRouter.with(requireActivity())
                                .url(RoutePath.ARTIST_DETAIL)
                                .extra("id", id)
                                .start()
                        },
                        onOpenAlbum = { id ->
                            CRouter.with(requireActivity())
                                .url(RoutePath.ALBUM_DETAIL)
                                .extra("id", id)
                                .start()
                        },
                        onOpenMv = { id ->
                            CRouter.with(requireActivity())
                                .url(RoutePath.MV_DETAIL)
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

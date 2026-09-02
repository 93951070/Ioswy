package me.wcy.music.discover.album

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.album.new.AlbumNewScreen
import me.wcy.music.album.new.viewmodel.AlbumNewViewModel
import me.wcy.music.consts.RoutePath
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route

@Route(RoutePath.ALBUM_NEW)
@AndroidEntryPoint
class AlbumNewFragment : BaseMusicFragment() {
    private val viewModel by viewModels<AlbumNewViewModel>()
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    AlbumNewScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onOpenAlbum = { id ->
                            CRouter.with(requireActivity())
                                .url(RoutePath.ALBUM_DETAIL)
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

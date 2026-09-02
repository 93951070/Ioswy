package me.wcy.music.mine.extra

import android.view.View
import androidx.compose.ui.platform.ComposeView
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.ImportPlaylistScreen
import me.wcy.music.compose.ui.ImportPlaylistViewModel
import me.wcy.music.consts.RoutePath
import me.wcy.router.annotation.Route

@Route(RoutePath.IMPORT_PLAYLIST)
@AndroidEntryPoint
class ImportPlaylistFragment : BaseMusicFragment() {
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    ImportPlaylistScreen(
                        viewModel = ImportPlaylistViewModel(),
                        onBack = { finish() }
                    )
                }
            }
            composeView = view
        }
    }
}

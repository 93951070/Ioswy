package me.wcy.music.discover.video

import android.view.View
import androidx.compose.ui.platform.ComposeView
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.consts.RoutePath
import me.wcy.router.annotation.Route
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.VideoScreen

@Route(RoutePath.VIDEO)
@AndroidEntryPoint
class VideoFragment : BaseMusicFragment() {
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    VideoScreen(onBack = { finish() })
                }
            }
            composeView = view
        }
    }
}

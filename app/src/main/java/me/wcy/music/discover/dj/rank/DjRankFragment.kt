package me.wcy.music.discover.dj.rank

import android.view.View
import androidx.compose.ui.platform.ComposeView
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.router.annotation.Route
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.DjRankScreen
import me.wcy.music.consts.RoutePath
import me.wcy.router.CRouter

@Route(RoutePath.DJ_RANK)
@AndroidEntryPoint
class DjRankFragment : BaseMusicFragment() {
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    DjRankScreen(
                        onBack = { finish() },
                        onOpenRadio = { id ->
                            CRouter.with(requireActivity())
                                .url(RoutePath.DJ_DETAIL)
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

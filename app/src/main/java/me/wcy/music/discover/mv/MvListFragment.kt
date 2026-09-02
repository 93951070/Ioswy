package me.wcy.music.discover.mv

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.consts.RoutePath
import me.wcy.music.mv.MvListScreen
import me.wcy.music.mv.viewmodel.MvListViewModel
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route

@Route(RoutePath.MV_LIST)
@AndroidEntryPoint
class MvListFragment : BaseMusicFragment() {
    private val viewModel by viewModels<MvListViewModel>()
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    MvListScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
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

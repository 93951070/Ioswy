package me.wcy.music.discover.dj

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.consts.RoutePath
import me.wcy.music.dj.list.DjRecommendScreen
import me.wcy.music.dj.list.viewmodel.DjRecommendViewModel
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route

/**
 * Created by wangchenyan.top on 2023/9/26.
 */
@Route(RoutePath.DJ_RECOMMEND)
@AndroidEntryPoint
class DjRecommendFragment : BaseMusicFragment() {
    private val viewModel by viewModels<DjRecommendViewModel>()
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    DjRecommendScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onOpenDj = { id ->
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

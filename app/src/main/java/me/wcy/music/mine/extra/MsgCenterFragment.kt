package me.wcy.music.mine.extra

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.consts.RoutePath
import me.wcy.music.mine.extra.msg.MsgCenterScreen
import me.wcy.music.mine.extra.msg.MsgCenterViewModel
import me.wcy.router.annotation.Route

@Route(RoutePath.MSG_CENTER)
@AndroidEntryPoint
class MsgCenterFragment : BaseMusicFragment() {
    private val viewModel by viewModels<MsgCenterViewModel>()
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    MsgCenterScreen(
                        viewModel = viewModel,
                        onBack = { finish() }
                    )
                }
            }
            composeView = view
        }
    }
}

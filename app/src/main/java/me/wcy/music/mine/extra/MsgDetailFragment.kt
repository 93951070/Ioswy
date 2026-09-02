package me.wcy.music.mine.extra

import android.view.View
import androidx.compose.ui.platform.ComposeView
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.mine.extra.msg.MsgDetailScreen

@AndroidEntryPoint
class MsgDetailFragment : BaseMusicFragment() {
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        val uid = requireArguments().getLong("uid")
        val nickname = requireArguments().getString("nickname").orEmpty()
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    MsgDetailScreen(
                        uid = uid,
                        nickname = nickname,
                        onBack = { finish() }
                    )
                }
            }
            composeView = view
        }
    }

}

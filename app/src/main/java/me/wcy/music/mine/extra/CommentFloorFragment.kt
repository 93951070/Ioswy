package me.wcy.music.mine.extra

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.consts.RoutePath
import me.wcy.router.annotation.Route
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.CommentFloorScreen
import me.wcy.music.discover.comment.viewmodel.CommentFloorViewModel
import top.wangchenyan.common.ext.toast

@Route(RoutePath.COMMENT_FLOOR)
@AndroidEntryPoint
class CommentFloorFragment : BaseMusicFragment() {
    private val viewModel by viewModels<CommentFloorViewModel>()
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        val args = requireArguments()
        val resourceId = args.getLong("resourceId")
        val resourceType = args.getInt("resourceType", 0)
        val parentCommentId = args.getLong("parentCommentId")
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    CommentFloorScreen(
                        viewModel = viewModel,
                        resourceId = resourceId,
                        resourceType = resourceType,
                        parentCommentId = parentCommentId,
                        onBack = { finish() },
                        onMessage = { toast(it) }
                    )
                }
            }
            composeView = view
        }
    }
}

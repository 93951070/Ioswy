package me.wcy.music.discover.mv

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.consts.RoutePath
import me.wcy.music.mv.detail.MvDetailScreen
import me.wcy.music.mv.detail.viewmodel.MvDetailViewModel
import me.wcy.router.annotation.Route

@Route(RoutePath.MV_DETAIL)
@AndroidEntryPoint
class MvDetailFragment : BaseMusicFragment() {
    private val viewModel by viewModels<MvDetailViewModel>()
    private var composeView: ComposeView? = null

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    val id = getRouteArguments().getLongExtra("id", 0)
                    if (id <= 0) {
                        finish()
                        return@MusicTheme
                    }
                    MvDetailScreen(
                        viewModel = viewModel,
                        mvid = id,
                        onBack = { finish() },
                        onPlayMv = { url ->
                            playMv(url)
                        }
                    )
                }
            }
            composeView = view
        }
    }

    private fun playMv(url: String) {
        if (url.isBlank()) return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(url), "video/mp4")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        kotlin.runCatching { startActivity(intent) }
    }
}

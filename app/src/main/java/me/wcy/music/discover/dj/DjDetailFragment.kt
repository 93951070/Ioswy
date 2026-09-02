package me.wcy.music.discover.dj

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.consts.RoutePath
import me.wcy.music.dj.detail.DjDetailScreen
import me.wcy.music.dj.detail.viewmodel.DjDetailViewModel
import me.wcy.music.service.PlayerController
import me.wcy.music.utils.toMediaItem
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route
import javax.inject.Inject

/**
 * Created by wangchenyan.top on 2023/9/26.
 */
@Route(RoutePath.DJ_DETAIL)
@AndroidEntryPoint
class DjDetailFragment : BaseMusicFragment() {
    private val viewModel by viewModels<DjDetailViewModel>()
    private var composeView: ComposeView? = null

    @Inject
    lateinit var playerController: PlayerController

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    val id = getRouteArguments().getLongExtra("id", 0)
                    if (id <= 0) {
                        finish()
                        return@MusicTheme
                    }
                    DjDetailScreen(
                        viewModel = viewModel,
                        rid = id,
                        onBack = { finish() },
                        onPlaySong = { song ->
                            playSong(song)
                        }
                    )
                }
            }
            composeView = view
        }
    }

    private fun playSong(song: SongData) {
        if (song.id <= 0) return
        val mediaItem = song.toMediaItem()
        playerController.replaceAll(listOf(mediaItem), mediaItem)
        CRouter.with(requireContext()).url(RoutePath.PLAYING).start()
    }
}

package me.wcy.music.discover.recommend.song

import android.view.View
import androidx.compose.ui.platform.ComposeView
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.RecommendSongScreen
import me.wcy.music.consts.RoutePath
import me.wcy.music.service.PlayerController
import me.wcy.music.utils.toMediaItem
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route
import javax.inject.Inject

/**
 * Created by wangchenyan.top on 2023/9/15.
 */
@Route(RoutePath.RECOMMEND_SONG, needLogin = true)
@AndroidEntryPoint
class RecommendSongFragment : BaseMusicFragment() {
    private var composeView: ComposeView? = null

    @Inject
    lateinit var playerController: PlayerController

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    RecommendSongScreen(
                        onBack = { finish() },
                        onPlayAll = { songs ->
                            playSongs(songs, 0)
                        },
                        onPlaySong = { songs, position ->
                            playSongs(songs, position)
                        }
                    )
                }
            }
            composeView = view
        }
    }

    private fun playSongs(songs: List<SongData>, position: Int) {
        val items = songs.map { it.toMediaItem() }
        if (items.isNotEmpty()) {
            playerController.replaceAll(items, items[position.coerceAtMost(items.lastIndex)])
            CRouter.with(requireContext()).url(RoutePath.PLAYING).start()
        }
    }
}

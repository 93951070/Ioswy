package me.wcy.music.mine.extra

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.consts.RoutePath
import me.wcy.music.mine.extra.recent.RecentPlayScreen
import me.wcy.music.mine.extra.recent.RecentPlayViewModel
import me.wcy.music.service.PlayerController
import me.wcy.music.utils.toMediaItem
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route
import javax.inject.Inject

@Route(RoutePath.RECENT_PLAY)
@AndroidEntryPoint
class RecentPlayFragment : BaseMusicFragment() {
    private val viewModel by viewModels<RecentPlayViewModel>()
    private var composeView: ComposeView? = null

    @Inject
    lateinit var playerController: PlayerController

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            viewModel.uid = arguments?.getLong("uid") ?: 0
            view.setContent {
                MusicTheme {
                    RecentPlayScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onPlaySongs = { songs, index ->
                            playSongs(songs, index)
                        }
                    )
                }
            }
            composeView = view
        }
    }

    private fun playSongs(songs: List<SongData>, index: Int) {
        val mediaItems = songs.map { it.toMediaItem() }
        if (index in mediaItems.indices) {
            playerController.replaceAll(mediaItems, mediaItems[index])
            context?.let {
                CRouter.with(it).url(RoutePath.PLAYING).start()
            }
        }
    }
}

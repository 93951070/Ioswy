package me.wcy.music.discover.playlist.square

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.PlaylistSquareScreen
import me.wcy.music.consts.RoutePath
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.discover.playlist.square.viewmodel.PlaylistSquareViewModel
import me.wcy.music.service.PlayerController
import me.wcy.music.utils.toMediaItem
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route
import javax.inject.Inject

/**
 * Created by wangchenyan.top on 2023/9/26.
 */
@Route(RoutePath.PLAYLIST_SQUARE)
@AndroidEntryPoint
class PlaylistSquareFragment : BaseMusicFragment() {
    private val viewModel by viewModels<PlaylistSquareViewModel>()
    private var composeView: ComposeView? = null

    @Inject
    lateinit var playerController: PlayerController

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    PlaylistSquareScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onOpenPlaylistDetail = { id ->
                            CRouter.with(requireActivity())
                                .url(RoutePath.PLAYLIST_DETAIL)
                                .extra("id", id)
                                .start()
                        },
                        onPlayPlaylist = { playlist ->
                            playPlaylist(playlist.id)
                        }
                    )
                }
            }
            composeView = view
        }
    }

    private fun playPlaylist(playlistId: Long) {
        lifecycleScope.launch {
            kotlin.runCatching {
                DiscoverNet.getFullPlaylistSongList(playlistId)
            }.onSuccess { songListData ->
                if (songListData.code == 200 && songListData.songs.isNotEmpty()) {
                    val songs = songListData.songs.map { it.toMediaItem() }
                    playerController.replaceAll(songs, songs.first())
                    CRouter.with(requireContext()).url(RoutePath.PLAYING).start()
                }
            }
        }
    }
}

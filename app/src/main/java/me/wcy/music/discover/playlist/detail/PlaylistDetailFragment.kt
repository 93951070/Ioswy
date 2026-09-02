package me.wcy.music.discover.playlist.detail

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.PlaylistDetailScreen
import me.wcy.music.consts.RoutePath
import me.wcy.music.discover.playlist.detail.viewmodel.PlaylistViewModel
import me.wcy.music.service.PlayerController
import me.wcy.music.service.likesong.LikeSongProcessor
import me.wcy.music.utils.toMediaItem
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route
import javax.inject.Inject

/**
 * Created by wangchenyan.top on 2023/9/22.
 */
@Route(RoutePath.PLAYLIST_DETAIL)
@AndroidEntryPoint
class PlaylistDetailFragment : BaseMusicFragment() {
    private val viewModel by viewModels<PlaylistViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PlaylistViewModel {
                    likeSongProcessor.updateLikeSongList()
                } as T
            }
        }
    }
    private var composeView: ComposeView? = null

    @Inject
    lateinit var likeSongProcessor: LikeSongProcessor

    @Inject
    lateinit var playerController: PlayerController

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    val args = getRouteArguments()
                    val id = args.getLongExtra("id", 0)
                    val realtimeData = args.getBooleanExtra("realtime_data", false)
                    val isLike = args.getBooleanExtra("is_like", false)
                    if (id <= 0) {
                        finish()
                        return@MusicTheme
                    }
                    PlaylistDetailScreen(
                        viewModel = viewModel,
                        playlistId = id,
                        realtimeData = realtimeData,
                        isLike = isLike,
                        onBack = { finish() },
                        onOpenPlaying = {
                            CRouter.with(requireContext()).url(RoutePath.PLAYING).start()
                        },
                        onPlayAll = { songs ->
                            playSongs(0, songs)
                        },
                        onPlaySong = { index, songs ->
                            playSongs(index, songs)
                        }
                    )
                }
            }
            composeView = view
        }
    }

    private fun playSongs(index: Int, songs: List<SongData>) {
        val mediaItems = songs.map { it.toMediaItem() }
        if (mediaItems.isEmpty() || index !in mediaItems.indices) return
        playerController.replaceAll(mediaItems, mediaItems[index])
        CRouter.with(requireContext()).url(RoutePath.PLAYING).start()
    }
}

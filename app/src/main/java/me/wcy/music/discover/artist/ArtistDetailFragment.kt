package me.wcy.music.discover.artist

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.common.bean.SongData
import me.wcy.music.artist.detail.ArtistDetailScreen
import me.wcy.music.artist.detail.viewmodel.ArtistDetailViewModel
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.consts.RoutePath
import me.wcy.music.service.PlayerController
import me.wcy.music.utils.toMediaItem
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route
import javax.inject.Inject

/**
 * 歌手详情页
 */
@Route(RoutePath.ARTIST_DETAIL)
@AndroidEntryPoint
class ArtistDetailFragment : BaseMusicFragment() {
    private val viewModel by viewModels<ArtistDetailViewModel>()
    private var composeView: ComposeView? = null

    @Inject
    lateinit var playerController: PlayerController

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    val args = getRouteArguments()
                    val id = args.getLongExtra("id", 0)
                    if (id <= 0) {
                        finish()
                        return@MusicTheme
                    }
                    ArtistDetailScreen(
                        viewModel = viewModel,
                        artistId = id,
                        onBack = { finish() },
                        onPlaySongs = { songs, index ->
                            playSongs(index, songs)
                        },
                        onOpenAlbum = { albumId ->
                            CRouter.with(requireContext())
                                .url(RoutePath.ALBUM_DETAIL)
                                .extra("id", albumId)
                                .start()
                        },
                        onOpenMv = { mvId ->
                            CRouter.with(requireContext())
                                .url(RoutePath.MV_DETAIL)
                                .extra("id", mvId)
                                .start()
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

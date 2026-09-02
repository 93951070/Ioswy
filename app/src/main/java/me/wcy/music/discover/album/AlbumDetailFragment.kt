package me.wcy.music.discover.album

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.album.detail.AlbumDetailScreen
import me.wcy.music.album.detail.viewmodel.AlbumDetailViewModel
import me.wcy.music.consts.RoutePath
import me.wcy.music.service.PlayerController
import me.wcy.music.utils.toMediaItem
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route
import javax.inject.Inject

@Route(RoutePath.ALBUM_DETAIL)
@AndroidEntryPoint
class AlbumDetailFragment : BaseMusicFragment() {
    private val viewModel by viewModels<AlbumDetailViewModel>()
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
                    AlbumDetailScreen(
                        viewModel = viewModel,
                        albumId = id,
                        onBack = { finish() },
                        onOpenArtist = { artistId ->
                            CRouter.with(requireActivity())
                                .url(RoutePath.ARTIST_DETAIL)
                                .extra("id", artistId)
                                .start()
                        },
                        onPlaySongs = { songs, index ->
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

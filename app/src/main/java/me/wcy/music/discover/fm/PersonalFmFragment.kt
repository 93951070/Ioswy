package me.wcy.music.discover.fm

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.common.bean.AlbumData
import me.wcy.music.common.bean.ArtistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.PersonalFmScreen
import me.wcy.music.consts.RoutePath
import me.wcy.music.discover.fm.viewmodel.PersonalFmViewModel
import me.wcy.music.service.PlayState
import me.wcy.music.service.PlayerController
import me.wcy.music.service.likesong.LikeSongProcessor
import me.wcy.music.utils.getBaseCover
import me.wcy.music.utils.getDuration
import me.wcy.music.utils.getSongId
import me.wcy.music.utils.toMediaItem
import me.wcy.router.annotation.Route
import top.wangchenyan.common.ext.toast
import javax.inject.Inject

@Route(RoutePath.PERSONAL_FM, needLogin = true)
@AndroidEntryPoint
class PersonalFmFragment : BaseMusicFragment() {
    private var composeView: ComposeView? = null

    @Inject
    lateinit var playerController: PlayerController

    @Inject
    lateinit var likeSongProcessor: LikeSongProcessor

    private val viewModel by viewModels<PersonalFmViewModel>()

    private val currentFmSong: StateFlow<SongData?> by lazy {
        combine(playerController.playlist, playerController.currentSong) { playlist, song ->
            playlist.firstOrNull { it.mediaId == song?.mediaId }?.toFmSongData()
        }.stateIn(lifecycleScope, SharingStarted.Eagerly, null)
    }

    private val isPlaying: StateFlow<Boolean> by lazy {
        playerController.playState.map { it == PlayState.Playing }
            .stateIn(lifecycleScope, SharingStarted.Eagerly, false)
    }

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    PersonalFmScreen(
                        currentSong = currentFmSong,
                        isPlaying = isPlaying,
                        playProgress = playerController.playProgress,
                        fmError = viewModel.error,
                        onPlayPause = { playerController.playPause() },
                        onSeekTo = { playerController.seekTo(it) },
                        onNext = { onNext() },
                        isLiked = { likeSongProcessor.isLiked(it) },
                        onLike = { songId ->
                            lifecycleScope.launch {
                                val res = likeSongProcessor.like(requireActivity(), songId)
                                if (!res.isSuccess()) {
                                    toast(res.msg)
                                }
                            }
                        },
                        onErrorRetry = { viewModel.loadFm { songs -> onFmLoaded(songs) } },
                        onBack = { finish() }
                    )
                }
            }
            viewModel.loadFm { songs -> onFmLoaded(songs) }
            composeView = view
        }
    }

    private fun onNext() {
        val playlist = playerController.playlist.value
        val song = playerController.currentSong.value
        val index = playlist.indexOfFirst { it.mediaId == song?.mediaId }
        if (index >= 0 && index == playlist.lastIndex) {
            viewModel.loadFm { songs -> onFmLoaded(songs) }
        } else {
            playerController.next()
        }
    }

    private fun onFmLoaded(songs: List<SongData>) {
        val items = songs.map { it.toMediaItem() }
        playerController.replaceAll(items, items.first())
    }

    private fun MediaItem.toFmSongData(): SongData {
        return SongData(
            id = getSongId(),
            name = mediaMetadata.title?.toString() ?: "",
            ar = listOf(ArtistData(name = mediaMetadata.artist?.toString() ?: "")),
            al = AlbumData(
                name = mediaMetadata.albumTitle?.toString() ?: "",
                picUrl = mediaMetadata.getBaseCover() ?: ""
            ),
            dt = mediaMetadata.getDuration()
        )
    }
}

package me.wcy.music.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.discover.home.viewmodel.DiscoverViewModel
import me.wcy.music.service.PlayerController
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.utils.toMediaItem

/**
 * Android 兼容壳：MainScreen 冻结旧签名，这里保留 playerController 参数并委托 shared 版 DiscoverScreen。
 */
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    playerController: PlayerController,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onOpenRanking: () -> Unit,
    onOpenPlaylistSquare: () -> Unit,
    onOpenRecommendSong: () -> Unit,
    onOpenPersonalFm: () -> Unit,
    onOpenPlaying: () -> Unit
) {
    val scope = rememberCoroutineScope()

    fun playPlaylist(playlistId: Long, songPosition: Int = 0) {
        scope.launch {
            kotlin.runCatching {
                DiscoverNet.getFullPlaylistSongList(playlistId)
            }.onSuccess { songListData ->
                if (songListData.code == 200 && songListData.songs.isNotEmpty()) {
                    val songs = songListData.songs.map { it.toMediaItem() }
                    playerController.replaceAll(songs, songs.getOrElse(songPosition) { songs[0] })
                }
            }
        }
    }

    DiscoverScreen(
        viewModel = viewModel,
        onOpenDrawer = onOpenDrawer,
        onOpenSearch = onOpenSearch,
        onOpenPlaylistDetail = onOpenPlaylistDetail,
        onOpenRanking = onOpenRanking,
        onOpenPlaylistSquare = onOpenPlaylistSquare,
        onOpenRecommendSong = onOpenRecommendSong,
        onOpenPersonalFm = onOpenPersonalFm,
        onOpenPlaying = onOpenPlaying,
        onPlaySong = { song: SongData ->
            playerController.addAndPlay(song.toMediaItem())
            onOpenPlaying()
        },
        onPlayPlaylist = { playlist: PlaylistData ->
            playPlaylist(playlist.id)
        },
        onPlayPlaylistSong = { playlist: PlaylistData, position: Int ->
            playPlaylist(playlist.id, position)
        }
    )
}

package me.wcy.music.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.discover.home.viewmodel.DiscoverViewModel
import me.wcy.music.service.PlayerController
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.player.PlayerEngine
import me.wcy.music.utils.toMediaItem

/**
 * Android 兼容壳：MainScreen 冻结旧签名，这里保留 playerController 参数并委托 shared 版 DiscoverScreen。
 */
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    playerController: PlayerController,
    playerEngine: PlayerEngine,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onOpenRanking: () -> Unit,
    onOpenPlaylistSquare: () -> Unit,
    onOpenRecommendSong: () -> Unit,
    onOpenPersonalFm: () -> Unit,
    onOpenArtistList: () -> Unit,
    onOpenNewSong: () -> Unit,
    onOpenDj: () -> Unit,
    onOpenMvList: () -> Unit,
    onOpenPlaying: () -> Unit,
    onOpenDjRadio: (Long) -> Unit = {},
    onOpenMv: (Long) -> Unit = {},
    onOpenVideo: () -> Unit = {},
    onOpenDjRank: () -> Unit = {}
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
        playerEngine = playerEngine,
        onOpenDrawer = onOpenDrawer,
        onOpenDjRadio = onOpenDjRadio,
        onOpenMv = onOpenMv,
        onOpenVideo = onOpenVideo,
        onOpenDjRank = onOpenDjRank,
        onOpenSearch = onOpenSearch,
        onOpenPlaylistDetail = onOpenPlaylistDetail,
        onOpenRanking = onOpenRanking,
        onOpenPlaylistSquare = onOpenPlaylistSquare,
        onOpenRecommendSong = onOpenRecommendSong,
        onOpenPersonalFm = onOpenPersonalFm,
        onOpenArtistList = onOpenArtistList,
        onOpenNewSong = onOpenNewSong,
        onOpenDj = onOpenDj,
        onOpenMvList = onOpenMvList,
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
        },
        onPlayDailySong = { songs: List<SongData>, index: Int ->
            // 与 iOS playSongList 对齐：整组替换播放（心动队列/每日推荐都需要完整队列）
            val items = songs.map { it.toMediaItem() }
            items.firstOrNull()?.let { playerController.replaceAll(items, items.getOrElse(index) { items[0] }) }
        }
    )
}

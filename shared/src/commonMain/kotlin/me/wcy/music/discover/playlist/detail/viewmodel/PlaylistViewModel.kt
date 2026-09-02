package me.wcy.music.discover.playlist.detail.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.net.MineNet
import me.wcy.music.shared.net.NetResult
import me.wcy.music.shared.net.SharedNet
import me.wcy.music.shared.net.apiCall

/**
 * Created by wangchenyan.top on 2023/9/22.
 */
class PlaylistViewModel(
    private val onLikeSongListChanged: () -> Unit = {}
) : ViewModel() {

    private val _playlistData = MutableStateFlow<PlaylistData?>(null)
    val playlistData: StateFlow<PlaylistData?> = _playlistData.asStateFlow()

    private val _songList = MutableStateFlow<List<SongData>>(emptyList())
    val songList: StateFlow<List<SongData>> = _songList.asStateFlow()

    private var playlistId = 0L
    private var realtimeData = false
    private var isLike = false

    fun init(playlistId: Long, realtimeData: Boolean, isLike: Boolean) {
        this.playlistId = playlistId
        this.realtimeData = realtimeData
        this.isLike = isLike
    }

    suspend fun loadData(): NetResult<Unit> {
        val detailRes = kotlin.runCatching {
            DiscoverNet.getPlaylistDetail(playlistId)
        }
        val detail = detailRes.getOrNull()
        if (detail == null || detail.code != 200) {
            return NetResult(code = -1, msg = detailRes.exceptionOrNull()?.message)
        }
        val timestamp = if (realtimeData) SharedNet.currentTimeMillis() else null
        val songListRes = kotlin.runCatching {
            DiscoverNet.getFullPlaylistSongList(playlistId, timestamp = timestamp)
        }
        val songListData = songListRes.getOrNull()
        if (songListData == null || songListData.code != 200) {
            return NetResult(code = -1, msg = songListRes.exceptionOrNull()?.message)
        }
        _playlistData.value = detail.playlist
        _songList.value = songListData.songs
        return NetResult(code = 200)
    }

    suspend fun collect(): NetResult<Unit> {
        val data = _playlistData.value ?: return NetResult(code = -1)
        val res = apiCall {
            MineNet.collectPlaylist(data.id, if (data.subscribed) 2 else 1)
        }
        return if (res.isSuccess()) {
            _playlistData.value = data.copy(subscribed = !data.subscribed)
            NetResult(code = 200)
        } else {
            NetResult(code = res.code, msg = res.msg)
        }
    }

    fun removeSong(songData: SongData) {
        val songList = _songList.value.toMutableList()
        songList.remove(songData)
        _songList.value = songList
        if (isLike) {
            onLikeSongListChanged()
        }
    }
}

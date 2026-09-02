package me.wcy.music.discover.ranking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.shared.net.DiscoverNet

/**
 * Created by wangchenyan.top on 2023/10/25.
 */
class RankingViewModel : ViewModel() {
    data class TitleData(val title: String)
    data class RankingPlaylist(val playlist: PlaylistData, val songs: List<SongData>)

    private val _rankingList = MutableStateFlow<List<Any>>(emptyList())
    val rankingList: StateFlow<List<Any>> = _rankingList.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            val rankingListRes = kotlin.runCatching {
                DiscoverNet.getRankingList()
            }.getOrNull() ?: return@launch
            if (rankingListRes.code != 200) return@launch
            val rankingList = rankingListRes.playlists
            val officialList = rankingList.filter { it.toplistType.isNotEmpty() }
            val selectedList = rankingList.filter { it.toplistType.isEmpty() }
            val finalList =
                listOf(TitleData("官方榜")) + officialList.map { RankingPlaylist(it, emptyList()) } +
                    listOf(TitleData("精选榜")) + selectedList.map { RankingPlaylist(it, emptyList()) }
            _rankingList.value = finalList
            officialList.forEach { playlist ->
                launch {
                    val songListRes = kotlin.runCatching {
                        DiscoverNet.getPlaylistSongList(playlist.id, limit = 3)
                    }.getOrNull()
                    if (songListRes?.code == 200) {
                        _rankingList.update { list ->
                            list.map { item ->
                                if (item is RankingPlaylist && item.playlist.id == playlist.id) {
                                    item.copy(songs = songListRes.songs)
                                } else {
                                    item
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

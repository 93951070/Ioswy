package me.wcy.music.search.bean

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData

/**
 * Created by wangchenyan.top on 2023/9/20.
 */
@Serializable
data class SearchResultData(
    @SerialName("songs")
    val songs: List<SongData> = emptyList(),
    @SerialName("songCount")
    val songCount: Int = 0,
    @SerialName("playlists")
    val playlists: List<PlaylistData> = emptyList(),
    @SerialName("playlistCount")
    val playlistCount: Int = 0,
)

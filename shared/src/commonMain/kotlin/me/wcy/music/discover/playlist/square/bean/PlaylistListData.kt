package me.wcy.music.discover.playlist.square.bean

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import me.wcy.music.common.bean.PlaylistData

/**
 * Created by wangchenyan.top on 2023/9/25.
 */
@Serializable
data class PlaylistListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("playlists")
    val playlists: List<PlaylistData> = emptyList(),
    // /toplist 顶层键是 list，/top/playlist 是 playlists，两键互斥
    @SerialName("list")
    val list: List<PlaylistData> = emptyList(),
)

package me.wcy.music.discover.playlist.detail.bean

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import me.wcy.music.common.bean.SongData

/**
 * Created by wangchenyan.top on 2023/9/22.
 */
@Serializable
data class SongListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("songs")
    val songs: List<SongData> = emptyList()
)

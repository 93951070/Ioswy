package me.wcy.music.discover.playlist.detail.bean

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import me.wcy.music.common.bean.PlaylistData

/**
 * Created by wangchenyan.top on 2023/9/22.
 */
@Serializable
data class PlaylistDetailData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("playlist")
    val playlist: PlaylistData = PlaylistData(),
)

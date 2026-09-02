package me.wcy.music.discover.recommend.song.bean

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import me.wcy.music.common.bean.SongData

/**
 * Created by wangchenyan.top on 2023/9/6.
 */
@Serializable
data class RecommendSongListData(
    @SerialName("dailySongs")
    val dailySongs: List<SongData> = emptyList()
)

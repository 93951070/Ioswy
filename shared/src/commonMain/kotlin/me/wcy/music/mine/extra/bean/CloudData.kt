package me.wcy.music.mine.extra.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.SongData

/**
 * user/cloud 单条云盘歌曲：simpleSong 为歌曲对象，fileName 为上传文件名。
 */
@Serializable
data class CloudItem(
    @SerialName("simpleSong")
    val simpleSong: SongData = SongData(),
    @SerialName("fileName")
    val fileName: String = "",
    @SerialName("songId")
    val songId: Long = 0,
    @SerialName("addTime")
    val addTime: Long = 0
)

@Serializable
data class CloudData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("data")
    val data: List<CloudItem> = listOf()
)

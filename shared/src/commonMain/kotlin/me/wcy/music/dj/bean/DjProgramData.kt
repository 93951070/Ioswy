package me.wcy.music.dj.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.SongData

@Serializable
data class DjProgramData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("serialNum")
    val serialNum: Long = 0,
    @SerialName("coverUrl")
    val coverUrl: String = "",
    @SerialName("createTime")
    val createTime: Long = 0,
    // fixLegacyFields 会把响应里的 duration 统一改名为 dt
    @SerialName("dt")
    val duration: Long = 0,
    @SerialName("listenerCount")
    val listenerCount: Long = 0,
    @SerialName("mainSong")
    val mainSong: SongData = SongData()
)

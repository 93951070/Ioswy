package me.wcy.music.discover.fm.bean

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import me.wcy.music.common.bean.SongData

@Serializable
data class PersonalFmData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: List<SongData> = emptyList(),
)

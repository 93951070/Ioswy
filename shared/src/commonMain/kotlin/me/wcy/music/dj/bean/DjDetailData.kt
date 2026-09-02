package me.wcy.music.dj.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DjDetailData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("data")
    val data: DjRadioData = DjRadioData()
)

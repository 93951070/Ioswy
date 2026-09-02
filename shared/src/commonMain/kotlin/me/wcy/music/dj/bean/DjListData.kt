package me.wcy.music.dj.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DjListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("djRadios")
    val djRadios: List<DjRadioData> = emptyList(),
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("count")
    val count: Int = 0
)

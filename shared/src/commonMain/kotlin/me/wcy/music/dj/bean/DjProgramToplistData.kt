package me.wcy.music.dj.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DjProgramToplistData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("toplist")
    val toplist: List<Item> = emptyList()
) {
    @Serializable
    data class Item(
        @SerialName("rank")
        val rank: Int = 0,
        @SerialName("lastRank")
        val lastRank: Int = 0,
        @SerialName("program")
        val program: DjProgramData = DjProgramData()
    )
}

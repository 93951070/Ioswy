package me.wcy.music.discover.artist.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * /artist/list 热门歌手元素，仅保留发现页展示所需字段。
 */
@Serializable
data class HotArtistData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("alias")
    val alias: List<String> = emptyList(),
    @SerialName("musicSize")
    val musicSize: Int = 0
)

@Serializable
data class ArtistListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("artists")
    val artists: List<HotArtistData> = emptyList(),
    @SerialName("more")
    val more: Boolean = false
)

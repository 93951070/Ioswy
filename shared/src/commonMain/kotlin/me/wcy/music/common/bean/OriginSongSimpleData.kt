package me.wcy.music.common.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Created by wangchenyan.top on 2023/9/6.
 */
@Serializable
data class OriginSongSimpleData(
    @SerialName("songId")
    val songId: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("artists")
    val artists: List<ArtistData> = listOf(),
    @SerialName("albumMeta")
    val albumMeta: AlbumData = AlbumData()
)
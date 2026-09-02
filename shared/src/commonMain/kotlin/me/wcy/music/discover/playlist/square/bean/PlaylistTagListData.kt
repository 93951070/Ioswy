package me.wcy.music.discover.playlist.square.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by wangchenyan.top on 2023/9/26.
 */
@Serializable
data class PlaylistTagListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("tags")
    val tags: List<PlaylistTagData> = emptyList(),
)

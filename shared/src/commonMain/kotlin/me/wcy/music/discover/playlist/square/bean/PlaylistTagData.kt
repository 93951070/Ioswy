package me.wcy.music.discover.playlist.square.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PlaylistTagData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("activity")
    val activity: Boolean = false,
    @SerialName("hot")
    val hot: Boolean = false,
    @SerialName("position")
    val position: Int = 0,
    @SerialName("category")
    val category: Int = 0,
    @SerialName("createTime")
    val createTime: Long = 0,
    @SerialName("usedCount")
    val usedCount: Long = 0,
    @SerialName("type")
    val type: Int = 0
)
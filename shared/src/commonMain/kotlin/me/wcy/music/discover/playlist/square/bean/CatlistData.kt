package me.wcy.music.discover.playlist.square.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CatlistData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("all")
    val all: CatTag? = null,
    @SerialName("sub")
    val sub: List<CatTag> = emptyList(),
    @SerialName("categories")
    val categories: Map<String, String> = emptyMap(),
)

@Serializable
data class CatTag(
    @SerialName("name")
    val name: String = "",
    @SerialName("category")
    val category: Int = 0,
    @SerialName("hot")
    val hot: Boolean = false,
)

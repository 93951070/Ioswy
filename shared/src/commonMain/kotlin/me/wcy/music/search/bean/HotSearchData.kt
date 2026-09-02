package me.wcy.music.search.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class HotSearchData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: List<HotSearchWord> = emptyList(),
)

@Serializable
data class HotSearchWord(
    @SerialName("searchWord")
    val searchWord: String = "",
    @SerialName("score")
    val score: Long = 0,
    @SerialName("content")
    val content: String = "",
    @SerialName("iconUrl")
    val iconUrl: String? = null,
    @SerialName("iconType")
    val iconType: Int = 0,
)

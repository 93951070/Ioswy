package me.wcy.music.mine.collect.song.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by wangchenyan.top on 2024/3/21.
 */
@Serializable
data class CollectSongResult(
    @SerialName("status")
    val status: Int = 0,
    @SerialName("body")
    val body: Body = Body(),
) {
    @Serializable
    data class Body(
    @SerialName("code")
        val code: Int = 0,
    @SerialName("message")
        val message: String = "",
    )
}

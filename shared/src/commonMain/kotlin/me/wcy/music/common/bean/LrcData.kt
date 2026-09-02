package me.wcy.music.common.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LrcData(
    @SerialName("version")
    val version: Int = 0,
    @SerialName("lyric")
    val lyric: String = ""
) {
    fun isValid() = lyric.isNotEmpty()
}
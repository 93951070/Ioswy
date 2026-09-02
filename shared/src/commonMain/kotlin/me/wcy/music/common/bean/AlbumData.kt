package me.wcy.music.common.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.wcy.music.shared.util.CoverUtils.asLargeCover
import me.wcy.music.shared.util.CoverUtils.asSmallCover

/**
 * Created by wangchenyan.top on 2023/9/6.
 */
@Serializable
data class AlbumData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @Deprecated("Please use resized url")
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("tns")
    @Transient
    val tns: List<Any> = listOf(),
    @SerialName("pic_str")
    val picStr: String = "",
    @SerialName("pic")
    val pic: Long = 0
) {
    fun getSmallCover(): String {
        return picUrl.asSmallCover()
    }

    fun getLargeCover(): String {
        return picUrl.asLargeCover()
    }
}

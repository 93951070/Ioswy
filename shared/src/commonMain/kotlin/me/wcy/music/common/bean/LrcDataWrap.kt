package me.wcy.music.common.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by wangchenyan.top on 2023/9/18.
 */
@Serializable
data class LrcDataWrap(
    @SerialName("code")
    val code: Int = -1,
    @SerialName("lrc")
    val lrc: LrcData = LrcData()
)

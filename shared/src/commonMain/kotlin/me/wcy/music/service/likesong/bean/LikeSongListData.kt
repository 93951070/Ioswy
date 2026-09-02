package me.wcy.music.service.likesong.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by wangchenyan.top on 2024/3/21.
 */
@Serializable
data class LikeSongListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("ids")
    val ids: Set<Long> = emptySet()
)

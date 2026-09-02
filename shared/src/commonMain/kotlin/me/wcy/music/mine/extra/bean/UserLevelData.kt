package me.wcy.music.mine.extra.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserLevelData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: UserLevelInfo = UserLevelInfo()
)

@Serializable
data class UserLevelInfo(
    @SerialName("level")
    val level: Int = 0
)

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
    val level: Int = 0,
    @SerialName("progress")
    val progress: Double = 0.0,
    @SerialName("nowPlayCount")
    val nowPlayCount: Int = 0,
    @SerialName("nextPlayCount")
    val nextPlayCount: Int = 0,
    @SerialName("nowLoginCount")
    val nowLoginCount: Int = 0,
    @SerialName("nextLoginCount")
    val nextLoginCount: Int = 0,
    @SerialName("info")
    val info: String = ""
)

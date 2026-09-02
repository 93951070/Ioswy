package me.wcy.music.dj.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DjRadioData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("dj")
    val dj: DjData = DjData(),
    @SerialName("subCount")
    val subCount: Long = 0,
    @SerialName("programCount")
    val programCount: Long = 0,
    @SerialName("playCount")
    val playCount: Long = 0,
    @SerialName("desc")
    val desc: String = "",
    @SerialName("category")
    val category: String = "",
    @SerialName("categoryId")
    val categoryId: Long = 0,
    @SerialName("createTime")
    val createTime: Long = 0,
    @SerialName("subed")
    val subed: Boolean = false
)

@Serializable
data class DjData(
    @SerialName("userId")
    val userId: Long = 0,
    @SerialName("nickname")
    val nickname: String = "",
    @SerialName("avatarUrl")
    val avatarUrl: String = "",
    @SerialName("signature")
    val signature: String = "",
    @SerialName("brand")
    val brand: String = ""
)

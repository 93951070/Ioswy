package me.wcy.music.common.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Created by wangchenyan.top on 2023/9/6.
 */
@Serializable
data class ArtistData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("tns")
    @Transient
    val tns: List<Any> = listOf(),
    @SerialName("alias")
    @Transient
    val alias: List<Any> = listOf()
)
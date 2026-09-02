package me.wcy.music.common.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Created by wangchenyan.top on 2023/9/6.
 */
@Serializable
data class QualityData(
    @SerialName("br")
    val br: Int = 0,
    @SerialName("fid")
    val fid: Int = 0,
    @SerialName("size")
    val size: Int = 0,
    @SerialName("vd")
    val vd: Int = 0,
    @SerialName("sr")
    val sr: Int = 0
)
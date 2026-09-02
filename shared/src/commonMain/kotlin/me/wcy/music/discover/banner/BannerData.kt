package me.wcy.music.discover.banner

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import me.wcy.music.common.bean.SongData

@Serializable
data class BannerData(
    @SerialName("pic")
    val pic: String = "",
    @SerialName("targetId")
    val targetId: Long = 0,
    @SerialName("targetType")
    val targetType: Int = 0,
    @SerialName("titleColor")
    val titleColor: String = "",
    @SerialName("typeTitle")
    val typeTitle: String = "",
    @SerialName("url")
    val url: String = "",
    @SerialName("exclusive")
    val exclusive: Boolean = false,
    @SerialName("encodeId")
    val encodeId: String = "",
    @SerialName("song")
    val song: SongData? = null,
    @SerialName("bannerId")
    val bannerId: String = "",
    @SerialName("alg")
    val alg: String = "",
    @SerialName("scm")
    val scm: String = "",
    @SerialName("requestId")
    val requestId: String = "",
    @SerialName("showAdTag")
    val showAdTag: Boolean = false,
    @SerialName("s_ctrp")
    val sCtrp: String = "",
    @SerialName("bannerBizType")
    val bannerBizType: String = ""
)
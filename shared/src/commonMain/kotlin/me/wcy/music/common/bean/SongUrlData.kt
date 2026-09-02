package me.wcy.music.common.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SongUrlData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("url")
    val url: String = "",
    @SerialName("br")
    val br: Int = 0,
    @SerialName("size")
    val size: Int = 0,
    @SerialName("md5")
    val md5: String = "",
    @SerialName("code")
    val code: Int = 0,
    @SerialName("expi")
    val expi: Int = 0,
    @SerialName("type")
    val type: String = "",
    @SerialName("gain")
    val gain: Double = 0.0,
    @SerialName("peak")
    val peak: Int = 0,
    @SerialName("fee")
    val fee: Int = 0,
    @SerialName("payed")
    val payed: Int = 0,
    @SerialName("flag")
    val flag: Int = 0,
    @SerialName("canExtend")
    val canExtend: Boolean = false,
    @SerialName("level")
    val level: String = "",
    @SerialName("encodeType")
    val encodeType: String = "",
    @SerialName("urlSource")
    val urlSource: Int = 0,
    @SerialName("rightSource")
    val rightSource: Int = 0,
    @SerialName("time")
    val time: Int = 0
)
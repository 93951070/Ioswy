package me.wcy.music.account.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class QrCodeKeyData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("unikey")
    val unikey: String = ""
)
package me.wcy.music.account.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class QrCodeData(
    @SerialName("qrurl")
    val qrurl: String = ""
)
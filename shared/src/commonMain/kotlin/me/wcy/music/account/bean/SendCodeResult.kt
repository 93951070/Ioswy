package me.wcy.music.account.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SendCodeResult(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String = "",
)

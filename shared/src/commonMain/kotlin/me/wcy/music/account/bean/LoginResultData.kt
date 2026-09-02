package me.wcy.music.account.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LoginResultData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String = "",
    @SerialName("nickname")
    val nickname: String = "",
    @SerialName("avatarUrl")
    val avatarUrl: String = "",
    @SerialName("cookie")
    val cookie: String = ""
) {
    companion object {
        // 二维码不存在或已过期
        const val STATUS_INVALID = 800

        // 等待扫码
        const val STATUS_NOT_SCAN = 801

        // 授权中
        const val STATUS_SCANNING = 802

        // 授权登陆成功，包含 cookie
        const val STATUS_SUCCESS = 803
    }
}
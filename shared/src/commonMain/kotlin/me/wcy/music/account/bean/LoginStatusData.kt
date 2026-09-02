package me.wcy.music.account.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LoginStatusData(
    @SerialName("data")
    val `data`: Data = Data()
) {
    @Serializable
    data class Data(
    @SerialName("code")
        val code: Int = 0,
    @SerialName("account")
        val account: Account = Account(),
    @SerialName("profile")
        val profile: ProfileData? = null
    ) {
        @Serializable
        data class Account(
    @SerialName("id")
            val id: Long = 0,
    @SerialName("userName")
            val userName: String = "",
    @SerialName("type")
            val type: Int = 0,
    @SerialName("status")
            val status: Int = 0,
    @SerialName("whitelistAuthority")
            val whitelistAuthority: Int = 0,
    @SerialName("createTime")
            val createTime: Long = 0,
    @SerialName("tokenVersion")
            val tokenVersion: Int = 0,
    @SerialName("ban")
            val ban: Int = 0,
    @SerialName("baoyueVersion")
            val baoyueVersion: Int = 0,
    @SerialName("donateVersion")
            val donateVersion: Int = 0,
    @SerialName("vipType")
            val vipType: Int = 0,
    @SerialName("anonimousUser")
            val anonimousUser: Boolean = false,
    @SerialName("paidFee")
            val paidFee: Boolean = false
        )
    }
}
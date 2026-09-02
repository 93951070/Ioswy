package me.wcy.music.account.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Created by wangchenyan.top on 2023/8/28.
 */
@Serializable
data class ProfileData(
    @SerialName("userId")
    val userId: Long = 0,
    @SerialName("userType")
    val userType: Int = 0,
    @SerialName("nickname")
    val nickname: String = "",
    @SerialName("avatarImgId")
    val avatarImgId: Long = 0,
    @SerialName("avatarUrl")
    val avatarUrl: String = "",
    @SerialName("backgroundImgId")
    val backgroundImgId: Long = 0,
    @SerialName("backgroundUrl")
    val backgroundUrl: String = "",
    @SerialName("signature")
    val signature: String = "",
    @SerialName("createTime")
    val createTime: Long = 0,
    @SerialName("userName")
    val userName: String = "",
    @SerialName("accountType")
    val accountType: Int = 0,
    @SerialName("shortUserName")
    val shortUserName: String = "",
    @SerialName("birthday")
    val birthday: Long = 0,
    @SerialName("authority")
    val authority: Int = 0,
    @SerialName("gender")
    val gender: Int = 0,
    @SerialName("accountStatus")
    val accountStatus: Int = 0,
    @SerialName("province")
    val province: Int = 0,
    @SerialName("city")
    val city: Int = 0,
    @SerialName("authStatus")
    val authStatus: Int = 0,
    @SerialName("defaultAvatar")
    val defaultAvatar: Boolean = false,
    @SerialName("djStatus")
    val djStatus: Int = 0,
    @SerialName("locationStatus")
    val locationStatus: Int = 0,
    @SerialName("vipType")
    val vipType: Int = 0,
    @SerialName("followed")
    val followed: Boolean = false,
    @SerialName("mutual")
    val mutual: Boolean = false,
    @SerialName("authenticated")
    val authenticated: Boolean = false,
    @SerialName("lastLoginTime")
    val lastLoginTime: Long = 0,
    @SerialName("lastLoginIP")
    val lastLoginIP: String = "",
    @SerialName("viptypeVersion")
    val viptypeVersion: Long = 0,
    @SerialName("authenticationTypes")
    val authenticationTypes: Int = 0,
    @SerialName("anchor")
    val anchor: Boolean = false
)

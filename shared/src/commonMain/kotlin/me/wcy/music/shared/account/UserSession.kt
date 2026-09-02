package me.wcy.music.shared.account

import kotlinx.coroutines.flow.StateFlow
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.shared.net.NetResult

/**
 * 登录会话抽象：shared 层消费的登录态最小面。
 * Android 壳由 UserServiceImpl 桥接实现，iOS 壳由本地存储实现。
 */
interface UserSession {
    val profile: StateFlow<ProfileData?>

    fun getCookie(): String
    fun isLogin(): Boolean
    fun getUserId(): Long

    /** 用 cookie 换取并保存 profile */
    suspend fun login(cookie: String): NetResult<ProfileData>

    suspend fun logout()
}

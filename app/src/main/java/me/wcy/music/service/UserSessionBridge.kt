package me.wcy.music.service

import kotlinx.coroutines.flow.StateFlow
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.account.service.UserService
import me.wcy.music.shared.account.UserSession
import me.wcy.music.shared.net.NetResult
import top.wangchenyan.common.model.CommonResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionBridge @Inject constructor(
    private val userService: UserService
) : UserSession {
    override val profile: StateFlow<ProfileData?> = userService.profile

    override fun getCookie(): String = userService.getCookie()
    override fun isLogin(): Boolean = userService.isLogin()
    override fun getUserId(): Long = userService.getUserId()

    override suspend fun login(cookie: String): NetResult<ProfileData> =
        userService.login(cookie).toNetResult()

    override suspend fun logout() = userService.logout()

    private fun <T> CommonResult<T>.toNetResult(): NetResult<T> =
        NetResult(code = code, msg = msg, data = data)
}

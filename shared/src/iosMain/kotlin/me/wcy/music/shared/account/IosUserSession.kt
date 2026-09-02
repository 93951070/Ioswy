package me.wcy.music.shared.account

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.shared.net.AccountNet
import me.wcy.music.shared.net.NetResult
import me.wcy.music.shared.net.SharedNet
import platform.Foundation.NSUserDefaults

/**
 * iOS 登录会话：cookie/profile 存 NSUserDefaults，
 * login 流程对齐 Android UserServiceImpl（cookie -> getLoginStatus -> 存 profile）。
 * 同时把 cookie 同步进 SharedNet，让后续 API 请求带上登录态。
 */
class IosUserSession : UserSession {

    private val defs = NSUserDefaults.standardUserDefaults

    private val _profile = MutableStateFlow(loadProfile())
    override val profile: StateFlow<ProfileData?> = _profile.asStateFlow()

    init {
        SharedNet.cookie = getCookie()
    }

    override fun getCookie(): String = defs.stringForKey(KEY_COOKIE) ?: ""

    override fun isLogin(): Boolean = _profile.value != null

    override fun getUserId(): Long = _profile.value?.userId ?: 0

    override suspend fun login(cookie: String): NetResult<ProfileData> {
        saveCookie(cookie)
        return try {
            val status = AccountNet.getLoginStatus()
            val profile = status.data.profile
            if (status.data.account.status == 0 && profile != null) {
                _profile.value = profile
                defs.setObjectForKey(
                    SharedJson.encodeToString(ProfileData.serializer(), profile),
                    KEY_PROFILE
                )
                NetResult(code = 200, data = profile)
            } else {
                saveCookie("")
                NetResult(code = status.data.account.status, msg = "login fail")
            }
        } catch (e: Throwable) {
            saveCookie("")
            NetResult(code = -1, msg = e.message)
        }
    }

    override suspend fun logout() {
        saveCookie("")
        defs.removeObjectForKey(KEY_PROFILE)
        _profile.value = null
    }

    private fun saveCookie(cookie: String) {
        SharedNet.cookie = cookie
        if (cookie.isEmpty()) {
            defs.removeObjectForKey(KEY_COOKIE)
        } else {
            defs.setObjectForKey(cookie, KEY_COOKIE)
        }
    }

    private fun loadProfile(): ProfileData? = runCatching {
        defs.stringForKey(KEY_PROFILE)?.let {
            SharedJson.decodeFromString(ProfileData.serializer(), it)
        }
    }.getOrNull()

    private companion object {
        const val KEY_COOKIE = "ios_user_session_cookie"
        const val KEY_PROFILE = "ios_user_session_profile"
    }
}

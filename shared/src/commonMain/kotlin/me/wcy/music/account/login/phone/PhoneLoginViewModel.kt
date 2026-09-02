package me.wcy.music.account.login.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.shared.account.UserSession
import me.wcy.music.shared.net.AccountNet
import me.wcy.music.shared.net.NetResult

/**
 * Created by wangchenyan.top on 2024/1/3.
 */
class PhoneLoginViewModel constructor(
    private val userSession: UserSession
) : ViewModel() {
    private val _sendPhoneCodeCountdown = MutableStateFlow(0)
    val sendPhoneCodeCountdown: StateFlow<Int> = _sendPhoneCodeCountdown.asStateFlow()

    suspend fun sendPhoneCode(phone: String): NetResult<Unit> {
        if (_sendPhoneCodeCountdown.value > 0) {
            return NetResult(code = -1)
        }
        val res = kotlin.runCatching {
            AccountNet.sendPhoneCode(phone)
        }
        return if (res.isSuccess) {
            val data = res.getOrThrow()
            if (data.code == 200) {
                viewModelScope.launch {
                    _sendPhoneCodeCountdown.value = 30
                    repeat(Int.MAX_VALUE) {
                        delay(1000)
                        _sendPhoneCodeCountdown.value = _sendPhoneCodeCountdown.value - 1
                        if (_sendPhoneCodeCountdown.value == 0) {
                            return@launch
                        }
                    }
                }
                NetResult(code = 200, data = Unit)
            } else {
                NetResult(code = data.code, msg = data.message)
            }
        } else {
            NetResult(code = -1, msg = res.exceptionOrNull()?.message)
        }
    }

    suspend fun phoneLogin(phone: String, code: String): NetResult<Unit> {
        val loginRes = kotlin.runCatching {
            AccountNet.phoneLogin(phone, code)
        }
        return if (loginRes.isSuccess) {
            val data = loginRes.getOrNull()
            if (data?.code == 200) {
                val getProfileRes = userSession.login(data.cookie)
                if (getProfileRes.isSuccessWithData()) {
                    NetResult(code = 200, data = Unit)
                } else {
                    NetResult(code = getProfileRes.code, msg = getProfileRes.msg)
                }
            } else {
                NetResult(code = data?.code ?: -1, msg = data?.message)
            }
        } else {
            var result = NetResult<Unit>(code = -1, msg = loginRes.exceptionOrNull()?.message)
            if (result.code == -462) {
                result = result.copy(msg = "登录失败，请更新服务端版本或稍后重试")
            }
            result
        }
    }
}

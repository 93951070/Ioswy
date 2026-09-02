package me.wcy.music.account.login.qrcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.account.bean.LoginResultData
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.shared.account.UserSession
import me.wcy.music.shared.net.AccountNet
import me.wcy.music.shared.net.NetResult
import me.wcy.music.shared.net.apiCall

/**
 * Created by wangchenyan.top on 2023/8/28.
 */
class QrcodeLoginViewModel constructor(
    private val userSession: UserSession
) : ViewModel() {
    private var qrCodeKey = ""
    private val _qrUrl = MutableStateFlow<String?>(null)
    val qrUrl: StateFlow<String?> = _qrUrl.asStateFlow()
    private val _loginStatus = MutableStateFlow<LoginResultData?>(null)
    val loginStatus: StateFlow<LoginResultData?> = _loginStatus.asStateFlow()
    private var job: Job? = null

    fun getLoginQrCode() {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.Default) {
            qrCodeKey = ""
            _qrUrl.value = null
            _loginStatus.value = null
            val getKeyRes = apiCall {
                AccountNet.getQrCodeKey()
            }
            if (getKeyRes.isSuccessWithData().not()) {
                _loginStatus.value = LoginResultData(-1)
                return@launch
            }
            val keyData = getKeyRes.getDataOrThrow()
            qrCodeKey = keyData.unikey
            val getQrCodeRes = apiCall {
                AccountNet.getLoginQrCode(qrCodeKey)
            }
            if (getQrCodeRes.isSuccessWithData().not()) {
                _loginStatus.value = LoginResultData(-1)
                return@launch
            }
            val qrCodeData = getQrCodeRes.getDataOrThrow()
            _qrUrl.value = qrCodeData.qrurl

            while (true) {
                kotlin.runCatching {
                    AccountNet.checkLoginStatus(qrCodeKey)
                }.onSuccess { status ->
                    _loginStatus.value = status
                    if (status.code == LoginResultData.STATUS_NOT_SCAN
                        || status.code == LoginResultData.STATUS_SCANNING
                    ) {
                        delay(3000)
                    } else {
                        return@launch
                    }
                }.onFailure {
                    _loginStatus.value = LoginResultData(-1, it.message ?: "")
                    return@launch
                }
            }
        }
    }

    suspend fun loginWithCookie(cookie: String): NetResult<ProfileData> =
        userSession.login(cookie)
}

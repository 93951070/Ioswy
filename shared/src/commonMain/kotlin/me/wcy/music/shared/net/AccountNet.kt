package me.wcy.music.shared.net

import me.wcy.music.account.bean.LoginResultData
import me.wcy.music.account.bean.LoginStatusData
import me.wcy.music.account.bean.QrCodeData
import me.wcy.music.account.bean.QrCodeKeyData
import me.wcy.music.account.bean.SendCodeResult
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean

/**
 * 账户相关接口。
 */
object AccountNet {

    suspend fun sendPhoneCode(
        phone: String,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): SendCodeResult {
        return SharedJson.decodeBean(SharedNet.get(
                "captcha/sent",
                params = listOf(
                    "phone" to phone,
                    "timestamp" to timestamp
                )
            )
        )
    }

    suspend fun phoneLogin(
        phone: String,
        captcha: String,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): LoginResultData {
        return SharedJson.decodeBean(SharedNet.get(
                "login/cellphone",
                params = listOf(
                    "phone" to phone,
                    "captcha" to captcha,
                    "timestamp" to timestamp
                )
            )
        )
    }

    suspend fun getQrCodeKey(
        timestamp: Long = SharedNet.currentTimeMillis()
    ): NetResult<QrCodeKeyData> {
        return SharedJson.decodeBean(SharedNet.get(
                "login/qr/key",
                params = listOf(
                    "timestamp" to timestamp
                )
            )
        )
    }

    suspend fun getLoginQrCode(
        key: String,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): NetResult<QrCodeData> {
        return SharedJson.decodeBean(SharedNet.get(
                "login/qr/create",
                params = listOf(
                    "key" to key,
                    "timestamp" to timestamp
                )
            )
        )
    }

    suspend fun checkLoginStatus(
        key: String,
        timestamp: Long = SharedNet.currentTimeMillis(),
        noCookie: Boolean = true
    ): LoginResultData {
        return SharedJson.decodeBean(SharedNet.get(
                "login/qr/check",
                params = listOf(
                    "key" to key,
                    "timestamp" to timestamp,
                    "noCookie" to noCookie
                )
            )
        )
    }

    suspend fun getLoginStatus(
        timestamp: Long = SharedNet.currentTimeMillis()
    ): LoginStatusData {
        return SharedJson.decodeBean(SharedNet.post(
                "login/status",
                params = listOf(
                    "timestamp" to timestamp
                )
            )
        )
    }
}

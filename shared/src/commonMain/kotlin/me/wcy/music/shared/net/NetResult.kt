package me.wcy.music.shared.net

import kotlinx.serialization.Serializable

/**
 * 对齐 top.wangchenyan.common.net.NetResult 的跨平台复刻，供 Ktor API 层使用。
 */
@Serializable
data class NetResult<T>(
    val code: Int = 0,
    val msg: String? = null,
    val data: T? = null,
    val total: Int = 0
) {
    fun isSuccess(): Boolean = code == 200

    fun isSuccessWithData(): Boolean = isSuccess() && data != null

    fun getDataOrThrow(): T {
        check(isSuccessWithData()) { "code = $code, msg = $msg" }
        @Suppress("UNCHECKED_CAST")
        return data as T
    }
}

/**
 * 对齐 top.wangchenyan.common.net.apiCall：异常统一转成失败的 NetResult，不向上抛出。
 */
suspend fun <T> apiCall(block: suspend () -> NetResult<T>): NetResult<T> {
    return try {
        block()
    } catch (e: Throwable) {
        NetResult(code = -1, msg = e.message)
    }
}

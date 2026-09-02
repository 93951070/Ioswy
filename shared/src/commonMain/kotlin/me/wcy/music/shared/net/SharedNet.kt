package me.wcy.music.shared.net

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock
import me.wcy.music.common.bean.SharedJson

/**
 * 跨平台网络入口。引擎由各 target 依赖提供（Android=OkHttp, iOS=Darwin）。
 * 域名与 Cookie 由各端启动时注入。
 */
const val DEFAULT_BASE_URL = "https://music.wangchenyan.top"

object SharedNet {
    var baseUrl: String = DEFAULT_BASE_URL
    var cookie: String = ""

    private val client by lazy {
        HttpClient {
            expectSuccess = false
            install(ContentNegotiation) {
                json(SharedJson)
            }
        }
    }

    suspend fun get(
        path: String,
        params: List<Pair<String, Any?>> = emptyList()
    ): String {
        val text = client.get("$baseUrl/$path") {
            params.forEach { (k, v) -> v?.let { parameter(k, it) } }
            if (cookie.isNotEmpty()) {
                header(HttpHeaders.Cookie, cookie)
            }
        }.bodyAsText()
        return text
    }

    suspend fun post(
        path: String,
        params: List<Pair<String, Any?>> = emptyList()
    ): String {
        val text = client.post("$baseUrl/$path") {
            params.forEach { (k, v) -> v?.let { parameter(k, it) } }
            if (cookie.isNotEmpty()) {
                header(HttpHeaders.Cookie, cookie)
            }
        }.bodyAsText()
        return text
    }

    fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}

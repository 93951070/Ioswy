package me.wcy.music.common.bean

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

val SharedJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    encodeDefaults = false
}

/**
 * 网易云部分接口返回旧字段结构（artists/album/duration、picUrl/playcount），
 * kotlinx-serialization 无 alternate 支持，反序列化前统一重命名。
 *
 * 必须用 JsonElement 预处理而非 @Serializable(with = JsonTransformingSerializer)：
 * 后者与插件生成的 serializer 存在全局初始化循环依赖，K/N 上 get-descriptor
 * 无限递归导致 iOS 发现页加载时爆栈崩溃（Android JVM 不触发）。
 */
inline fun <reified T> Json.decodeBean(string: String): T =
    decodeFromJsonElement(fixLegacyFields(parseToJsonElement(string)))

fun fixLegacyFields(element: JsonElement): JsonElement = when (element) {
    is JsonObject -> JsonObject(
        element.entries.associate { (key, value) ->
            val newKey = when {
                key == "artists" -> "ar"
                key == "album" -> "al"
                key == "duration" -> "dt"
                key == "playcount" -> "playCount"
                // picUrl 仅在歌单对象（有 userId/trackCount 特征）改为 coverImgUrl，
                // 避免误伤 AlbumData.picUrl
                key == "picUrl" && ("userId" in element || "trackCount" in element) -> "coverImgUrl"
                else -> key
            }
            newKey to fixLegacyFields(value)
        }
    )
    is JsonArray -> JsonArray(element.map { fixLegacyFields(it) })
    else -> element
}

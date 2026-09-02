package me.wcy.music.common.bean

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

val SharedJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    encodeDefaults = false
}

/**
 * 网易云部分接口返回旧字段结构（artists/album/duration、picUrl/playcount），
 * kotlinx-serialization 无 alternate 支持，反序列化前统一重命名。
 */
object SongDataJson : JsonTransformingSerializer<SongData>(SongData.serializer()) {
    private val rename = mapOf("artists" to "ar", "album" to "al", "duration" to "dt")

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element !is JsonObject) return element
        return JsonObject(element.entries.associate { (k, v) -> (rename[k] ?: k) to v })
    }
}

object PlaylistDataJson : JsonTransformingSerializer<PlaylistData>(PlaylistData.serializer()) {
    private val rename = mapOf("picUrl" to "coverImgUrl", "playcount" to "playCount")

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element !is JsonObject) return element
        return JsonObject(element.entries.associate { (k, v) -> (rename[k] ?: k) to v })
    }
}

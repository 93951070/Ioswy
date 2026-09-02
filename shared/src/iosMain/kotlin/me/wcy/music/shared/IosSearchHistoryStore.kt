package me.wcy.music.shared

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.search.SearchHistoryStore
import platform.Foundation.NSUserDefaults

/**
 * 搜索历史 iOS 实现：List<String> 序列化成 JSON 存 NSUserDefaults。
 */
class IosSearchHistoryStore : SearchHistoryStore {

    private val defs = NSUserDefaults.standardUserDefaults

    override fun loadHistory(): List<String> = runCatching {
        defs.stringForKey(KEY)?.let {
            SharedJson.decodeFromString(ListSerializer(String.serializer()), it)
        }
    }.getOrNull() ?: emptyList()

    override fun saveHistory(keywords: List<String>) {
        defs.setObjectForKey(
            SharedJson.encodeToString(ListSerializer(String.serializer()), keywords),
            KEY
        )
    }

    private companion object {
        const val KEY = "ios_search_history"
    }
}

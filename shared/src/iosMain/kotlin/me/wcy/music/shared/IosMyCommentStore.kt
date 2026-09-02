package me.wcy.music.shared

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.discover.comment.MyCommentStore
import me.wcy.music.discover.comment.bean.CommentItem
import platform.Foundation.NSUserDefaults

/**
 * 自己发表的评论 iOS 实现：songId -> 评论数组 序列化成 JSON 存 NSUserDefaults，
 * 语义对齐 Android MyCommentStoreImpl（新评论头部插入、按 commentId 去重、上限 50 条）。
 */
object IosMyCommentStore : MyCommentStore {

    private const val KEY = "ios_my_comments"
    private val defs = NSUserDefaults.standardUserDefaults
    private val serializer = MapSerializer(
        String.serializer(),
        ListSerializer(CommentItem.serializer())
    )

    override fun load(songId: Long): List<CommentItem> {
        if (songId <= 0) return emptyList()
        return loadAll()[songId] ?: emptyList()
    }

    override fun prepend(songId: Long, comment: CommentItem) {
        if (songId <= 0) return
        val list = (listOf(comment) + (loadAll()[songId] ?: emptyList()))
            .distinctBy { it.commentId }
            .take(50)
        defs.setObject(
            SharedJson.encodeToString(serializer, mapOf(songId.toString() to list)),
            forKey = KEY
        )
    }

    private fun loadAll(): Map<Long, List<CommentItem>> = runCatching {
        defs.stringForKey(KEY)?.let {
            SharedJson.decodeFromString(serializer, it)
                .mapKeys { (key, _) -> key.toLongOrNull() ?: 0L }
        }
    }.getOrNull() ?: emptyMap()
}

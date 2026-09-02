package me.wcy.music.discover.comment

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.wcy.music.discover.comment.bean.CommentItem
import top.wangchenyan.common.CommonApp

/**
 * 自己发表的评论本地缓存，SharedPreferences 实现。
 * 接口 MyCommentStore 已迁移至 shared/commonMain，本文件为宿主平台实现。
 */
object MyCommentStoreImpl : MyCommentStore {
    private const val PREFS = "my_comments"
    private val gson = Gson()
    private val type = object : TypeToken<List<CommentItem>>() {}.type

    private fun prefs() =
        CommonApp.app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun load(songId: Long): List<CommentItem> {
        if (songId <= 0) return emptyList()
        return runCatching {
            gson.fromJson<List<CommentItem>>(prefs().getString(songId.toString(), null), type)
                ?: emptyList()
        }.getOrDefault(emptyList())
    }

    override fun prepend(songId: Long, comment: CommentItem) {
        if (songId <= 0) return
        val list = (listOf(comment) + load(songId)).distinctBy { it.commentId }.take(50)
        prefs().edit().putString(songId.toString(), gson.toJson(list)).apply()
    }
}

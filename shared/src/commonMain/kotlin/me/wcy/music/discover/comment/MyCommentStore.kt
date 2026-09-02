package me.wcy.music.discover.comment

import me.wcy.music.discover.comment.bean.CommentItem

/**
 * 自己发表的评论本地缓存抽象，由宿主平台实现（Android 侧基于 SharedPreferences）。
 * 网易云上游评论列表对新发评论有收录延迟，用本地缓存保证发过的评论重进不丢。
 */
interface MyCommentStore {
    fun load(songId: Long): List<CommentItem>

    fun prepend(songId: Long, comment: CommentItem)
}

/**
 * 空实现，作为 CommentViewModel 构造默认值，保证无参构造可用。
 */
object EmptyMyCommentStore : MyCommentStore {
    override fun load(songId: Long): List<CommentItem> = emptyList()

    override fun prepend(songId: Long, comment: CommentItem) = Unit
}

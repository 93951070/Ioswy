package me.wcy.music.mine.extra.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * msg/private、msg/comments、msg/notices 三接口的消息条目。
 * 字段名存在差异：私信摘要在 lastMsg、时间是 createTime；
 * 评论摘要在 notice/comment.content、时间是 time；通知摘要在 noticeMsg、时间是 time。
 * 统一通过 message()/timestamp() 取值，未登录或空数据时字段缺省。
 */
@Serializable
data class MsgUser(
    @SerialName("userId")
    val userId: Long = 0,
    @SerialName("nickname")
    val nickname: String = "",
    @SerialName("avatarUrl")
    val avatarUrl: String = ""
)

@Serializable
data class MsgCommentData(
    @SerialName("content")
    val content: String = ""
)

@Serializable
data class MsgItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("user")
    val user: MsgUser? = null,
    @SerialName("lastMsg")
    val lastMsg: String = "",
    @SerialName("noticeMsg")
    val noticeMsg: String = "",
    @SerialName("notice")
    val notice: String = "",
    @SerialName("comment")
    val comment: MsgCommentData? = null,
    @SerialName("time")
    val time: Long = 0,
    @SerialName("createTime")
    val createTime: Long = 0
) {
    fun message(): String {
        return lastMsg.ifBlank {
            noticeMsg.ifBlank {
                notice.ifBlank { comment?.content ?: "" }
            }
        }
    }

    fun timestamp(): Long = if (createTime > 0) createTime else time
}

@Serializable
data class MsgData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("more")
    val more: Boolean = false,
    @SerialName("newMsgCount")
    val newMsgCount: Int = 0,
    @SerialName("msgs")
    val msgs: List<MsgItem> = listOf()
)

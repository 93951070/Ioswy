package me.wcy.music.mine.extra.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.wcy.music.common.bean.SharedJson

/**
 * msg/private、msg/comments、msg/notices 三接口的消息条目。
 * 字段名存在差异：私信摘要在 lastMsg、时间是 createTime；
 * 评论摘要在 notice/comment.content、时间是 time；通知摘要在 noticeMsg、时间是 time。
 * 统一通过 message()/timestamp() 取值，未登录或空数据时字段缺省。
 */
/**
 * msg/private、msg/comments、msg/notices 三接口的消息条目。
 * 私信列表实测结构：id=lastMsgId、时间=lastMsgTime、头像来自 fromUser；
 * lastMsg 是 JSON 串（{"msg":"正文",...}）；评论摘要在 notice/comment.content；通知在 noticeMsg。
 * 统一通过 message()/timestamp() 取值。
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
    // 私信列表的会话标识与时间在 lastMsgId/lastMsgTime 上
    @SerialName("lastMsgId")
    val lastMsgId: Long = 0,
    @SerialName("lastMsgTime")
    val lastMsgTime: Long = 0,
    @SerialName("user")
    val user: MsgUser? = null,
    // 私信对端用户信息
    @SerialName("fromUser")
    val fromUser: MsgUser? = null,
    @SerialName("lastMsg")
    val lastMsg: String = "",
    // msg/private/history 的正文在 msg 字段（JSON 串），列表接口在 lastMsg
    @SerialName("msg")
    val msg: String = "",
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
        return parseJsonMsg(lastMsg).ifBlank {
            parseJsonMsg(msg).ifBlank {
                noticeMsg.ifBlank {
                    notice.ifBlank { comment?.content ?: "" }
                }
            }
        }
    }

    // 私信正文是 JSON 串（{"msg":"内容",...}），解析取正文；纯文本原样返回
    private fun parseJsonMsg(raw: String): String = if (raw.startsWith("{")) {
        runCatching {
            SharedJson.parseToJsonElement(raw).jsonObject["msg"]
                ?.jsonPrimitive?.content ?: raw
        }.getOrDefault(raw)
    } else {
        raw
    }

    /** 会话对方的用户信息（私信用 fromUser，通知类无） */
    fun peer(): MsgUser? = fromUser ?: user

    fun timestamp(): Long {
        return listOf(createTime, lastMsgTime, time).firstOrNull { it > 0 } ?: 0
    }
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

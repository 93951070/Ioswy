package me.wcy.music.discover.comment.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CommentData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("total")
    val total: Long = 0,
    @SerialName("more")
    val more: Boolean = false,
    @SerialName("hotComments")
    val hotComments: List<CommentItem> = emptyList(),
    @SerialName("comments")
    val comments: List<CommentItem> = emptyList(),
)

@Serializable
data class CommentItem(
    @SerialName("user")
    val user: CommentUser? = null,
    @SerialName("commentId")
    val commentId: Long = 0,
    @SerialName("content")
    val content: String = "",
    @SerialName("time")
    val time: Long = 0,
    @SerialName("timeStr")
    val timeStr: String = "",
    @SerialName("likedCount")
    val likedCount: Long = 0,
    @SerialName("liked")
    val liked: Boolean = false,
)

@Serializable
data class CommentUser(
    @SerialName("userId")
    val userId: Long = 0,
    @SerialName("nickname")
    val nickname: String = "",
    @SerialName("avatarUrl")
    val avatarUrl: String = "",
)

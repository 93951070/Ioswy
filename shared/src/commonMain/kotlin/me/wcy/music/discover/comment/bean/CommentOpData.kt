package me.wcy.music.discover.comment.bean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CommentOpData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("comment")
    val comment: CommentItem? = null,
)

package me.wcy.music.shared.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.discover.comment.bean.CommentData
import me.wcy.music.discover.comment.bean.CommentItem

@Serializable
data class CommentFloorData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CommentFloorInner? = null,
)

@Serializable
data class CommentFloorInner(
    @SerialName("ownerComment")
    val ownerComment: CommentItem? = null,
    @SerialName("currentComment")
    val currentComment: CommentItem? = null,
    @SerialName("comments")
    val comments: List<CommentItem> = emptyList(),
    @SerialName("bestComments")
    val bestComments: List<CommentItem> = emptyList(),
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("totalCount")
    val totalCount: Long = 0,
    @SerialName("time")
    val time: Long = 0,
)

@Serializable
data class CommentNewData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CommentNewInner? = null,
)

@Serializable
data class CommentNewInner(
    @SerialName("commentsTitle")
    val commentsTitle: String = "",
    @SerialName("comments")
    val comments: List<CommentItem> = emptyList(),
    @SerialName("hotComments")
    val hotComments: List<CommentItem>? = null,
    @SerialName("totalCount")
    val totalCount: Long = 0,
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("cursor")
    val cursor: String = "",
)

/**
 * 评论扩展接口：热评、楼中楼、新版统一评论。
 *
 * type 取值：0 歌曲 / 2 歌单 / 4 电台 / 5 专辑 / 3 MV。
 */
object CommentExtraNet {

    /**
     * 热评。响应顶层为 topComments/hasMore/hotComments/total/code，
     * 与 CommentData 结构兼容（comments 缺省为空，hasMore 字段被忽略）。
     */
    suspend fun getHotComments(
        id: Long,
        type: Int = 0,
        limit: Int = 30,
    ): CommentData {
        return SharedJson.decodeBean(SharedNet.get(
                "comment/hot",
                params = listOf(
                    "id" to id,
                    "type" to type,
                    "limit" to limit
                )
            )
        )
    }

    /**
     * 楼中楼评论。parentCommentId 必须是真实存在的评论 id，否则返回 code 400。
     */
    suspend fun getCommentFloor(
        parentCommentId: Long,
        id: Long,
        type: Int = 0,
        limit: Int = 20,
        time: Long? = null,
    ): CommentFloorData {
        return SharedJson.decodeBean(SharedNet.get(
                "comment/floor",
                params = listOf(
                    "parentCommentId" to parentCommentId,
                    "id" to id,
                    "type" to type,
                    "limit" to limit,
                    "time" to time
                )
            )
        )
    }

    /**
     * 新版统一评论。sortType：1 推荐 / 2 热度 / 3 时间；
     * cursor 为响应返回的时间戳字符串，翻页时原样回传。
     */
    suspend fun getCommentNew(
        id: Long,
        type: Int = 0,
        pageNo: Int = 1,
        pageSize: Int = 20,
        sortType: Int = 1,
        cursor: String? = null,
    ): CommentNewData {
        return SharedJson.decodeBean(SharedNet.get(
                "comment/new",
                params = listOf(
                    "id" to id,
                    "type" to type,
                    "pageNo" to pageNo,
                    "pageSize" to pageSize,
                    "sortType" to sortType,
                    "cursor" to cursor
                )
            )
        )
    }
}

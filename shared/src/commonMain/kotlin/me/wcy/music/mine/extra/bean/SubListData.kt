package me.wcy.music.mine.extra.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * artist/sublist、album/sublist、mv/sublist 共用外壳：
 * 三个接口字段名为 hasMore/count 或 more/count，两者都声明保证兼容。
 */
@Serializable
data class SubListData<T>(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("more")
    val more: Boolean = false,
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("data")
    val data: List<T> = listOf()
)

@Serializable
data class ArtistSubItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("alias")
    val alias: List<String> = listOf(),
    @SerialName("trans")
    val trans: List<String> = listOf()
) {
    fun aliasText(): String = (alias + trans).joinToString(" / ")
}

@Serializable
data class AlbumSubItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("alias")
    val alias: List<String> = listOf(),
    @SerialName("transNames")
    val transNames: List<String> = listOf()
) {
    fun aliasText(): String = (alias + transNames).joinToString(" / ")
}

/**
 * mv/sublist 的时长字段为 duration，decodeBean 预处理已统一归一为 dt；
 * durationMs 兜底兼容任务描述中的字段名。
 */
@Serializable
data class MvSubItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("cover")
    val cover: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("artistName")
    val artistName: String = "",
    @SerialName("dt")
    val duration: Long = 0,
    @SerialName("durationMs")
    val durationMs: Long = 0
) {
    fun coverUrl(): String = cover.ifBlank { picUrl }

    fun durationValue(): Long = if (duration > 0) duration else durationMs
}

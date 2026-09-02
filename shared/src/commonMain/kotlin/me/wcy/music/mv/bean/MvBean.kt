package me.wcy.music.mv.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.ArtistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.shared.util.CoverUtils.asLargeCover
import me.wcy.music.shared.util.CoverUtils.asSmallCover

/**
 * MV 条目（/mv/first、/mv/all、/mv/detail.data、/mv/sublist.data）
 */
@Serializable
data class MvItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("cover")
    val cover: String = "",
    @SerialName("artistName")
    val artistName: String = "",
    @SerialName("artistId")
    val artistId: Long = 0,
    @SerialName("artists")
    val artists: List<ArtistData> = listOf(),
    @SerialName("duration")
    val duration: Long = 0,
    @SerialName("playCount")
    val playCount: Long = 0,
    @SerialName("publishTime")
    val publishTime: String = "",
    @SerialName("desc")
    val desc: String = "",
    @SerialName("briefDesc")
    val briefDesc: String = "",
    @SerialName("subCount")
    val subCount: Long = 0,
    @SerialName("shareCount")
    val shareCount: Long = 0,
    @SerialName("commentCount")
    val commentCount: Long = 0,
    @SerialName("subed")
    val subed: Boolean = false,
    @SerialName("alias")
    val alias: List<String> = listOf()
) {
    fun getSmallCover(): String {
        return cover.asSmallCover()
    }

    fun getLargeCover(): String {
        return cover.asLargeCover()
    }
}

/**
 * GET /mv/detail 返回 {subed, data: MvItem, code}
 */
@Serializable
data class MvDetailData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("subed")
    val subed: Boolean = false,
    @SerialName("data")
    val data: MvItem = MvItem()
)

/**
 * GET /mv/url 返回 {code, data: {id, url, r, size}}
 */
@Serializable
data class MvUrlInfo(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("url")
    val url: String = "",
    @SerialName("r")
    val r: Int = 0,
    @SerialName("size")
    val size: Long = 0
)

@Serializable
data class MvUrlData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: MvUrlInfo = MvUrlInfo()
)

/**
 * GET /mv/first 或 /mv/all 返回 {data: List<MvItem>, hasMore, count, code}
 */
@Serializable
data class PersonalizedMvCard(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("artistName")
    val artistName: String = "",
    @SerialName("duration")
    val duration: Long = 0,
    @SerialName("playCount")
    val playCount: Long = 0
)

@Serializable
data class PersonalizedMvData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("result")
    val result: List<PersonalizedMvCard> = listOf()
)

@Serializable
data class MvListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("count")
    val count: Long = 0,
    @SerialName("data")
    val data: List<MvItem> = listOf()
)

/**
 * GET /mv/sublist 返回 {count, data: List<MvItem>, hasMore, code}
 */
@Serializable
data class MvSublistData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("count")
    val count: Long = 0,
    @SerialName("data")
    val data: List<MvItem> = listOf()
)

/**
 * GET /personalized/newsong 返回 {code, category, result: List<NewSongItem>}
 */
@Serializable
data class NewSongItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("type")
    val type: Int = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("song")
    val song: SongData = SongData()
)

@Serializable
data class NewSongListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("category")
    val category: Int = 0,
    @SerialName("result")
    val result: List<NewSongItem> = listOf()
)

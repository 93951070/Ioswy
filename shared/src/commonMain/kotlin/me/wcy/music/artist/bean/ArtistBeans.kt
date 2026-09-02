package me.wcy.music.artist.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.SongData

/**
 * 歌手通用信息（歌手列表/详情共用；common.bean.ArtistData 无 picUrl，故自建）。
 */
@Serializable
data class ArtistInfo(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("img1v1Url")
    val img1v1Url: String = "",
    @SerialName("alias")
    val alias: List<String> = listOf(),
    @SerialName("briefDesc")
    val briefDesc: String = "",
    @SerialName("musicSize")
    val musicSize: Long = 0,
    @SerialName("albumSize")
    val albumSize: Long = 0,
    @SerialName("mvSize")
    val mvSize: Long = 0,
    @SerialName("subbed")
    val subbed: Boolean = false
) {
    fun getCoverUrl(): String = if (picUrl.isNotBlank()) picUrl else img1v1Url
}

/**
 * GET artist/list 返回。
 * 注意：decodeBean 的 fixLegacyFields 会把响应里的 "artists" 键统一改名 "ar"，故用 @SerialName("ar") 接。
 */
@Serializable
data class ArtistListData(
    @SerialName("ar")
    val artists: List<ArtistInfo> = listOf(),
    @SerialName("more")
    val more: Boolean = false,
    @SerialName("code")
    val code: Int = 0
)

/**
 * GET artists 返回（歌手详情 + 热门歌曲，hotSongs 为旧结构，decodeBean 已统一改名 ar/al/dt）。
 */
@Serializable
data class ArtistDetailData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("artist")
    val artist: ArtistInfo = ArtistInfo(),
    @SerialName("hotSongs")
    val hotSongs: List<SongData> = listOf()
)

/**
 * GET artist/top/song 返回。
 */
@Serializable
data class ArtistTopSongData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("songs")
    val songs: List<SongData> = listOf()
)

/**
 * GET artist/album 返回的专辑项（AlbumData 无 size 字段，自建轻量结构）。
 */
@Serializable
data class ArtistAlbumItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("size")
    val size: Long = 0,
    @SerialName("publishTime")
    val publishTime: Long = 0
)

/**
 * GET artist/album 返回。
 */
@Serializable
data class ArtistAlbumData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("artist")
    val artist: ArtistInfo = ArtistInfo(),
    @SerialName("hotAlbums")
    val hotAlbums: List<ArtistAlbumItem> = listOf()
)

/**
 * GET artist/mv 返回的 MV 项。
 */
@Serializable
data class MvItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("img")
    val img: String = "",
    @SerialName("playCount")
    val playCount: Long = 0,
    @SerialName("duration")
    val duration: Long = 0,
    @SerialName("publishTime")
    val publishTime: Long = 0,
    @SerialName("artistName")
    val artistName: String = ""
)

/**
 * GET artist/mv 返回。
 */
@Serializable
data class ArtistMvData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("mvs")
    val mvs: List<MvItem> = listOf()
)

/**
 * GET artist/desc 返回的介绍段落。
 */
@Serializable
data class ArtistDescItem(
    @SerialName("ti")
    val ti: String = "",
    @SerialName("txt")
    val txt: String = ""
)

/**
 * GET artist/desc 返回。
 */
@Serializable
data class ArtistDescData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("briefDesc")
    val briefDesc: String = "",
    @SerialName("introduction")
    val introduction: List<ArtistDescItem> = listOf()
)

/**
 * GET artist/sublist 返回的收藏歌手项。
 */
@Serializable
data class SubListItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("img1v1Url")
    val img1v1Url: String = "",
    @SerialName("alias")
    val alias: List<String> = listOf(),
    @SerialName("musicSize")
    val musicSize: Long = 0,
    @SerialName("albumSize")
    val albumSize: Long = 0,
    @SerialName("mvSize")
    val mvSize: Long = 0
)

/**
 * GET artist/sublist 返回。
 */
@Serializable
data class ArtistSublistData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("count")
    val count: Long = 0,
    @SerialName("more")
    val more: Boolean = false,
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("data")
    val data: List<SubListItem> = listOf()
)

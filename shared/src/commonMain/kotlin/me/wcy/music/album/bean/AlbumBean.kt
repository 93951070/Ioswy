package me.wcy.music.album.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.ArtistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.shared.util.CoverUtils.asLargeCover
import me.wcy.music.shared.util.CoverUtils.asSmallCover

/**
 * 专辑对象（/album 的 album 字段、/album/sublist 的 data 元素）
 */
@Serializable
data class AlbumInfo(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("publishTime")
    val publishTime: Long = 0,
    @SerialName("company")
    val company: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("briefDesc")
    val briefDesc: String = "",
    @SerialName("artist")
    val artist: ArtistData = ArtistData(),
    @SerialName("artists")
    val artists: List<ArtistData> = listOf(),
    @SerialName("subType")
    val subType: String = "",
    @SerialName("type")
    val type: String = "",
    @SerialName("size")
    val size: Int = 0,
    @SerialName("alias")
    val alias: List<String> = listOf(),
    @SerialName("pic_str")
    val picStr: String = "",
    @SerialName("pic")
    val pic: Long = 0
) {
    fun getSmallCover(): String {
        return picUrl.asSmallCover()
    }

    fun getLargeCover(): String {
        return picUrl.asLargeCover()
    }
}

/**
 * GET /album 返回 {resourceState, songs, code, album}
 */
@Serializable
data class AlbumDetailData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("resourceState")
    val resourceState: Boolean = false,
    @SerialName("album")
    val album: AlbumInfo = AlbumInfo(),
    @SerialName("songs")
    val songs: List<SongData> = listOf()
)

/**
 * GET /album/sublist 返回 {count, more, data: List<AlbumInfo>}
 */
@Serializable
data class AlbumSublistData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("count")
    val count: Long = 0,
    @SerialName("more")
    val more: Boolean = false,
    @SerialName("data")
    val data: List<AlbumInfo> = listOf()
)

/**
 * GET /album/list 返回 {products: List<NewAlbumItem>, code}
 */
@Serializable
data class NewAlbumItem(
    @SerialName("albumId")
    val albumId: Long = 0,
    @SerialName("albumName")
    val albumName: String = "",
    @SerialName("artistName")
    val artistName: String = "",
    @SerialName("coverUrl")
    val coverUrl: String = "",
    @SerialName("pubTime")
    val pubTime: Long = 0,
    @SerialName("saleNum")
    val saleNum: Int = 0
)

@Serializable
data class NewAlbumListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("products")
    val products: List<NewAlbumItem> = listOf()
)

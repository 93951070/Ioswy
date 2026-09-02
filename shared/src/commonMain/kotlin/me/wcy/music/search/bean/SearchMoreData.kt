package me.wcy.music.search.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.ArtistData
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.dj.bean.DjRadioData
import me.wcy.music.mv.bean.MvItem

/**
 * /cloudsearch 多类型搜索统一 result：不同 type 返回不同数组，其余字段为空默认值。
 * fixLegacyFields 会把所有层级 artists 键改名为 ar，歌手数组需用 @SerialName("ar") 接收。
 */
@Serializable
data class SearchMultiResult(
    @SerialName("songs")
    val songs: List<SongData> = emptyList(),
    @SerialName("ar")
    val artists: List<SearchArtistData> = emptyList(),
    @SerialName("albums")
    val albums: List<SearchAlbumData> = emptyList(),
    @SerialName("playlists")
    val playlists: List<PlaylistData> = emptyList(),
    @SerialName("mvs")
    val mvs: List<MvItem> = emptyList(),
    @SerialName("djRadios")
    val djRadios: List<DjRadioData> = emptyList(),
    @SerialName("userprofiles")
    val userprofiles: List<SearchUserData> = emptyList()
)

@Serializable
data class SearchMultiResultData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("result")
    val result: SearchMultiResult = SearchMultiResult()
)

/**
 * 歌手搜索项（cloudsearch type=100）
 */
@Serializable
data class SearchArtistData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("alias")
    val alias: List<String> = emptyList()
)

/**
 * 专辑搜索项（cloudsearch type=10），artist 为旧版单数字段，fixLegacyFields 不会改名
 */
@Serializable
data class SearchAlbumData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("artist")
    val artist: ArtistData = ArtistData()
)

/**
 * 用户搜索项（cloudsearch type=1002）
 */
@Serializable
data class SearchUserData(
    @SerialName("userId")
    val userId: Long = 0,
    @SerialName("nickname")
    val nickname: String = "",
    @SerialName("avatarUrl")
    val avatarUrl: String = "",
    @SerialName("signature")
    val signature: String = ""
)

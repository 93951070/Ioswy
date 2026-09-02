package me.wcy.music.artist

import kotlinx.serialization.json.JsonElement
import me.wcy.music.artist.bean.ArtistAlbumData
import me.wcy.music.artist.bean.ArtistDescData
import me.wcy.music.artist.bean.ArtistDetailData
import me.wcy.music.artist.bean.ArtistListData
import me.wcy.music.artist.bean.ArtistMvData
import me.wcy.music.artist.bean.ArtistSublistData
import me.wcy.music.artist.bean.ArtistTopSongData
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.shared.net.NetResult
import me.wcy.music.shared.net.SharedNet

/**
 * 歌手分类：网易云 /artist/list 标准分类。
 * type: -1 全部 / 1 男歌手 / 2 女歌手 / 3 组合
 * area: -1 全部 / 96 华语 / 21 欧美 / 8 日本 / 16 韩国 / 0 其他
 */
data class ArtistCategory(
    val name: String,
    val type: Int,
    val area: String
)

object ArtistCategories {
    val ALL: List<ArtistCategory> = listOf(
        ArtistCategory("入驻歌手", -1, "-1"),
        ArtistCategory("华语男歌手", 1, "96"),
        ArtistCategory("华语女歌手", 2, "96"),
        ArtistCategory("华语组合", 3, "96"),
        ArtistCategory("欧美男歌手", 1, "21"),
        ArtistCategory("欧美女歌手", 2, "21"),
        ArtistCategory("欧美组合", 3, "21"),
        ArtistCategory("日本男歌手", 1, "8"),
        ArtistCategory("日本女歌手", 2, "8"),
        ArtistCategory("日本组合", 3, "8"),
        ArtistCategory("韩国男歌手", 1, "16"),
        ArtistCategory("韩国女歌手", 2, "16"),
        ArtistCategory("韩国组合", 3, "16"),
        ArtistCategory("其他男歌手", 1, "0"),
        ArtistCategory("其他女歌手", 2, "0"),
        ArtistCategory("其他组合", 3, "0")
    )
}

/**
 * 歌手域接口。
 */
object ArtistNet {

    suspend fun getArtistList(
        type: Int,
        area: String = "-1",
        limit: Int = 60,
        offset: Int = 0
    ): ArtistListData {
        return SharedJson.decodeBean(SharedNet.get(
                "artist/list",
                params = listOf(
                    "type" to type,
                    "area" to area,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    suspend fun getArtistDetail(id: Long): ArtistDetailData {
        return SharedJson.decodeBean(SharedNet.get(
                "artists",
                params = listOf("id" to id)
            )
        )
    }

    suspend fun getArtistTopSong(
        id: Long,
        limit: Int = 50,
        order: String = "hot",
        offset: Int = 0
    ): ArtistTopSongData {
        return SharedJson.decodeBean(SharedNet.get(
                "artist/top/song",
                params = listOf(
                    "id" to id,
                    "limit" to limit,
                    "order" to order,
                    "offset" to offset
                )
            )
        )
    }

    suspend fun getArtistAlbum(
        id: Long,
        limit: Int = 30,
        offset: Int = 0
    ): ArtistAlbumData {
        return SharedJson.decodeBean(SharedNet.get(
                "artist/album",
                params = listOf(
                    "id" to id,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    suspend fun getArtistMv(
        id: Long,
        limit: Int = 30,
        offset: Int = 0
    ): ArtistMvData {
        return SharedJson.decodeBean(SharedNet.get(
                "artist/mv",
                params = listOf(
                    "id" to id,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    suspend fun getArtistDesc(id: Long): ArtistDescData {
        return SharedJson.decodeBean(SharedNet.get(
                "artist/desc",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 收藏/取消收藏歌手
     * @param t 类型,1:收藏,2:取消收藏
     */
    suspend fun subArtist(
        id: Long,
        t: Int,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.get(
                "artist/sub",
                params = listOf(
                    "id" to id,
                    "t" to t,
                    "timestamp" to timestamp
                )
            )
        )
    }

    suspend fun getArtistSublist(limit: Int = 50): ArtistSublistData {
        return SharedJson.decodeBean(SharedNet.get(
                "artist/sublist",
                params = listOf("limit" to limit)
            )
        )
    }
}

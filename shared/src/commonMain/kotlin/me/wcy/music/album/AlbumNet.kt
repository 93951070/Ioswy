package me.wcy.music.album

import kotlinx.serialization.json.JsonElement
import me.wcy.music.album.bean.AlbumDetailData
import me.wcy.music.album.bean.AlbumSublistData
import me.wcy.music.album.bean.NewAlbumListData
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.shared.net.NetResult
import me.wcy.music.shared.net.SharedNet

/**
 * 专辑相关接口。
 */
object AlbumNet {

    /**
     * 专辑详情，含歌曲列表
     */
    suspend fun getAlbumDetail(id: Long): AlbumDetailData {
        return SharedJson.decodeBean(SharedNet.get(
                "album",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 收藏/取消收藏专辑
     * @param id 专辑 id
     * @param t 类型,1:收藏,2:取消收藏
     */
    suspend fun subAlbum(
        id: Long,
        t: Int,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.get(
                "album/sub",
                params = listOf(
                    "id" to id,
                    "t" to t,
                    "timestamp" to timestamp
                )
            )
        )
    }

    /**
     * 已收藏专辑列表
     */
    suspend fun getAlbumSublist(limit: Int = 50): AlbumSublistData {
        return SharedJson.decodeBean(SharedNet.get(
                "album/sublist",
                params = listOf("limit" to limit)
            )
        )
    }

    /**
     * 新碟上架
     * @param area ALL:全部, ZH:华语, EA:欧美, KR:韩国, JP:日本
     * @param type new:全部新碟, hot:热门, original:华语
     */
    suspend fun getNewAlbumList(
        area: String = "ALL",
        type: String = "new",
        limit: Int = 35,
        offset: Int = 0
    ): NewAlbumListData {
        return SharedJson.decodeBean(SharedNet.get(
                "album/list",
                params = listOf(
                    "area" to area,
                    "type" to type,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }
}

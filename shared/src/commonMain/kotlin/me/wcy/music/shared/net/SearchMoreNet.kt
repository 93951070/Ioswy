package me.wcy.music.shared.net

import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.discover.playlist.detail.bean.SongListData
import me.wcy.music.discover.playlist.square.bean.PlaylistListData
import me.wcy.music.search.bean.SearchMultiResultData

/**
 * 搜索扩展接口：多类型搜索、相似歌曲/相似歌单。
 * 每日推荐 recommend/songs 复用 DiscoverNet.getRecommendSongs，不重复封装。
 */
object SearchMoreNet {

    suspend fun searchMulti(
        type: Int,
        keywords: String,
        limit: Int = 30,
        offset: Int = 0
    ): NetResult<SearchMultiResultData> {
        return SharedJson.decodeBean(SharedNet.post(
                "cloudsearch",
                params = listOf(
                    "type" to type,
                    "keywords" to keywords,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 相似歌曲，需要登录 cookie；匿名可能返回空列表或 code 301，调用方需容忍空数据。
     */
    suspend fun getSimiSongs(id: Long): NetResult<SongListData> {
        return SharedJson.decodeBean(SharedNet.get(
                "simi/song",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 相似歌单（包含该歌的歌单），需要登录 cookie；匿名返回空列表或 code 301。
     */
    suspend fun getSimiPlaylists(id: Long): NetResult<PlaylistListData> {
        return SharedJson.decodeBean(SharedNet.get(
                "simi/playlist",
                params = listOf("id" to id)
            )
        )
    }
}

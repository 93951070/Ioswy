package me.wcy.music.shared.net

import me.wcy.music.common.bean.SharedJson
import me.wcy.music.search.bean.HotSearchData
import me.wcy.music.search.bean.SearchResultData

/**
 * 搜索页接口。
 */
object SearchNet {

    /**
     * 搜索歌曲
     * @param type 搜索类型；默认为 1 即单曲 , 取值意义 :
     * - 1: 单曲,
     * - 10: 专辑,
     * - 100: 歌手,
     * - 1000: 歌单,
     * - 1002: 用户,
     * - 1004: MV,
     * - 1006: 歌词,
     * - 1009: 电台,
     * - 1014: 视频,
     * - 1018:综合,
     * - 2000:声音(搜索声音返回字段格式会不一样)
     */
    suspend fun search(
        type: Int,
        keywords: String,
        limit: Int,
        offset: Int,
    ): NetResult<SearchResultData> {
        return SharedJson.decodeFromString(
            SharedNet.post(
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

    suspend fun getHotSearch(): HotSearchData {
        return SharedJson.decodeFromString(SharedNet.get("search/hot/detail"))
    }
}

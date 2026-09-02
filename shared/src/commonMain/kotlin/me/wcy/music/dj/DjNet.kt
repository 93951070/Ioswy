package me.wcy.music.dj

import kotlinx.serialization.json.JsonElement
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.dj.bean.DjCatelistData
import me.wcy.music.dj.bean.DjDetailData
import me.wcy.music.dj.bean.DjListData
import me.wcy.music.dj.bean.DjProgramListData
import me.wcy.music.dj.bean.DjProgramToplistData
import me.wcy.music.shared.net.NetResult
import me.wcy.music.shared.net.SharedNet

/**
 * 电台/播客接口。
 */
object DjNet {

    suspend fun getDjRecommend(limit: Int = 30): DjListData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/recommend",
                params = listOf(
                    "limit" to limit
                )
            )
        )
    }

    suspend fun getDjHot(
        limit: Int = 30,
        offset: Int = 0
    ): DjListData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/hot",
                params = listOf(
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    suspend fun getDjCatelist(): DjCatelistData {
        return SharedJson.decodeBean(SharedNet.get("dj/catelist"))
    }

    suspend fun getDjDetail(rid: Long): DjDetailData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/detail",
                params = listOf(
                    "rid" to rid
                )
            )
        )
    }

    suspend fun getDjProgram(
        rid: Long,
        limit: Int = 30,
        offset: Int = 0,
        asc: Boolean = false
    ): DjProgramListData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/program",
                params = listOf(
                    "rid" to rid,
                    "limit" to limit,
                    "offset" to offset,
                    "asc" to asc
                )
            )
        )
    }

    suspend fun getDjProgramToplist(limit: Int = 30): DjProgramToplistData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/program/toplist",
                params = listOf(
                    "limit" to limit
                )
            )
        )
    }

    /**
     * 订阅/取消订阅电台
     * @param rid 电台 id
     * @param t 类型,1:订阅,2:取消订阅
     */
    suspend fun subDj(
        rid: Long,
        t: Int,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/sub",
                params = listOf(
                    "rid" to rid,
                    "t" to t,
                    "timestamp" to timestamp
                )
            )
        )
    }

    suspend fun getDjSublist(limit: Int = 50): DjListData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/sublist",
                params = listOf(
                    "limit" to limit
                )
            )
        )
    }

    suspend fun getProgramRecommend(): DjProgramListData {
        return SharedJson.decodeBean(SharedNet.get("program/recommend"))
    }
}

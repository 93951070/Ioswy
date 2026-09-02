package me.wcy.music.mv

import kotlinx.serialization.json.JsonElement
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.mv.bean.MvDetailData
import me.wcy.music.mv.bean.MvListData
import me.wcy.music.mv.bean.MvSublistData
import me.wcy.music.mv.bean.MvUrlData
import me.wcy.music.mv.bean.NewSongListData
import me.wcy.music.mv.bean.PersonalizedMvData
import me.wcy.music.shared.net.NetResult
import me.wcy.music.shared.net.SharedNet

/**
 * MV 相关接口。
 */
object MvNet {

    /**
     * MV 详情
     */
    suspend fun getMvDetail(mvid: Long): MvDetailData {
        return SharedJson.decodeBean(SharedNet.get(
                "mv/detail",
                params = listOf("mvid" to mvid)
            )
        )
    }

    /**
     * MV 播放地址
     * @param r 分辨率,如 1080
     */
    suspend fun getMvUrl(id: Long, r: Int = 1080): MvUrlData {
        return SharedJson.decodeBean(SharedNet.get(
                "mv/url",
                params = listOf(
                    "id" to id,
                    "r" to r
                )
            )
        )
    }

    /**
     * 最新 MV
     * @param area 地区,为空表示全部
     * @param type 类型,为空表示全部
     */
    suspend fun getMvFirst(
        area: String = "",
        type: String = "",
        limit: Int = 30
    ): MvListData {
        return SharedJson.decodeBean(SharedNet.get(
                "mv/first",
                params = listOf(
                    "area" to area,
                    "type" to type,
                    "limit" to limit
                )
            )
        )
    }

    /**
     * 全部 MV
     * @param area 地区,如 内地/港台/欧美/日本/韩国
     * @param type 类型,如 官方版/原生/现场版/网易出品
     * @param order 排序,如 上升最快/最热/最新
     */
    suspend fun getMvAll(
        area: String = "",
        type: String = "",
        order: String = "",
        limit: Int = 30,
        offset: Int = 0
    ): MvListData {
        return SharedJson.decodeBean(SharedNet.get(
                "mv/all",
                params = listOf(
                    "area" to area,
                    "type" to type,
                    "order" to order,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 收藏/取消收藏 MV
     * @param mvid MV id
     * @param t 类型,1:收藏,2:取消收藏
     */
    suspend fun subMv(
        mvid: Long,
        t: Int,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.get(
                "mv/sub",
                params = listOf(
                    "mvid" to mvid,
                    "t" to t,
                    "timestamp" to timestamp
                )
            )
        )
    }

    /**
     * 已收藏 MV 列表
     */
    suspend fun getMvSublist(limit: Int = 50): MvSublistData {
        return SharedJson.decodeBean(SharedNet.get(
                "mv/sublist",
                params = listOf("limit" to limit)
            )
        )
    }

    /**
     * 新歌速递
     */
    suspend fun getPersonalizedNewsong(limit: Int = 10): NewSongListData {
        return SharedJson.decodeBean(SharedNet.get(
                "personalized/newsong",
                params = listOf("limit" to limit)
            )
        )
    }

    suspend fun getPersonalizedMv(): PersonalizedMvData {
        return SharedJson.decodeBean(SharedNet.get("personalized/mv"))
    }
}

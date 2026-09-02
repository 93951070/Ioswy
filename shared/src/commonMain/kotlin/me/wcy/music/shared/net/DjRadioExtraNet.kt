package me.wcy.music.shared.net

import kotlinx.serialization.json.JsonElement
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.dj.bean.DjListData
import me.wcy.music.shared.bean.dj.BroadcastCategoryRegionData
import me.wcy.music.shared.bean.dj.BroadcastChannelInfoData
import me.wcy.music.shared.bean.dj.BroadcastChannelListData
import me.wcy.music.shared.bean.dj.BroadcastCollectListData
import me.wcy.music.shared.bean.dj.DjAnchorToplistData
import me.wcy.music.shared.bean.dj.DjBannerData
import me.wcy.music.shared.bean.dj.DjCategoryExcludehotData
import me.wcy.music.shared.bean.dj.DjCategoryRecommendData
import me.wcy.music.shared.bean.dj.DjPayToplistData
import me.wcy.music.shared.bean.dj.DjPaygiftData
import me.wcy.music.shared.bean.dj.DjPersonalizeData
import me.wcy.music.shared.bean.dj.DjProgramDetailData
import me.wcy.music.shared.bean.dj.DjProgramToplistHoursData
import me.wcy.music.shared.bean.dj.DjSubscribersData
import me.wcy.music.shared.bean.dj.DjTodayPerferedData
import me.wcy.music.shared.bean.dj.DjToplistData
import me.wcy.music.shared.bean.dj.VoiceLyricData
import me.wcy.music.shared.bean.dj.VoicelistSearchData

/**
 * 电台/播客扩展接口。
 * 电台基础详情/节目列表/分类见 DjNet，此处补充排行榜、分类推荐与播客（广播电台/声音）接口。
 */
object DjRadioExtraNet {

    /**
     * 推荐电台 banner
     */
    suspend fun getDjBanner(type: Int = 0): DjBannerData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/banner",
                params = listOf("type" to type)
            )
        )
    }

    /**
     * 电台排行榜（新晋/热门）
     * @param type 0:新晋,1:热门
     */
    suspend fun getDjToplist(
        limit: Int = 100,
        type: Int = 1
    ): DjToplistData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/toplist",
                params = listOf(
                    "limit" to limit,
                    "type" to type
                )
            )
        )
    }

    /**
     * 电台 24 小时主播榜，仅支持 limit
     */
    suspend fun getDjToplistHours(limit: Int = 100): DjAnchorToplistData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/toplist/hours",
                params = listOf("limit" to limit)
            )
        )
    }

    /**
     * 电台新人榜
     */
    suspend fun getDjToplistNewcomer(
        limit: Int = 100,
        offset: Int = 0
    ): DjAnchorToplistData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/toplist/newcomer",
                params = listOf(
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 付费电台榜，仅支持 limit
     */
    suspend fun getDjToplistPay(limit: Int = 100): DjPayToplistData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/toplist/pay",
                params = listOf("limit" to limit)
            )
        )
    }

    /**
     * 最热主播榜，仅支持 limit
     */
    suspend fun getDjToplistPopular(limit: Int = 100): DjAnchorToplistData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/toplist/popular",
                params = listOf("limit" to limit)
            )
        )
    }

    /**
     * 电台节目详情，注意 id 为节目 id 而非电台 id
     */
    suspend fun getDjProgramDetail(id: Long): DjProgramDetailData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/program/detail",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 电台 24 小时节目榜，仅支持 limit
     */
    suspend fun getDjProgramToplistHours(limit: Int = 100): DjProgramToplistHoursData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/program/toplist/hours",
                params = listOf("limit" to limit)
            )
        )
    }

    /**
     * 分类推荐电台
     * @param type 分类 id,如 10001:有声书,3:情感调频,2001:创作|翻唱
     */
    suspend fun getDjRecommendType(type: Long): DjListData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/recommend/type",
                params = listOf("type" to type)
            )
        )
    }

    /**
     * 付费精品电台
     */
    suspend fun getDjPaygift(
        limit: Int = 30,
        offset: Int = 0
    ): DjPaygiftData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/paygift",
                params = listOf(
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 个性化推荐电台，需要登录 cookie
     */
    suspend fun getDjPersonalizeRecommend(limit: Int = 10): DjPersonalizeData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/personalize/recommend",
                params = listOf("limit" to limit)
            )
        )
    }

    /**
     * 类别热门电台，cateId 必填
     */
    suspend fun getDjRadioHot(
        cateId: Long,
        limit: Int = 30,
        offset: Int = 0
    ): DjListData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/radio/hot",
                params = listOf(
                    "cateId" to cateId,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 电台订阅者，注意参数名为 id
     */
    suspend fun getDjSubscribers(
        id: Long,
        limit: Int = 20,
        offset: Int = 0,
        time: Long = -1
    ): DjSubscribersData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/subscriber",
                params = listOf(
                    "id" to id,
                    "limit" to limit,
                    "offset" to offset,
                    "time" to time
                )
            )
        )
    }

    /**
     * 今日优选，需要登录 cookie
     */
    suspend fun getDjTodayPerfered(page: Int = 0): DjTodayPerferedData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/today/perfered",
                params = listOf("page" to page)
            )
        )
    }

    /**
     * 非热门电台分类
     */
    suspend fun getDjCategoryExcludeHot(): DjCategoryExcludehotData {
        return SharedJson.decodeBean(SharedNet.get("dj/category/excludehot"))
    }

    /**
     * 精选电台分类推荐
     */
    suspend fun getDjCategoryRecommend(): DjCategoryRecommendData {
        return SharedJson.decodeBean(SharedNet.get("dj/category/recommend"))
    }

    /**
     * 播客：电台分类与地区列表
     */
    suspend fun getBroadcastCategoryRegion(): BroadcastCategoryRegionData {
        return SharedJson.decodeBean(SharedNet.get("broadcast/category/region/get"))
    }

    /**
     * 播客：电台列表，lastId/score 用于翻页（取上一页最后一条的 id/score）
     */
    suspend fun getBroadcastChannelList(
        categoryId: Long = 0,
        regionId: Long = 0,
        limit: Int = 20,
        lastId: Long = 0,
        score: Int = -1
    ): BroadcastChannelListData {
        return SharedJson.decodeBean(SharedNet.get(
                "broadcast/channel/list",
                params = listOf(
                    "categoryId" to categoryId,
                    "regionId" to regionId,
                    "limit" to limit,
                    "lastId" to lastId,
                    "score" to score
                )
            )
        )
    }

    /**
     * 播客：电台当前播放信息与直播流地址
     */
    suspend fun getBroadcastChannelCurrentInfo(id: Long): BroadcastChannelInfoData {
        return SharedJson.decodeBean(SharedNet.get(
                "broadcast/channel/currentinfo",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 播客：已收藏电台列表
     */
    suspend fun getBroadcastChannelCollectList(): BroadcastCollectListData {
        return SharedJson.decodeBean(SharedNet.get("broadcast/channel/collect/list"))
    }

    /**
     * 播客：收藏/取消收藏电台
     * @param t 类型,1:收藏,其他:取消收藏
     */
    suspend fun subBroadcast(
        id: Long,
        t: Int,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.post(
                "broadcast/sub",
                params = listOf(
                    "id" to id,
                    "t" to t,
                    "timestamp" to timestamp
                )
            )
        )
    }

    /**
     * 声音播客：搜索播客列表（workbench 接口，普通账号实测返回空列表）
     */
    suspend fun getVoicelistSearch(
        podcastName: String,
        limit: Int = 200,
        offset: Int = 0
    ): VoicelistSearchData {
        return SharedJson.decodeBean(SharedNet.get(
                "voicelist/search",
                params = listOf(
                    "podcastName" to podcastName,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 声音播客：搜索声音列表（workbench 接口，仅主播可用）
     */
    suspend fun getVoicelistListSearch(
        name: String,
        voiceListId: Long,
        limit: Int = 200,
        offset: Int = 0
    ): VoicelistSearchData {
        return SharedJson.decodeBean(SharedNet.get(
                "voicelist/list/search",
                params = listOf(
                    "name" to name,
                    "voiceListId" to voiceListId,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 声音播客：获取播客内声音列表。voice/workbench 主播接口，仅允许操作自己的播客，
     * 普通账号返回 code 400（只允许操作自己的播客），故返回原始 JSON 由调用方判定。
     */
    suspend fun getVoicelistList(
        voiceListId: Long,
        limit: Int = 200,
        offset: Int = 0
    ): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.get(
                "voicelist/list",
                params = listOf(
                    "voiceListId" to voiceListId,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 声音播客：播客详情。workbench 主播接口，仅允许操作自己的播客，同 getVoicelistList 处理。
     */
    suspend fun getVoicelistDetail(id: Long): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.get(
                "voicelist/detail",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 声音播客：声音详情。workbench 主播接口，仅允许操作自己的声音，同 getVoicelistList 处理。
     */
    suspend fun getVoiceDetail(id: Long): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.get(
                "voice/detail",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 声音播客：歌词，无歌词时 data 为 null
     */
    suspend fun getVoiceLyric(id: Long): VoiceLyricData {
        return SharedJson.decodeBean(SharedNet.get(
                "voice/lyric",
                params = listOf("id" to id)
            )
        )
    }
}

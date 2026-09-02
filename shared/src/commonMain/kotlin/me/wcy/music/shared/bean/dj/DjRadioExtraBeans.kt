package me.wcy.music.shared.bean.dj

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.dj.bean.DjData
import me.wcy.music.dj.bean.DjProgramData
import me.wcy.music.dj.bean.DjRadioData

/**
 * GET /dj/banner 返回 {code, data: List<DjBanner>}
 */
@Serializable
data class DjBanner(
    @SerialName("targetId")
    val targetId: Long = 0,
    @SerialName("targetType")
    val targetType: Int = 0,
    @SerialName("pic")
    val pic: String = "",
    @SerialName("url")
    val url: String = "",
    @SerialName("typeTitle")
    val typeTitle: String = "",
    @SerialName("exclusive")
    val exclusive: Boolean = false
)

@Serializable
data class DjBannerData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: List<DjBanner> = listOf()
)

/**
 * GET /dj/toplist 返回 {updateTime, toplist: List<DjToplistRadio>}，无 code 字段
 */
@Serializable
data class DjToplistRadio(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("dj")
    val dj: DjData = DjData(),
    @SerialName("programCount")
    val programCount: Int = 0,
    @SerialName("subCount")
    val subCount: Long = 0,
    @SerialName("createTime")
    val createTime: Long = 0,
    @SerialName("categoryId")
    val categoryId: Long = 0,
    @SerialName("category")
    val category: String = "",
    @SerialName("rcmdtext")
    val rcmdText: String = "",
    @SerialName("radioFeeType")
    val radioFeeType: Int = 0,
    @SerialName("feeScope")
    val feeScope: Int = 0,
    @SerialName("playCount")
    val playCount: Long = 0,
    @SerialName("lastRank")
    val lastRank: Int = 0,
    @SerialName("score")
    val score: Long = 0
)

@Serializable
data class DjToplistData(
    @SerialName("updateTime")
    val updateTime: Long = 0,
    @SerialName("toplist")
    val toplist: List<DjToplistRadio> = listOf()
)

/**
 * 电台主播榜条目（/dj/toplist/hours、/dj/toplist/newcomer、/dj/toplist/popular 共用）
 */
@Serializable
data class DjAnchor(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("rank")
    val rank: Int = 0,
    @SerialName("lastRank")
    val lastRank: Int = 0,
    @SerialName("score")
    val score: Long = 0,
    @SerialName("nickName")
    val nickName: String = "",
    @SerialName("avatarUrl")
    val avatarUrl: String = "",
    @SerialName("userType")
    val userType: Int = 0,
    @SerialName("userFollowedCount")
    val userFollowedCount: Long = 0
)

@Serializable
data class DjAnchorToplist(
    @SerialName("total")
    val total: Int = 0,
    @SerialName("updateTime")
    val updateTime: Long = 0,
    @SerialName("list")
    val list: List<DjAnchor> = listOf()
)

/**
 * GET /dj/toplist/hours、/dj/toplist/newcomer、/dj/toplist/popular 共用返回
 */
@Serializable
data class DjAnchorToplistData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("data")
    val data: DjAnchorToplist = DjAnchorToplist()
)

/**
 * GET /dj/toplist/pay 返回，list 为付费电台榜
 */
@Serializable
data class DjPayRadio(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("rank")
    val rank: Int = 0,
    @SerialName("lastRank")
    val lastRank: Int = 0,
    @SerialName("score")
    val score: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("creatorName")
    val creatorName: String = ""
)

@Serializable
data class DjPayToplist(
    @SerialName("total")
    val total: Int = 0,
    @SerialName("updateTime")
    val updateTime: Long = 0,
    @SerialName("list")
    val list: List<DjPayRadio> = listOf()
)

@Serializable
data class DjPayToplistData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("data")
    val data: DjPayToplist = DjPayToplist()
)

/**
 * GET /dj/program/toplist/hours 返回 {code, msg, data: {total, updateTime, list}}
 */
@Serializable
data class DjProgramToplistHoursItem(
    @SerialName("program")
    val program: DjProgramData = DjProgramData(),
    @SerialName("rank")
    val rank: Int = 0,
    @SerialName("lastRank")
    val lastRank: Int = 0,
    @SerialName("score")
    val score: Long = 0,
    @SerialName("programFeeType")
    val programFeeType: Int = 0
)

@Serializable
data class DjProgramToplistHours(
    @SerialName("total")
    val total: Int = 0,
    @SerialName("updateTime")
    val updateTime: Long = 0,
    @SerialName("list")
    val list: List<DjProgramToplistHoursItem> = listOf()
)

@Serializable
data class DjProgramToplistHoursData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("data")
    val data: DjProgramToplistHours = DjProgramToplistHours()
)

/**
 * GET /dj/program/detail?id=节目id 返回 {program, code}
 */
@Serializable
data class DjProgramDetailData(
    @SerialName("program")
    val program: DjProgramData = DjProgramData(),
    @SerialName("code")
    val code: Int = 0
)

/**
 * GET /dj/paygift 返回 {code, msg, data: {hasMore, list}}
 */
@Serializable
data class DjPaygiftRadio(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("rcmdText")
    val rcmdText: String = "",
    @SerialName("radioFeeType")
    val radioFeeType: Int = 0,
    @SerialName("feeScope")
    val feeScope: Int = 0,
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("programCount")
    val programCount: Int = 0,
    @SerialName("subCount")
    val subCount: Long = 0,
    @SerialName("subed")
    val subed: Boolean = false,
    @SerialName("playCount")
    val playCount: Long = 0,
    @SerialName("alg")
    val alg: String? = null,
    @SerialName("originalPrice")
    val originalPrice: Int = 0,
    @SerialName("discountPrice")
    val discountPrice: Int? = null,
    @SerialName("lastProgramName")
    val lastProgramName: String = ""
)

@Serializable
data class DjPaygift(
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("list")
    val list: List<DjPaygiftRadio> = listOf()
)

@Serializable
data class DjPaygiftData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("data")
    val data: DjPaygift = DjPaygift()
)

/**
 * GET /dj/personalize/recommend 返回 {code, data: List<DjRadioData>}
 */
@Serializable
data class DjPersonalizeData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: List<DjRadioData> = listOf()
)

@Serializable
data class DjSubscriber(
    @SerialName("userId")
    val userId: Long = 0,
    @SerialName("nickname")
    val nickname: String = "",
    @SerialName("avatarUrl")
    val avatarUrl: String = "",
    @SerialName("signature")
    val signature: String = ""
)

/**
 * GET /dj/subscriber 返回 {code, subscribers, time, hasMore}
 */
@Serializable
data class DjSubscribersData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("subscribers")
    val subscribers: List<DjSubscriber> = listOf(),
    @SerialName("time")
    val time: Long = 0,
    @SerialName("hasMore")
    val hasMore: Boolean = false
)

/**
 * GET /dj/today/perfered 返回 {code, msg, data: List<DjRadioData>}
 */
@Serializable
data class DjTodayPerferedData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("data")
    val data: List<DjRadioData> = listOf()
)

/**
 * GET /dj/category/excludehot 返回 {code, data: List<DjCategory>}
 */
@Serializable
data class DjCategory(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("pic56x56Url")
    val pic56x56Url: String = ""
)

@Serializable
data class DjCategoryExcludehotData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: List<DjCategory> = listOf()
)

/**
 * GET /dj/category/recommend 返回 {code, msg, data: List<DjRecommendCategory>}
 */
@Serializable
data class DjRecommendCategory(
    @SerialName("categoryId")
    val categoryId: Long = 0,
    @SerialName("categoryName")
    val categoryName: String = "",
    @SerialName("radios")
    val radios: List<DjPaygiftRadio> = listOf()
)

@Serializable
data class DjCategoryRecommendData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("data")
    val data: List<DjRecommendCategory> = listOf()
)

/**
 * 播客（广播电台）
 */
@Serializable
data class BroadcastNameItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = ""
)

/**
 * GET /broadcast/category/region/get 返回 {code, data: {categoryList, regionList}}
 */
@Serializable
data class BroadcastCategoryRegion(
    @SerialName("categoryList")
    val categoryList: List<BroadcastNameItem> = listOf(),
    @SerialName("regionList")
    val regionList: List<BroadcastNameItem> = listOf()
)

@Serializable
data class BroadcastCategoryRegionData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: BroadcastCategoryRegion = BroadcastCategoryRegion()
)

@Serializable
data class BroadcastChannel(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("regionName")
    val regionName: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("coverUrl")
    val coverUrl: String = "",
    @SerialName("subed")
    val subed: Boolean = false,
    @SerialName("score")
    val score: Int = 0,
    @SerialName("source")
    val source: String = "",
    @SerialName("roomId")
    val roomId: Long = 0
)

/**
 * GET /broadcast/channel/list 返回 {code, data: {hasMore, list}}
 */
@Serializable
data class BroadcastChannelList(
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("list")
    val list: List<BroadcastChannel> = listOf()
)

@Serializable
data class BroadcastChannelListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: BroadcastChannelList = BroadcastChannelList()
)

/**
 * GET /broadcast/channel/currentinfo 返回 {code, data, message}
 */
@Serializable
data class BroadcastChannelInfo(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("regionName")
    val regionName: String = "",
    @SerialName("channelName")
    val channelName: String = "",
    @SerialName("channelCoverUrl")
    val channelCoverUrl: String = "",
    @SerialName("programId")
    val programId: Long = 0,
    @SerialName("programName")
    val programName: String? = null,
    @SerialName("broadcaster")
    val broadcaster: String? = null,
    @SerialName("startTime")
    val startTime: Long = 0,
    @SerialName("endTime")
    val endTime: Long = 0,
    @SerialName("duration")
    val duration: Long = 0,
    @SerialName("playUrl")
    val playUrl: String = "",
    @SerialName("currentTime")
    val currentTime: Long = 0,
    @SerialName("thirdChannelId")
    val thirdChannelId: String? = null
)

@Serializable
data class BroadcastChannelInfoData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: BroadcastChannelInfo = BroadcastChannelInfo()
)

/**
 * GET /broadcast/channel/collect/list 返回 {code, message, data: List<BroadcastChannel>}
 */
@Serializable
data class BroadcastCollectListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: List<BroadcastChannel> = listOf()
)

/**
 * 声音播客列表条目（/voicelist/search，workbench 接口需主播身份，元素按保守字段定义）
 */
@Serializable
data class VoicelistItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("programCount")
    val programCount: Int = 0
)

@Serializable
data class VoicelistSearchResult(
    @SerialName("total")
    val total: Int = 0,
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("list")
    val list: List<VoicelistItem> = listOf()
)

/**
 * GET /voicelist/search 与 /voicelist/list/search 共用返回 {code, message, data}
 */
@Serializable
data class VoicelistSearchData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: VoicelistSearchResult = VoicelistSearchResult()
)

/**
 * GET /voice/lyric 返回 {code, message, data: {lrc, tlyric}?}，无歌词时 data 为 null
 */
@Serializable
data class VoiceLyricData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: VoiceLyric? = null
) {
    @Serializable
    data class VoiceLyric(
        @SerialName("lrc")
        val lrc: Lrc? = null,
        @SerialName("tlyric")
        val tlyric: Lrc? = null
    )

    @Serializable
    data class Lrc(
        @SerialName("version")
        val version: Int = 0,
        @SerialName("lyric")
        val lyric: String = ""
    )
}

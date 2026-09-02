package me.wcy.music.shared.bean.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.common.bean.LrcData
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.SongData
import me.wcy.music.common.bean.SongUrlData
import me.wcy.music.dj.bean.DjRadioData

// ==================== 首页 homepage/block/page ====================

/**
 * GET homepage/block/page 实测结构（匿名仅返回推荐歌单/风格推荐/新歌新碟 3 个 block）。
 * block 内部结构因 blockCode 差异极大，resources 的 uiElement/resourceExtInfo
 * 保留为可解析 bean + JsonElement 兜底，用扩展函数按需取值。
 */
@Serializable
data class HomepageBlockPageData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: HomepageBlocksData? = null
)

@Serializable
data class HomepageBlocksData(
    @SerialName("cursor")
    val cursor: String? = null,
    @SerialName("blocks")
    val blocks: List<HomepageBlockData> = emptyList(),
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("blockCodeOrderList")
    val blockCodeOrderList: List<String> = emptyList()
)

@Serializable
data class HomepageBlockData(
    @SerialName("blockCode")
    val blockCode: String = "",
    @SerialName("showType")
    val showType: String = "",
    @SerialName("action")
    val action: String? = null,
    @SerialName("actionType")
    val actionType: String? = null,
    @SerialName("sort")
    val sort: Int = 0,
    @SerialName("blockStyle")
    val blockStyle: Int = 0,
    @SerialName("canClose")
    val canClose: Boolean = false,
    @SerialName("canFeedback")
    val canFeedback: Boolean = false,
    @SerialName("uiElement")
    val uiElement: HomepageUiElementData? = null,
    @SerialName("extInfo")
    val extInfo: JsonElement? = null,
    @SerialName("resourceIdList")
    val resourceIdList: List<String> = emptyList(),
    @SerialName("creatives")
    val creatives: List<HomepageCreativeData> = emptyList()
)

@Serializable
data class HomepageUiElementData(
    @SerialName("mainTitle")
    val mainTitle: HomepageTitleData? = null,
    @SerialName("subTitle")
    val subTitle: HomepageTitleData? = null,
    @SerialName("image")
    val image: HomepageImageData? = null,
    @SerialName("labelTexts")
    val labelTexts: List<String> = emptyList(),
    @SerialName("rcmdShowType")
    val rcmdShowType: String? = null,
    @SerialName("button")
    val button: JsonElement? = null
)

@Serializable
data class HomepageTitleData(
    @SerialName("title")
    val title: String = "",
    @SerialName("canShowTitleLogo")
    val canShowTitleLogo: Boolean = false
)

@Serializable
data class HomepageImageData(
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("purePicture")
    val purePicture: Boolean? = null
)

@Serializable
data class HomepageCreativeData(
    @SerialName("creativeType")
    val creativeType: String = "",
    @SerialName("creativeId")
    val creativeId: String? = null,
    @SerialName("position")
    val position: Int = 0,
    @SerialName("alg")
    val alg: String? = null,
    @SerialName("action")
    val action: String? = null,
    @SerialName("actionType")
    val actionType: String? = null,
    @SerialName("uiElement")
    val uiElement: HomepageUiElementData? = null,
    @SerialName("resources")
    val resources: List<HomepageResourceData> = emptyList()
)

@Serializable
data class HomepageResourceData(
    @SerialName("resourceId")
    val resourceId: String? = null,
    @SerialName("resourceType")
    val resourceType: String? = null,
    @SerialName("resourceUrl")
    val resourceUrl: String? = null,
    @SerialName("action")
    val action: String? = null,
    @SerialName("actionType")
    val actionType: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("alg")
    val alg: String? = null,
    @SerialName("position")
    val position: Int = 0,
    @SerialName("valid")
    val valid: Boolean = false,
    @SerialName("uiElement")
    val uiElement: HomepageUiElementData? = null,
    @SerialName("resourceExtInfo")
    val resourceExtInfo: JsonElement? = null
)

/** 资源标题，来自 uiElement.mainTitle */
fun HomepageResourceData.title(): String = uiElement?.mainTitle?.title.orEmpty()

/** 资源副标题（如歌单资源数为空、歌曲资源为推荐理由） */
fun HomepageResourceData.subTitle(): String = uiElement?.subTitle?.title.orEmpty()

/** 资源封面，来自 uiElement.image.imageUrl */
fun HomepageResourceData.coverUrl(): String = uiElement?.image?.imageUrl.orEmpty()

/**
 * resourceExtInfo.songData：歌单资源为完整歌单、风格推荐/新歌资源为歌曲。
 * decodeBean 已对整棵响应树做 fixLegacyFields（artists→ar、album→al、duration→dt），
 * songData 可直接映射到 SongData。
 */
fun HomepageResourceData.resourceSongData(): SongData? =
    (resourceExtInfo as? JsonObject)?.get("songData")?.let { el ->
        runCatching { SharedJson.decodeFromJsonElement<SongData>(el) }.getOrNull()
    }

/** resourceExtInfo.playCount：歌单资源播放数 */
fun HomepageResourceData.resourcePlayCount(): Long =
    ((resourceExtInfo as? JsonObject)?.get("playCount") as? JsonPrimitive)?.content?.toLongOrNull() ?: 0L

/** resourceExtInfo.ar（原 artists，已被 fixLegacyFields 重命名）：新歌速递等资源的歌手列表 */
fun HomepageResourceData.resourceArtists(): List<SimpleArtistData> =
    (resourceExtInfo as? JsonObject)?.get("ar")?.let { el ->
        runCatching { SharedJson.decodeFromJsonElement<List<SimpleArtistData>>(el) }.getOrNull()
    } ?: emptyList()

// ==================== 首页金刚区 / 专属候放 / 历史日推 ====================

@Serializable
data class DragonBallListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: List<DragonBallData> = emptyList(),
    @SerialName("message")
    val message: String? = null
)

/** GET homepage/dragon/ball：实测当前账号返回 data:[]，字段按上游常规结构 */
@Serializable
data class DragonBallData(
    @SerialName("iconId")
    val iconId: Long = 0,
    @SerialName("iconUrl")
    val iconUrl: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("url")
    val url: String = "",
    @SerialName("position")
    val position: Int = 0,
    @SerialName("testTabId")
    val testTabId: String? = null
)

@Serializable
data class PrivateContentListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("result")
    val result: List<PrivateContentData> = emptyList()
)

@Serializable
data class PrivateContentData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("copywriter")
    val copywriter: String = "",
    @SerialName("type")
    val type: Int = 0,
    @SerialName("url")
    val url: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("sPicUrl")
    val sPicUrl: String = "",
    @SerialName("time")
    val time: Long = 0
)

/**
 * GET history/recommend/songs 与 history/recommend/songs/detail 共用：
 * 外层 {code, data:{dates, songs}}，匿名时 songs 为 null，仅 dates 有值。
 */
@Serializable
data class HistoryRecommendWrap(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: HistoryRecommendSongsData? = null
)

@Serializable
data class HistoryRecommendSongsData(
    @SerialName("dates")
    val dates: List<String> = emptyList(),
    @SerialName("songs")
    val songs: List<SongData>? = null,
    @SerialName("purchaseUrl")
    val purchaseUrl: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("noHistoryMessage")
    val noHistoryMessage: String = ""
)

// ==================== 歌词 / 歌曲详情 / 播放增强 ====================

/**
 * GET lyric/new：lrc/tlyric/romalrc/klyric 与旧接口一致；带逐字歌词的歌曲
 * 额外返回 yrc（逐字）、ytlrc（逐字翻译）、yromalrc（逐字音译），无则缺省该键。
 * yrc.lyric 是内嵌 JSON 字符串（{"t":0,"c":[{"tx":"..."}]}）。
 */
@Serializable
data class LyricNewData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("lrc")
    val lrc: LrcData? = null,
    @SerialName("tlyric")
    val tlyric: LrcData? = null,
    @SerialName("romalrc")
    val romalrc: LrcData? = null,
    @SerialName("klyric")
    val klyric: LrcData? = null,
    @SerialName("yrc")
    val yrc: LrcData? = null,
    @SerialName("ytlrc")
    val ytlrc: LrcData? = null,
    @SerialName("yromalrc")
    val yromalrc: LrcData? = null,
    @SerialName("sgc")
    val sgc: Boolean = false,
    @SerialName("sfy")
    val sfy: Boolean = false,
    @SerialName("qfy")
    val qfy: Boolean = false,
    @SerialName("uncollected")
    val uncollected: Boolean = false,
    @SerialName("lyricUser")
    val lyricUser: JsonElement? = null
)

@Serializable
data class SongDetailListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("songs")
    val songs: List<SongData> = emptyList(),
    @SerialName("privileges")
    val privileges: List<PrivilegeData> = emptyList()
)

/** 权限/音质摘要，各接口 privileges 元素通用 */
@Serializable
data class PrivilegeData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("fee")
    val fee: Int = 0,
    @SerialName("payed")
    val payed: Int = 0,
    @SerialName("st")
    val st: Int = 0,
    @SerialName("pl")
    val pl: Int = 0,
    @SerialName("dl")
    val dl: Int = 0,
    @SerialName("sp")
    val sp: Int = 0,
    @SerialName("cp")
    val cp: Int = 0,
    @SerialName("subp")
    val subp: Int = 0,
    @SerialName("cs")
    val cs: Boolean = false,
    @SerialName("maxbr")
    val maxbr: Int = 0,
    @SerialName("fl")
    val fl: Int = 0,
    @SerialName("toast")
    val toast: Boolean = false,
    @SerialName("flag")
    val flag: Int = 0,
    @SerialName("preSell")
    val preSell: Boolean = false,
    @SerialName("playMaxbr")
    val playMaxbr: Int = 0,
    @SerialName("downloadMaxbr")
    val downloadMaxbr: Int = 0,
    @SerialName("maxBrLevel")
    val maxBrLevel: String = "",
    @SerialName("playMaxBrLevel")
    val playMaxBrLevel: String = "",
    @SerialName("downloadMaxBrLevel")
    val downloadMaxBrLevel: String = "",
    @SerialName("plLevel")
    val plLevel: String = "",
    @SerialName("dlLevel")
    val dlLevel: String = "",
    @SerialName("flLevel")
    val flLevel: String = ""
)

/** GET song/download/url/v1：data 结构与 song/url/v1 一致，复用 SongUrlData */
@Serializable
data class SongDownloadUrlData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: SongUrlData? = null
)

/** GET song/like/check：ids 为不可播/未收藏检查结果（实测需登录，ids 用重复参数传递） */
@Serializable
data class SongLikeCheckData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("ids")
    val ids: List<Long> = emptyList()
)

/** GET playmode/intelligence/list：实测当前测试账号始终返回 code 400「不支持该歌单类型」 */
@Serializable
data class PlayModeIntelligenceData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: List<PlayModeIntelligenceItem>? = null
)

@Serializable
data class PlayModeIntelligenceItem(
    @SerialName("active")
    val active: Boolean = false,
    @SerialName("recommended")
    val recommended: Boolean = false,
    @SerialName("reason")
    val reason: String? = null,
    @SerialName("songInfo")
    val songInfo: SongData? = null
)

// ==================== 排行榜 / 听歌数据 ====================

/** GET toplist/artist：{code, list:{artists, type, updateTime}} */
@Serializable
data class TopArtistListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("list")
    val list: TopArtistPageData? = null
)

@Serializable
data class TopArtistPageData(
    @SerialName("artists")
    val artists: List<TopArtistData> = emptyList(),
    @SerialName("type")
    val type: Int = 0,
    @SerialName("updateTime")
    val updateTime: Long = 0
)

@Serializable
data class TopArtistData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("img1v1Url")
    val img1v1Url: String = "",
    @SerialName("alias")
    val alias: List<String> = emptyList(),
    @SerialName("trans")
    val trans: String = "",
    @SerialName("briefDesc")
    val briefDesc: String = "",
    @SerialName("albumSize")
    val albumSize: Int = 0,
    @SerialName("musicSize")
    val musicSize: Int = 0,
    @SerialName("mvSize")
    val mvSize: Int = 0,
    @SerialName("score")
    val score: Long = 0,
    @SerialName("lastRank")
    val lastRank: Int = 0,
    @SerialName("topicPerson")
    val topicPerson: Int = 0,
    @SerialName("followed")
    val followed: Boolean = false
)

/** GET toplist/detail/v2：data 为榜单分类数组，每类下 list 为榜单条目 */
@Serializable
data class ToplistDetailV2Data(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: List<ToplistCategoryData> = emptyList()
)

@Serializable
data class ToplistCategoryData(
    @SerialName("name")
    val name: String = "",
    @SerialName("categoryCode")
    val categoryCode: String = "",
    @SerialName("displayType")
    val displayType: Int = 0,
    @SerialName("frontDisplayType")
    val frontDisplayType: Int = 0,
    @SerialName("targetUrl")
    val targetUrl: String? = null,
    @SerialName("list")
    val list: List<ToplistV2ItemData> = emptyList()
)

@Serializable
data class ToplistV2ItemData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("coverUrl")
    val coverUrl: String = "",
    @SerialName("coverType")
    val coverType: Int = 0,
    @SerialName("updateFrequency")
    val updateFrequency: String = "",
    @SerialName("targetType")
    val targetType: String = "",
    @SerialName("targetUrl")
    val targetUrl: String? = null,
    @SerialName("frontTargetUrl")
    val frontTargetUrl: String? = null,
    @SerialName("canPlay")
    val canPlay: Boolean = false,
    @SerialName("nameShowStyle")
    val nameShowStyle: String? = null,
    @SerialName("toplistCode")
    val toplistCode: String? = null,
    @SerialName("tracks")
    val tracks: List<ToplistTrackData> = emptyList()
)

@Serializable
data class ToplistTrackData(
    @SerialName("first")
    val first: String = "",
    @SerialName("second")
    val second: String = ""
)

/** GET top/list?id=xx：实测必须传榜单歌单 id（如 19723756 飙升榜），playlist 复用 PlaylistData */
@Serializable
data class TopListDetailData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("playlist")
    val playlist: PlaylistData? = null
)

/** GET top/song?type=：新歌速递，歌曲为旧字段结构（artists/album/duration），fixLegacyFields 归一后映射 */
@Serializable
data class NewSongListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: List<NewSongData> = emptyList()
)

@Serializable
data class NewSongData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("ar")
    val ar: List<SimpleArtistData> = emptyList(),
    @SerialName("al")
    val al: SimpleAlbumData = SimpleAlbumData(),
    @SerialName("dt")
    val dt: Long = 0,
    @SerialName("alias")
    val alias: List<String> = emptyList(),
    @SerialName("popularity")
    val popularity: Int = 0,
    @SerialName("mvid")
    val mvid: Long = 0,
    @SerialName("fee")
    val fee: Int = 0,
    @SerialName("starred")
    val starred: Boolean = false,
    @SerialName("playedNum")
    val playedNum: Long = 0,
    @SerialName("dayPlays")
    val dayPlays: Long = 0,
    @SerialName("privilege")
    val privilege: PrivilegeData? = null
)

@Serializable
data class SimpleArtistData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = ""
)

@Serializable
data class SimpleAlbumData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = ""
)

/**
 * GET top/album：实测返回 {code, hasMore, weekData, monthData}（周/月新碟榜），
 * 条目为旧字段专辑对象（artists），fixLegacyFields 归一后映射为 ar。
 * record/recent/album 的 data 结构兼容，复用本 bean。
 */
@Serializable
data class NewAlbumListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("weekData")
    val weekData: List<NewAlbumData> = emptyList(),
    @SerialName("monthData")
    val monthData: List<NewAlbumData> = emptyList()
)

@Serializable
data class NewAlbumData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("blurPicUrl")
    val blurPicUrl: String = "",
    @SerialName("publishTime")
    val publishTime: Long = 0,
    @SerialName("size")
    val size: Int = 0,
    @SerialName("company")
    val company: String = "",
    @SerialName("subType")
    val subType: String = "",
    @SerialName("type")
    val type: String = "",
    @SerialName("areaId")
    val areaId: Int = 0,
    @SerialName("paid")
    val paid: Boolean = false,
    @SerialName("exclusive")
    val exclusive: Boolean = false,
    @SerialName("isSub")
    val isSub: Boolean = false,
    @SerialName("alias")
    val alias: List<String> = emptyList(),
    @SerialName("description")
    val description: String = "",
    @SerialName("ar")
    val ar: List<SimpleArtistData> = emptyList()
)

// ==================== 最近播放 ====================

/**
 * GET record/recent/xxx：通用包装 {code, data:{list:[{playTime, resourceId, resourceType, banned, data}], total}}。
 * data 元素结构随 resourceType 变化，用泛型分别指定。
 */
@Serializable
data class RecentResourceListData<T>(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: RecentResourcePage<T>? = null
)

@Serializable
data class RecentResourcePage<T>(
    @SerialName("list")
    val list: List<RecentResourceItem<T>> = emptyList(),
    @SerialName("total")
    val total: Int = 0
)

@Serializable
data class RecentResourceItem<T>(
    @SerialName("playTime")
    val playTime: Long = 0,
    @SerialName("resourceId")
    val resourceId: Long = 0,
    @SerialName("resourceType")
    val resourceType: String = "",
    @SerialName("banned")
    val banned: Boolean = false,
    @SerialName("data")
    val data: T? = null
)

/** record/recent/playlist 元素 data */
@Serializable
data class RecentPlaylistData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("coverImgUrl")
    val coverImgUrl: String = "",
    @SerialName("uiPlaylistType")
    val uiPlaylistType: String = "",
    @SerialName("creator")
    val creator: ProfileData? = null
)

/** record/recent/video 元素 data（MLOG 类型，id 为字符串） */
@Serializable
data class RecentVideoData(
    @SerialName("id")
    val id: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("coverUrl")
    val coverUrl: String = "",
    @SerialName("duration")
    val duration: Long = 0,
    @SerialName("creator")
    val creator: ProfileData? = null
)

// ==================== 听歌数据 / 用户电台 / 私信 ====================

/** GET listen/data/total：{code, data:{totalDuration}} */
@Serializable
data class ListenDataTotalWrap(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: ListenDataTotalData? = null
)

@Serializable
data class ListenDataTotalData(
    @SerialName("totalDuration")
    val totalDuration: Long = 0
)

/** GET listen/data/year/report：{code, data:{displayYear, yearItems:[{year, playNum, playDuration}]}} */
@Serializable
data class ListenYearReportWrap(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: ListenYearReportData? = null
)

@Serializable
data class ListenYearReportData(
    @SerialName("displayYear")
    val displayYear: Int = 0,
    @SerialName("yearItems")
    val yearItems: List<ListenYearItemData> = emptyList()
)

@Serializable
data class ListenYearItemData(
    @SerialName("year")
    val year: Int = 0,
    @SerialName("playNum")
    val playNum: Long = 0,
    @SerialName("playDuration")
    val playDuration: Long = 0
)

/** GET user/dj：{code, count, more, programs} */
@Serializable
data class UserDjData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("more")
    val more: Boolean = false,
    @SerialName("programs")
    val programs: List<DjRadioData> = emptyList()
)

/** GET user/audio：{code, count, subCount, hasMore, djRadios} */
@Serializable
data class UserAudioData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("subCount")
    val subCount: Int = 0,
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("djRadios")
    val djRadios: List<DjRadioData> = emptyList()
)

/** GET send/song|playlist|album|text：成功 {code:200, id, newMsgs}，失败 {code:2202, message} */
@Serializable
data class SendMsgResultData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("id")
    val id: Long? = null
)

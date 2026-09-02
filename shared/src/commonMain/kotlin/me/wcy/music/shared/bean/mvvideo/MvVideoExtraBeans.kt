package me.wcy.music.shared.bean.mvvideo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.mv.bean.MvItem

/**
 * GET /mv/detail/info 与 GET /video/detail/info 共用结构
 */
@Serializable
data class MvDetailInfoData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("likedCount")
    val likedCount: Long = 0,
    @SerialName("shareCount")
    val shareCount: Long = 0,
    @SerialName("commentCount")
    val commentCount: Long = 0,
    @SerialName("liked")
    val liked: Boolean = false,
    @SerialName("subCount")
    val subCount: Long = 0
)

@Serializable
data class VideoCreator(
    @SerialName("userId")
    val userId: Long = 0,
    @SerialName("nickname")
    val nickname: String = "",
    @SerialName("avatarUrl")
    val avatarUrl: String = "",
    @SerialName("signature")
    val signature: String = ""
)

@Serializable
data class VideoUrlInfo(
    @SerialName("id")
    val id: String = "",
    @SerialName("url")
    val url: String = ""
)

/**
 * 视频对象（/video/detail.data、/related/allvideo.data、/video/timeline/recommend.datas[].data、
 * /video/group.datas 元素同构）
 */
@Serializable
data class VideoData(
    @SerialName("vid")
    val vid: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("coverUrl")
    val coverUrl: String = "",
    @SerialName("durationms")
    val durationms: Long = 0,
    @SerialName("publishTime")
    val publishTime: Long = 0,
    @SerialName("playTime")
    val playTime: Long = 0,
    @SerialName("praisedCount")
    val praisedCount: Long = 0,
    @SerialName("commentCount")
    val commentCount: Long = 0,
    @SerialName("shareCount")
    val shareCount: Long = 0,
    @SerialName("subscribeCount")
    val subscribeCount: Long = 0,
    @SerialName("width")
    val width: Int = 0,
    @SerialName("height")
    val height: Int = 0,
    @SerialName("praised")
    val praised: Boolean = false,
    @SerialName("subscribed")
    val subscribed: Boolean = false,
    @SerialName("creator")
    val creator: VideoCreator = VideoCreator(),
    @SerialName("urlInfo")
    val urlInfo: VideoUrlInfo? = null,
    @SerialName("videoGroup")
    val videoGroup: List<VideoGroupTag> = listOf()
)

/**
 * 视频所属分组标签（timeline 条目 data.videoGroup 元素）
 */
@Serializable
data class VideoGroupTag(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = ""
)

/**
 * GET /related/allvideo 返回 {code, message, data: List<VideoData>}
 */
@Serializable
data class RelatedVideoListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: List<VideoData> = listOf()
)

/**
 * GET /top/mv 返回 {code, data: List<MvItem>}
 */
@Serializable
data class TopMvData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: List<MvItem> = listOf()
)

/**
 * GET /artist/new/mv 返回 {code, message, data: {hasMore, newWorks}}
 */
@Serializable
data class ArtistNewMvWorks(
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("newWorks")
    val newWorks: List<MvItem> = listOf()
)

@Serializable
data class ArtistNewMvData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: ArtistNewMvWorks = ArtistNewMvWorks()
)

/**
 * GET /mv/exclusive/rcmd 返回 {code, data: List<MvItem>, more}
 */
@Serializable
data class MvExclusiveRcmdData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("more")
    val more: Boolean = false,
    @SerialName("data")
    val data: List<MvItem> = listOf()
)

@Serializable
data class VideoCategory(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("url")
    val url: String? = null,
    @SerialName("relatedVideoType")
    val relatedVideoType: String? = null,
    @SerialName("selectTab")
    val selectTab: Boolean = false
)

/**
 * GET /video/category/list 与 /video/group/list 共用结构
 * 返回 {code, message, data: List<VideoCategory>}
 */
@Serializable
data class VideoCategoryListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: List<VideoCategory> = listOf()
)

/**
 * GET /video/timeline/recommend 返回 {code, msg, hasmore, datas: List<VideoTimelineItem>}
 */
@Serializable
data class VideoTimelineItem(
    @SerialName("type")
    val type: Int = 0,
    @SerialName("displayed")
    val displayed: Boolean = false,
    @SerialName("alg")
    val alg: String? = null,
    @SerialName("data")
    val data: VideoData = VideoData()
)

@Serializable
data class VideoTimelineData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("hasmore")
    val hasmore: Boolean = false,
    @SerialName("datas")
    val datas: List<VideoTimelineItem> = listOf()
)

/**
 * GET /video/group 返回 {code, msg, hasmore, datas: List<VideoData>?}
 */
@Serializable
data class VideoGroupData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("hasmore")
    val hasmore: Boolean = false,
    @SerialName("datas")
    val datas: List<VideoData> = listOf()
)

/**
 * GET /video/detail 返回 {code, data: VideoData}
 */
@Serializable
data class VideoDetailData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: VideoData = VideoData()
)

@Serializable
data class VideoUrlItem(
    @SerialName("id")
    val id: String = "",
    @SerialName("url")
    val url: String = "",
    @SerialName("size")
    val size: Long = 0,
    @SerialName("validityTime")
    val validityTime: Long = 0,
    @SerialName("needPay")
    val needPay: Boolean = false,
    @SerialName("r")
    val r: Int = 0
)

/**
 * GET /video/url 返回 {code, urls: List<VideoUrlItem>}
 */
@Serializable
data class VideoUrlListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("urls")
    val urls: List<VideoUrlItem> = listOf()
)

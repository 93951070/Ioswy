package me.wcy.music.shared.net

import kotlinx.serialization.json.JsonElement
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.shared.bean.mvvideo.ArtistNewMvData
import me.wcy.music.shared.bean.mvvideo.MvDetailInfoData
import me.wcy.music.shared.bean.mvvideo.MvExclusiveRcmdData
import me.wcy.music.shared.bean.mvvideo.RelatedVideoListData
import me.wcy.music.shared.bean.mvvideo.TopMvData
import me.wcy.music.shared.bean.mvvideo.VideoCategoryListData
import me.wcy.music.shared.bean.mvvideo.VideoDetailData
import me.wcy.music.shared.bean.mvvideo.VideoGroupData
import me.wcy.music.shared.bean.mvvideo.VideoTimelineData
import me.wcy.music.shared.bean.mvvideo.VideoUrlListData

/**
 * MV/视频扩展接口。
 * MV 基础详情/播放地址/收藏列表见 MvNet，此处补充排行榜、相关视频与视频流接口。
 */
object MvVideoExtraNet {

    /**
     * MV 补充信息（点赞/分享/评论/收藏数）
     */
    suspend fun getMvDetailInfo(mvid: Long): NetResult<MvDetailInfoData> {
        return SharedJson.decodeBean(SharedNet.get(
                "mv/detail/info",
                params = listOf("mvid" to mvid)
            )
        )
    }

    /**
     * 相关视频，匿名时可能返回空数组
     */
    suspend fun getRelatedVideos(id: Long): NetResult<RelatedVideoListData> {
        return SharedJson.decodeBean(SharedNet.get(
                "related/allvideo",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * MV 排行榜
     * @param area 地区,如 内地/港台/欧美/日本/韩国，为空表示全部
     */
    suspend fun getTopMv(
        limit: Int = 30,
        area: String = ""
    ): TopMvData {
        return SharedJson.decodeBean(SharedNet.get(
                "top/mv",
                params = listOf(
                    "limit" to limit,
                    "area" to area
                )
            )
        )
    }

    /**
     * 歌手新 MV，未订阅歌手时 newWorks 可能为空
     */
    suspend fun getArtistNewMv(
        artistId: Long,
        limit: Int = 20
    ): ArtistNewMvData {
        return SharedJson.decodeBean(SharedNet.get(
                "artist/new/mv",
                params = listOf(
                    "limit" to limit,
                    "artistId" to artistId
                )
            )
        )
    }

    /**
     * 专属定制 MV
     */
    suspend fun getMvExclusiveRcmd(limit: Int = 10): MvExclusiveRcmdData {
        return SharedJson.decodeBean(SharedNet.get(
                "mv/exclusive/rcmd",
                params = listOf("limit" to limit)
            )
        )
    }

    /**
     * 视频分类标签
     */
    suspend fun getVideoCategoryList(): VideoCategoryListData {
        return SharedJson.decodeBean(SharedNet.get("video/category/list"))
    }

    /**
     * 推荐视频流；url 播放地址需要 -b cookie，匿名可拿到视频元信息
     */
    suspend fun getVideoTimelineRecommend(
        offset: Long? = null,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): VideoTimelineData {
        return SharedJson.decodeBean(SharedNet.get(
                "video/timeline/recommend",
                params = listOf(
                    "offset" to offset,
                    "timestamp" to timestamp
                )
            )
        )
    }

    /**
     * 视频标签分组（与 video/category/list 同构）
     */
    suspend fun getVideoGroupList(): VideoCategoryListData {
        return SharedJson.decodeBean(SharedNet.get("video/group/list"))
    }

    /**
     * 按分组取视频，匿名时 datas 可能为 null
     */
    suspend fun getVideoGroup(
        id: Long,
        offset: Long? = null
    ): VideoGroupData {
        return SharedJson.decodeBean(SharedNet.get(
                "video/group",
                params = listOf(
                    "id" to id,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 视频详情
     */
    suspend fun getVideoDetail(vid: String): VideoDetailData {
        return SharedJson.decodeBean(SharedNet.get(
                "video/detail",
                params = listOf("id" to vid)
            )
        )
    }

    /**
     * 视频点赞/分享/评论数，注意参数名为 vid
     */
    suspend fun getVideoDetailInfo(vid: String): NetResult<MvDetailInfoData> {
        return SharedJson.decodeBean(SharedNet.get(
                "video/detail/info",
                params = listOf("vid" to vid)
            )
        )
    }

    /**
     * 视频播放地址
     * @param res 分辨率,如 1080
     */
    suspend fun getVideoUrl(
        id: String,
        res: Int = 1080
    ): VideoUrlListData {
        return SharedJson.decodeBean(SharedNet.get(
                "video/url",
                params = listOf(
                    "id" to id,
                    "res" to res
                )
            )
        )
    }

    /**
     * 收藏/取消收藏视频
     * @param t 类型,1:收藏,其他:取消收藏
     */
    suspend fun subVideo(
        id: String,
        t: Int,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.post(
                "video/sub",
                params = listOf(
                    "id" to id,
                    "t" to t,
                    "timestamp" to timestamp
                )
            )
        )
    }
}

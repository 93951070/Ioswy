package me.wcy.music.shared.net

import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.SongData
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.dj.bean.DjRadioData
import me.wcy.music.shared.bean.home.ListenDataTotalWrap
import me.wcy.music.shared.bean.home.ListenYearReportWrap
import me.wcy.music.shared.bean.home.NewAlbumData
import me.wcy.music.shared.bean.home.NewAlbumListData
import me.wcy.music.shared.bean.home.NewSongListData
import me.wcy.music.shared.bean.home.RecentPlaylistData
import me.wcy.music.shared.bean.home.RecentResourceListData
import me.wcy.music.shared.bean.home.RecentVideoData
import me.wcy.music.shared.bean.home.SendMsgResultData
import me.wcy.music.shared.bean.home.TopArtistListData
import me.wcy.music.shared.bean.home.TopListDetailData
import me.wcy.music.shared.bean.home.ToplistDetailV2Data
import me.wcy.music.shared.bean.home.UserAudioData
import me.wcy.music.shared.bean.home.UserDjData

/**
 * 排行榜、听歌数据、最近播放、私信分享接口。
 * record/recent 系列与 listen/data 系列需要登录 cookie。
 */
object ListenDataNet {

    /**
     * 歌手榜。实测后端忽略 limit/offset 分页参数，始终返回全量列表。
     */
    suspend fun getTopArtistList(limit: Int = 100, offset: Int = 0): TopArtistListData {
        return SharedJson.decodeBean(SharedNet.get(
                "toplist/artist",
                params = listOf(
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 排行榜详情 v2：data 为榜单分类数组（榜单推荐/官方榜等），每类下 list 为榜单条目。
     */
    suspend fun getToplistDetailV2(): ToplistDetailV2Data {
        return SharedJson.decodeBean(SharedNet.get("toplist/detail/v2"))
    }

    /**
     * 排行榜歌单。实测必须传榜单歌单 id（如 19723756 飙升榜），idx 调用已被上游禁用，
     * 返回 {code, playlist}。
     */
    suspend fun getTopList(id: Long): TopListDetailData {
        return SharedJson.decodeBean(SharedNet.get(
                "top/list",
                params = listOf(
                    "id" to id
                )
            )
        )
    }

    /**
     * 新歌速递。type：0 全部 / 7 华语 / 96 欧美 / 8 日本 / 16 韩国。
     */
    suspend fun getTopSongList(type: Int = 7): NewSongListData {
        return SharedJson.decodeBean(SharedNet.get(
                "top/song",
                params = listOf(
                    "type" to type
                )
            )
        )
    }

    /**
     * 新碟上架。area：ALL/ZH/EA/KR/JP，type：new/hot/original。
     * 实测返回 weekData/monthData（周/月新碟榜），无 albums 字段。
     */
    suspend fun getNewAlbumList(
        area: String = "ALL",
        type: String = "new",
        limit: Int = 50,
        offset: Int = 0,
    ): NewAlbumListData {
        return SharedJson.decodeBean(SharedNet.get(
                "top/album",
                params = listOf(
                    "area" to area,
                    "type" to type,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /** 最近播放-歌曲（需登录） */
    suspend fun getRecentSongs(limit: Int = 100): RecentResourceListData<SongData> {
        return SharedJson.decodeBean(SharedNet.get(
                "record/recent/song",
                params = listOf(
                    "limit" to limit
                )
            )
        )
    }

    /** 最近播放-专辑（需登录） */
    suspend fun getRecentAlbums(limit: Int = 100): RecentResourceListData<NewAlbumData> {
        return SharedJson.decodeBean(SharedNet.get(
                "record/recent/album",
                params = listOf(
                    "limit" to limit
                )
            )
        )
    }

    /** 最近播放-歌单（需登录） */
    suspend fun getRecentPlaylists(limit: Int = 100): RecentResourceListData<RecentPlaylistData> {
        return SharedJson.decodeBean(SharedNet.get(
                "record/recent/playlist",
                params = listOf(
                    "limit" to limit
                )
            )
        )
    }

    /** 最近播放-电台（需登录） */
    suspend fun getRecentDjRadios(limit: Int = 100): RecentResourceListData<DjRadioData> {
        return SharedJson.decodeBean(SharedNet.get(
                "record/recent/dj",
                params = listOf(
                    "limit" to limit
                )
            )
        )
    }

    /** 最近播放-视频 MLOG（需登录） */
    suspend fun getRecentVideos(limit: Int = 100): RecentResourceListData<RecentVideoData> {
        return SharedJson.decodeBean(SharedNet.get(
                "record/recent/video",
                params = listOf(
                    "limit" to limit
                )
            )
        )
    }

    /** 听歌总览（需登录），实测仅 totalDuration 有值 */
    suspend fun getListenDataTotal(): ListenDataTotalWrap {
        return SharedJson.decodeBean(SharedNet.get("listen/data/total"))
    }

    /** 年度听歌报告（需登录），实测返回逐年 playNum/playDuration 汇总 */
    suspend fun getListenYearReport(): ListenYearReportWrap {
        return SharedJson.decodeBean(SharedNet.get("listen/data/year/report"))
    }

    /** 用户创建/收藏的电台 */
    suspend fun getUserDj(uid: Long): UserDjData {
        return SharedJson.decodeBean(SharedNet.get(
                "user/dj",
                params = listOf(
                    "uid" to uid
                )
            )
        )
    }

    /** 用户的声音（播客） */
    suspend fun getUserAudio(uid: Long): UserAudioData {
        return SharedJson.decodeBean(SharedNet.get(
                "user/audio",
                params = listOf(
                    "uid" to uid
                )
            )
        )
    }

    /** 私信分享歌曲（需登录）。实测参数名为 user_ids，多个用重复参数传递 */
    suspend fun sendSong(userIds: List<Long>, id: Long, msg: String = ""): SendMsgResultData {
        return SharedJson.decodeBean(SharedNet.get(
                "send/song",
                params = userIds.map { "user_ids" to it } + listOf("id" to id, "msg" to msg)
            )
        )
    }

    /** 私信分享歌单（需登录） */
    suspend fun sendPlaylist(userIds: List<Long>, id: Long, msg: String = ""): SendMsgResultData {
        return SharedJson.decodeBean(SharedNet.get(
                "send/playlist",
                params = userIds.map { "user_ids" to it } + listOf("id" to id, "msg" to msg)
            )
        )
    }

    /** 私信分享专辑（需登录） */
    suspend fun sendAlbum(userIds: List<Long>, id: Long, msg: String = ""): SendMsgResultData {
        return SharedJson.decodeBean(SharedNet.get(
                "send/album",
                params = userIds.map { "user_ids" to it } + listOf("id" to id, "msg" to msg)
            )
        )
    }

    /** 私信发送文字（需登录） */
    suspend fun sendText(userIds: List<Long>, msg: String): SendMsgResultData {
        return SharedJson.decodeBean(SharedNet.get(
                "send/text",
                params = userIds.map { "user_ids" to it } + listOf("msg" to msg)
            )
        )
    }
}

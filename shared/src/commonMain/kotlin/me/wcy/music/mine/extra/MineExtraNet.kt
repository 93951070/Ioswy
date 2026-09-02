package me.wcy.music.mine.extra

import kotlinx.serialization.json.JsonElement
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.mine.extra.bean.AlbumSubItem
import me.wcy.music.mine.extra.bean.ArtistSubItem
import me.wcy.music.mine.extra.bean.CloudData
import me.wcy.music.mine.extra.bean.MsgData
import me.wcy.music.mine.extra.bean.MvSubItem
import me.wcy.music.mine.extra.bean.RecordData
import me.wcy.music.mine.extra.bean.SubListData
import me.wcy.music.mine.extra.bean.UserLevelData
import me.wcy.music.shared.net.NetResult
import me.wcy.music.shared.net.SharedNet

/**
 * 我的-扩展功能接口：最近播放、收藏、云盘、消息、签到、等级。
 */
object MineExtraNet {

    /**
     * 听歌排行
     * @param uid 用户 id
     * @param type 0: 每周, 1: 所有时间
     */
    suspend fun getRecentPlaySongs(
        uid: Long,
        type: String = "1",
        timestamp: Long = SharedNet.currentTimeMillis()
    ): RecordData {
        return SharedJson.decodeBean(SharedNet.get(
                "user/record",
                params = listOf(
                    "uid" to uid,
                    "type" to type,
                    "timestamp" to timestamp
                )
            )
        )
    }

    /**
     * 每日签到
     * @param type 0: 安卓, 1: ios
     */
    suspend fun dailySignin(
        type: Int = 0,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.get(
                "daily_signin",
                params = listOf(
                    "type" to type,
                    "timestamp" to timestamp
                )
            )
        )
    }

    /**
     * 获取用户等级
     */
    suspend fun getUserLevel(timestamp: Long = SharedNet.currentTimeMillis()): UserLevelData {
        return SharedJson.decodeBean(SharedNet.get(
                "user/level",
                params = listOf("timestamp" to timestamp)
            )
        )
    }

    /**
     * 私信列表
     */
    suspend fun getPrivateMsg(limit: Int = 30, offset: Int = 0): MsgData {
        return SharedJson.decodeBean(SharedNet.get(
                "msg/private",
                params = listOf(
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 评论消息列表
     */
    suspend fun getCommentMsg(limit: Int = 30, offset: Int = 0): MsgData {
        return SharedJson.decodeBean(SharedNet.get(
                "msg/comments",
                params = listOf(
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 通知消息列表
     */
    suspend fun getNoticeMsg(limit: Int = 30, offset: Int = 0): MsgData {
        return SharedJson.decodeBean(SharedNet.get(
                "msg/notices",
                params = listOf(
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 云盘歌曲列表
     */
    suspend fun getUserCloud(limit: Int = 30, offset: Int = 0): CloudData {
        return SharedJson.decodeBean(SharedNet.get(
                "user/cloud",
                params = listOf(
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 删除云盘歌曲
     * @param id 歌曲 id
     */
    suspend fun delCloudSong(id: Long): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.get(
                "user/cloud/del",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 收藏的歌手列表
     */
    suspend fun getArtistSublist(limit: Int = 50, offset: Int = 0): SubListData<ArtistSubItem> {
        return SharedJson.decodeBean(SharedNet.get(
                "artist/sublist",
                params = listOf(
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 收藏的专辑列表
     */
    suspend fun getAlbumSublist(limit: Int = 50, offset: Int = 0): SubListData<AlbumSubItem> {
        return SharedJson.decodeBean(SharedNet.get(
                "album/sublist",
                params = listOf(
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 收藏的 MV 列表
     */
    suspend fun getMvSublist(limit: Int = 50, offset: Int = 0): SubListData<MvSubItem> {
        return SharedJson.decodeBean(SharedNet.get(
                "mv/sublist",
                params = listOf(
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 私信详情：与指定用户的聊天记录。uid 为对方 userId，before 传上一页最后一条时间戳做分页。
     */
    suspend fun getPrivateMsgHistory(
        uid: Long,
        limit: Int = 30,
        before: Long = 0
    ): MsgData {
        return SharedJson.decodeBean(SharedNet.get(
                "msg/private/history",
                params = listOf(
                    "uid" to uid,
                    "limit" to limit,
                    "before" to before
                )
            )
        )
    }
}

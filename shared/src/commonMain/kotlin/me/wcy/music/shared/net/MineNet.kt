package me.wcy.music.shared.net

import kotlinx.serialization.json.JsonElement
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.discover.playlist.square.bean.PlaylistListData
import me.wcy.music.mine.collect.song.bean.CollectSongResult
import me.wcy.music.service.likesong.bean.LikeSongListData

/**
 * 我的-账户相关接口。
 */
object MineNet {

    suspend fun getUserPlaylist(
        uid: Long,
        limit: Int = 1000,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): PlaylistListData {
        return SharedJson.decodeFromString(
            SharedNet.post(
                "user/playlist",
                params = listOf(
                    "uid" to uid,
                    "limit" to limit,
                    "timestamp" to timestamp
                )
            )
        )
    }

    /**
     * 收藏/取消收藏歌单
     * @param id 歌单 id
     * @param t 类型,1:收藏,2:取消收藏
     */
    suspend fun collectPlaylist(
        id: Long,
        t: Int,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): NetResult<JsonElement> {
        return SharedJson.decodeFromString(
            SharedNet.post(
                "playlist/subscribe",
                params = listOf(
                    "id" to id,
                    "t" to t,
                    "timestamp" to timestamp
                )
            )
        )
    }

    /**
     * 对歌单添加歌曲
     * @param op 从歌单增加单曲为 add, 删除为 del
     * @param pid 歌单 id
     * @param tracks 歌曲 id,可多个,用逗号隔开
     */
    suspend fun collectSong(
        pid: Long,
        tracks: String,
        op: String = "add",
        timestamp: Long = SharedNet.currentTimeMillis()
    ): CollectSongResult {
        return SharedJson.decodeFromString(
            SharedNet.post(
                "playlist/tracks",
                params = listOf(
                    "pid" to pid,
                    "tracks" to tracks,
                    "op" to op,
                    "timestamp" to timestamp
                )
            )
        )
    }

    /**
     * 喜欢音乐
     * @param id 歌曲 id
     * @param like 默认为 true 即喜欢 , 若传 false, 则取消喜欢
     */
    suspend fun likeSong(
        id: Long,
        like: Boolean = true,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): NetResult<JsonElement> {
        return SharedJson.decodeFromString(
            SharedNet.post(
                "like",
                params = listOf(
                    "id" to id,
                    "like" to like,
                    "timestamp" to timestamp
                )
            )
        )
    }

    /**
     * 喜欢音乐列表
     */
    suspend fun getMyLikeSongList(
        uid: Long,
        timestamp: Long = SharedNet.currentTimeMillis()
    ): LikeSongListData {
        return SharedJson.decodeFromString(
            SharedNet.post(
                "likelist",
                params = listOf(
                    "uid" to uid,
                    "timestamp" to timestamp
                )
            )
        )
    }
}

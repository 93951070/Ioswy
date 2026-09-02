package me.wcy.music.shared.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean

@Serializable
data class PlaylistOpData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("message")
    val message: String? = null,
)

@Serializable
data class PlaylistCreateData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("id")
    val id: Long = 0,
)

@Serializable
data class PlaylistDynamicData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("subscribed")
    val subscribed: Boolean = false,
    @SerialName("playCount")
    val playCount: Long = 0,
    @SerialName("bookedCount")
    val bookCount: Long = 0,
    @SerialName("commentCount")
    val commentCount: Long = 0,
    @SerialName("shareCount")
    val shareCount: Long = 0,
    @SerialName("followed")
    val followed: Boolean = false,
    @SerialName("remarkName")
    val remarkName: String? = null,
    @SerialName("gradeStatus")
    val gradeStatus: String = "",
)

@Serializable
data class PlaylistSubscribersData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("more")
    val more: Boolean = false,
    @SerialName("total")
    val total: Int = 0,
    @SerialName("subscribers")
    val subscribers: List<ProfileData> = emptyList(),
)

@Serializable
data class PlaylistRcmdData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: PlaylistRcmdInner? = null,
)

@Serializable
data class PlaylistRcmdInner(
    @SerialName("rcmdTitle")
    val rcmdTitle: String = "",
    @SerialName("jumpUrl")
    val jumpUrl: String = "",
    @SerialName("recPlaylist")
    val recPlaylist: List<PlaylistRcmdItem> = emptyList(),
)

@Serializable
data class PlaylistRcmdItem(
    @SerialName("playlist")
    val playlist: PlaylistData = PlaylistData(),
    @SerialName("alg")
    val alg: String = "",
)

@Serializable
data class PlaylistHqTagsData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("tags")
    val tags: List<PlaylistHqTag> = emptyList(),
)

@Serializable
data class PlaylistHqTag(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("type")
    val type: Int = 0,
    @SerialName("category")
    val category: Int = 0,
    @SerialName("hot")
    val hot: Boolean = false,
)

@Serializable
data class PlaylistMylikeData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: PlaylistMylikeInner? = null,
)

@Serializable
data class PlaylistMylikeInner(
    @SerialName("feeds")
    val feeds: List<PlaylistMylikeFeed> = emptyList(),
    @SerialName("more")
    val more: Boolean = false,
    @SerialName("time")
    val time: Long = 0,
)

@Serializable
data class PlaylistMylikeFeed(
    @SerialName("mlogBaseData")
    val mlogBaseData: MylikeMlogBase = MylikeMlogBase(),
    @SerialName("userProfile")
    val userProfile: ProfileData = ProfileData(),
    @SerialName("status")
    val status: Int = 0,
    @SerialName("shareUrl")
    val shareUrl: String = "",
)

@Serializable
data class MylikeMlogBase(
    @SerialName("id")
    val id: String = "",
    @SerialName("userId")
    val userId: Long = 0,
    @SerialName("type")
    val type: Int = 0,
    @SerialName("originalTitle")
    val originalTitle: String = "",
    @SerialName("text")
    val text: String = "",
    @SerialName("coverUrl")
    val coverUrl: String = "",
    @SerialName("pubTime")
    val pubTime: Long = 0,
    @SerialName("duration")
    val duration: Long = 0,
    @SerialName("threadId")
    val threadId: String = "",
)

@Serializable
data class PlaylistCoverUpdateData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: PlaylistCoverUpdateInner? = null,
)

@Serializable
data class PlaylistCoverUpdateInner(
    @SerialName("imgId")
    val imgId: String = "",
    @SerialName("url")
    val url: String = "",
    @SerialName("url_pre")
    val urlPre: String = "",
)

/**
 * 歌单管理接口（需登录态，SharedNet 自动携带 cookie）。
 */
object PlaylistManageNet {

    /**
     * 创建歌单。privacy：0 普通歌单 / 10 隐私歌单；type 固定 NORMAL。
     */
    suspend fun createPlaylist(
        name: String,
        privacy: Int = 0,
        type: String = "NORMAL",
        description: String = "",
    ): PlaylistCreateData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/create",
                params = listOf(
                    "name" to name,
                    "privacy" to privacy,
                    "type" to type,
                    "description" to description
                )
            )
        )
    }

    /**
     * 删除歌单。
     */
    suspend fun deletePlaylist(id: Long): PlaylistOpData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/delete",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 修改歌单名。
     */
    suspend fun updatePlaylistName(id: Long, name: String): PlaylistOpData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/name/update",
                params = listOf(
                    "id" to id,
                    "name" to name
                )
            )
        )
    }

    /**
     * 修改歌单描述。上游参数名为 desc（传 description 会返回 400）。
     */
    suspend fun updatePlaylistDesc(id: Long, desc: String): PlaylistOpData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/desc/update",
                params = listOf(
                    "id" to id,
                    "desc" to desc
                )
            )
        )
    }

    /**
     * 修改歌单标签，tags 为逗号分隔字符串。
     */
    suspend fun updatePlaylistTags(id: Long, tags: String): PlaylistOpData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/tags/update",
                params = listOf(
                    "id" to id,
                    "tags" to tags
                )
            )
        )
    }

    /**
     * 更新歌单封面。
     * ponytail: 上游要求 multipart 文件字段 imgFile，SharedNet 暂无文件上传能力，
     * 当前以 query 参数调用会返回 400 imgFile is required；
     * SharedNet 增加上传能力后把 imgPath 换成 multipart 文件即可。
     */
    suspend fun updatePlaylistCover(
        id: Long,
        imgPath: String,
        imgSize: Int = 0,
        imgX: Int = 0,
        imgY: Int = 0,
    ): PlaylistCoverUpdateData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/cover/update",
                params = listOf(
                    "id" to id,
                    "imgSize" to imgSize,
                    "imgX" to imgX,
                    "imgY" to imgY,
                    "imgFile" to imgPath
                )
            )
        )
    }

    /**
     * 添加歌曲到歌单。op 固定 add，tracks 为逗号分隔歌曲 id。
     */
    suspend fun addTracks(pid: Long, tracks: String): PlaylistOpData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/track/add",
                params = listOf(
                    "op" to "add",
                    "pid" to pid,
                    "tracks" to tracks,
                    "imme" to "true"
                )
            )
        )
    }

    /**
     * 从歌单删除歌曲。op 固定 del，tracks 为逗号分隔歌曲 id。
     */
    suspend fun deleteTracks(pid: Long, tracks: String): PlaylistOpData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/track/delete",
                params = listOf(
                    "op" to "del",
                    "pid" to pid,
                    "tracks" to tracks,
                    "imme" to "true"
                )
            )
        )
    }

    /**
     * 歌单内歌曲排序，ids 为逗号分隔歌曲 id（按新顺序）。
     */
    suspend fun updateOrder(pid: Long, ids: String): PlaylistOpData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/order/update",
                params = listOf(
                    "pid" to pid,
                    "ids" to ids
                )
            )
        )
    }

    /**
     * 隐私歌单设为公开（上游 privacy 固定传 0）。
     */
    suspend fun updatePrivacy(id: Long, privacy: Int = 0): PlaylistOpData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/privacy",
                params = listOf(
                    "id" to id,
                    "privacy" to privacy
                )
            )
        )
    }

    /**
     * 歌单打卡（播放数 +1）。实测环境返回 code -460 网络风控，需容忍失败。
     */
    suspend fun updatePlaycount(id: Long): PlaylistOpData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/update/playcount",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 歌单动态信息：收藏数/播放数/分享数/评论数/是否已收藏。
     */
    suspend fun getPlaylistDynamic(id: Long): PlaylistDynamicData {
        return SharedJson.decodeBean(SharedNet.get(
                "playlist/detail/dynamic",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 歌单收藏者列表。
     */
    suspend fun getPlaylistSubscribers(
        id: Long,
        limit: Int = 30,
        offset: Int = 0,
    ): PlaylistSubscribersData {
        return SharedJson.decodeBean(SharedNet.get(
                "playlist/subscribers",
                params = listOf(
                    "id" to id,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    /**
     * 相关歌单推荐（"喜欢这个歌单的用户也听了"）。
     */
    suspend fun getPlaylistRcmd(
        id: Long,
        limit: Int = 10,
    ): PlaylistRcmdData {
        return SharedJson.decodeBean(SharedNet.get(
                "playlist/detail/rcmd/get",
                params = listOf(
                    "id" to id,
                    "limit" to limit
                )
            )
        )
    }

    /**
     * 精品歌单标签列表。
     */
    suspend fun getHighqualityTags(): PlaylistHqTagsData {
        return SharedJson.decodeBean(SharedNet.get("playlist/highquality/tags"))
    }

    /**
     * 我喜欢的歌单（MLog feed 流）。需登录态，匿名返回 code 302。
     */
    suspend fun getMylikePlaylists(limit: Int = 30): PlaylistMylikeData {
        return SharedJson.decodeBean(SharedNet.get(
                "playlist/mylike",
                params = listOf("limit" to limit)
            )
        )
    }
}

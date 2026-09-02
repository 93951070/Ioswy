package me.wcy.music.shared.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SharedJson

@Serializable
data class CatlistData(
    val code: Int = 0,
    val categories: Map<String, String> = emptyMap(),
    val sub: List<CatSub> = emptyList()
)

@Serializable
data class CatSub(
    val name: String = "",
    val hot: Boolean = false,
    val category: Int = 0
)

@Serializable
data class PlaylistListData(
    val code: Int = 0,
    val playlists: List<PlaylistData> = emptyList(),
    @SerialName("more") val more: Boolean = false,
    @SerialName("total") val total: Int = 0
)

/**
 * 发现页-歌单相关公开接口。
 */
object PlaylistApi {

    suspend fun getCatlist(): CatlistData {
        return SharedJson.decodeFromString(SharedNet.get("playlist/catlist"))
    }

    suspend fun getTopPlaylists(cat: String, limit: Int, offset: Int): PlaylistListData {
        return SharedJson.decodeFromString(
            SharedNet.get(
                "top/playlist",
                params = listOf(
                    "cat" to cat,
                    "limit" to limit,
                    "offset" to offset,
                    "order" to "hot"
                )
            )
        )
    }
}

package me.wcy.music.shared.net

import kotlinx.serialization.json.JsonElement
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.shared.bean.home.DragonBallListData
import me.wcy.music.shared.bean.home.HistoryRecommendWrap
import me.wcy.music.shared.bean.home.HomepageBlockPageData
import me.wcy.music.shared.bean.home.PrivateContentListData

/**
 * 首页区块与推荐扩展接口。
 */
object HomepageNet {

    /**
     * 首页-发现页 block 页面。实测匿名仅返回 3 个 block：
     * HOMEPAGE_BLOCK_PLAYLIST_RCMD（推荐歌单）、HOMEPAGE_BLOCK_STYLE_RCMD（风格推荐）、
     * HOMEPAGE_BLOCK_NEW_ALBUM_NEW_SONG（新歌新碟）。登录后 block 数量更多。
     */
    suspend fun getHomepageBlockPage(
        refresh: Boolean = false,
        cursor: String? = null,
    ): HomepageBlockPageData {
        return SharedJson.decodeBean(SharedNet.get(
                "homepage/block/page",
                params = listOf(
                    "refresh" to refresh,
                    "cursor" to cursor
                )
            )
        )
    }

    /**
     * 首页金刚区（圆形入口）。实测当前后端匿名/登录均返回 data:[]，等待上游有数据时复用。
     */
    suspend fun getDragonBall(): DragonBallListData {
        return SharedJson.decodeBean(SharedNet.get("homepage/dragon/ball"))
    }

    /**
     * 专属候放/听歌定制内容。
     */
    suspend fun getPrivateContentList(limit: Int = 10): PrivateContentListData {
        return SharedJson.decodeBean(SharedNet.get(
                "personalized/privatecontent/list",
                params = listOf(
                    "limit" to limit
                )
            )
        )
    }

    /**
     * 历史推荐日期列表，匿名仅返回 dates，songs 为 null。
     */
    suspend fun getHistoryRecommendSongs(): HistoryRecommendWrap {
        return SharedJson.decodeBean(SharedNet.get("history/recommend/songs"))
    }

    /**
     * 某日推荐歌曲详情，date 取 getHistoryRecommendSongs 返回的日期（如 2026-09-01）。
     */
    suspend fun getHistoryRecommendSongsDetail(date: String): HistoryRecommendWrap {
        return SharedJson.decodeBean(SharedNet.get(
                "history/recommend/songs/detail",
                params = listOf(
                    "date" to date
                )
            )
        )
    }

    /**
     * 每日推荐歌曲「不感兴趣」。实测无更多可标记时返回 code 432「今日暂无更多推荐」，
     * 调用方以 code == 200 判定成功。
     */
    suspend fun dislikeRecommendSong(id: Long): NetResult<JsonElement> {
        return SharedJson.decodeBean(SharedNet.get(
                "recommend/songs/dislike",
                params = listOf(
                    "id" to id
                )
            )
        )
    }
}

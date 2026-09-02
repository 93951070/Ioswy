package me.wcy.music.shared.net

import me.wcy.music.common.bean.LrcDataWrap
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.common.bean.SongData
import me.wcy.music.common.bean.SongUrlData
import me.wcy.music.discover.artist.bean.ArtistListData
import me.wcy.music.discover.banner.BannerListData
import me.wcy.music.discover.comment.bean.CommentData
import me.wcy.music.discover.comment.bean.CommentOpData
import me.wcy.music.discover.fm.bean.PersonalFmData
import me.wcy.music.dj.bean.DjListData
import me.wcy.music.discover.playlist.detail.bean.PlaylistDetailData
import me.wcy.music.discover.playlist.detail.bean.SongListData
import me.wcy.music.discover.playlist.square.bean.CatlistData
import me.wcy.music.discover.playlist.square.bean.PlaylistListData
import me.wcy.music.discover.playlist.square.bean.PlaylistTagListData
import me.wcy.music.discover.recommend.song.bean.RecommendSongListData

/**
 * 发现页接口。
 */
object DiscoverNet {

    suspend fun getRecommendSongs(): NetResult<RecommendSongListData> {
        return SharedJson.decodeBean(SharedNet.post("recommend/songs"))
    }

    suspend fun getRecommendPlaylists(): PlaylistListData {
        return SharedJson.decodeBean(SharedNet.post("recommend/resource"))
    }

    suspend fun getSongUrl(
        id: Long,
        level: String,
    ): NetResult<List<SongUrlData>> {
        return SharedJson.decodeBean(SharedNet.post(
                "song/url/v1",
                params = listOf(
                    "id" to id,
                    "level" to level
                )
            )
        )
    }

    /** 下载用流地址：song/url/v1 standard 档，无版权/取不到时返回 null */
    suspend fun getSongDownloadUrl(songId: Long): String? {
        if (songId <= 0) return null
        return runCatching { getSongUrl(songId, "standard") }
            .getOrNull()
            ?.takeIf { it.isSuccessWithData() }
            ?.data
            ?.firstOrNull()
            ?.url
            ?.takeIf { it.isNotBlank() }
    }

    suspend fun getLrc(
        id: Long,
    ): LrcDataWrap {
        return SharedJson.decodeBean(SharedNet.post(
                "lyric",
                params = listOf(
                    "id" to id
                )
            )
        )
    }

    suspend fun getPlaylistDetail(
        id: Long,
    ): PlaylistDetailData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/detail",
                params = listOf(
                    "id" to id
                )
            )
        )
    }

    suspend fun getPlaylistSongList(
        id: Long,
        limit: Int? = null,
        offset: Int? = null,
        timestamp: Long? = null
    ): SongListData {
        return SharedJson.decodeBean(SharedNet.post(
                "playlist/track/all",
                params = listOf(
                    "id" to id,
                    "limit" to limit,
                    "offset" to offset,
                    "timestamp" to timestamp
                )
            )
        )
    }

    suspend fun getPlaylistTagList(): PlaylistTagListData {
        return SharedJson.decodeBean(SharedNet.post("playlist/hot"))
    }

    suspend fun getPlaylistList(
        cat: String,
        limit: Int,
        offset: Int,
    ): PlaylistListData {
        return SharedJson.decodeBean(SharedNet.post(
                "top/playlist",
                params = listOf(
                    "cat" to cat,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    suspend fun getRankingList(): PlaylistListData {
        val raw: PlaylistListData = SharedJson.decodeBean(SharedNet.post("toplist"))
        return raw.copy(playlists = raw.playlists + raw.list)
    }

    suspend fun getBannerList(): BannerListData {
        return SharedJson.decodeBean(SharedNet.get(
                "banner",
                params = listOf(
                    "type" to 2
                )
            )
        )
    }

    suspend fun getPersonalFm(
        timestamp: Long = SharedNet.currentTimeMillis()
    ): PersonalFmData {
        return SharedJson.decodeBean(SharedNet.get(
                "personal_fm",
                params = listOf(
                    "timestamp" to timestamp
                )
            )
        )
    }

    suspend fun getCatlist(): CatlistData {
        return SharedJson.decodeBean(SharedNet.get("playlist/catlist"))
    }

    suspend fun getCommentMusic(
        id: Long,
        limit: Int = 30,
        offset: Int = 0,
    ): CommentData {
        return SharedJson.decodeBean(SharedNet.get(
                "comment/music",
                params = listOf(
                    "id" to id,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    suspend fun getCommentDj(
        id: Long,
        limit: Int = 30,
        offset: Int = 0,
    ): CommentData {
        return SharedJson.decodeBean(SharedNet.get(
                "comment/dj",
                params = listOf(
                    "id" to id,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    suspend fun getCommentMv(
        id: Long,
        limit: Int = 30,
        offset: Int = 0,
    ): CommentData {
        return SharedJson.decodeBean(SharedNet.get(
                "comment/mv",
                params = listOf(
                    "id" to id,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        )
    }

    suspend fun likeComment(
        songId: String,
        commentId: Long,
        t: Int,
        type: Int = 0,
    ): CommentOpData {
        return SharedJson.decodeBean(SharedNet.post(
                "comment/like",
                params = listOf(
                    "id" to songId,
                    "cid" to commentId,
                    "t" to t,
                    "type" to type
                )
            )
        )
    }

    suspend fun addComment(
        songId: String,
        type: Int = 0,
        t: Int = 1,
        content: String,
    ): CommentOpData {
        return SharedJson.decodeBean(SharedNet.post(
                "comment",
                params = listOf(
                    "id" to songId,
                    "type" to type,
                    "t" to t,
                    "content" to content
                )
            )
        )
    }

    suspend fun getHotArtistList(limit: Int = 10): ArtistListData {
        return SharedJson.decodeBean(SharedNet.get(
                "artist/list",
                params = listOf(
                    "type" to -1,
                    "limit" to limit
                )
            )
        )
    }

    suspend fun getHighQualityPlaylistList(limit: Int = 10): PlaylistListData {
        return SharedJson.decodeBean(SharedNet.get(
                "top/playlist/highquality",
                params = listOf(
                    "limit" to limit
                )
            )
        )
    }

    suspend fun getDjRecommendList(limit: Int = 6): DjListData {
        return SharedJson.decodeBean(SharedNet.get(
                "dj/recommend",
                params = listOf(
                    "limit" to limit
                )
            )
        )
    }

    private const val SONG_LIST_LIMIT = 800

    suspend fun getFullPlaylistSongList(id: Long, timestamp: Long? = null): SongListData {
        var offset = 0
        val list = mutableListOf<SongData>()
        while (true) {
            val songList = getPlaylistSongList(
                id,
                limit = SONG_LIST_LIMIT,
                offset = offset,
                timestamp = timestamp
            )
            if (songList.code != 200) {
                throw Exception("code = ${songList.code}")
            }
            if (songList.songs.isEmpty()) {
                break
            }
            list.addAll(songList.songs)
            offset = list.size
        }
        return SongListData(200, list)
    }
}

package me.wcy.music.shared.net

import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.shared.bean.home.LyricNewData
import me.wcy.music.shared.bean.home.PlayModeIntelligenceData
import me.wcy.music.shared.bean.home.SongDetailListData
import me.wcy.music.shared.bean.home.SongDownloadUrlData
import me.wcy.music.shared.bean.home.SongLikeCheckData

/**
 * 播放/歌曲增强接口：新歌词、批量歌曲详情、下载地址、喜爱检查、心动模式。
 */
object PlayExtraNet {

    /**
     * 新歌词接口。lrc/tlyric/romalrc/klyric 与旧 lyric 接口一致；
     * 带逐字歌词的歌曲额外返回 yrc（逐字）、ytlrc（逐字翻译）、yromalrc（逐字音译），
     * yrc.lyric 为内嵌 JSON 字符串（{"t":0,"c":[{"tx":"..."}]}）。
     */
    suspend fun getNewLrc(id: Long, cv: Int = 0): LyricNewData {
        return SharedJson.decodeBean(SharedNet.get(
                "lyric/new",
                params = listOf(
                    "id" to id,
                    "cv" to cv
                )
            )
        )
    }

    /**
     * 批量歌曲详情，ids 用逗号拼接（如 347230,347231）。
     */
    suspend fun getSongDetail(ids: List<Long>): SongDetailListData {
        return SharedJson.decodeBean(SharedNet.get(
                "song/detail",
                params = listOf(
                    "ids" to ids.joinToString(",")
                )
            )
        )
    }

    /**
     * 下载地址（需登录 cookie），level 同 song/url/v1（standard/exhigh/lossless/hires/jymaster）。
     * 无版权/无权限时 data.code 非 200 且 url 为 null。
     */
    suspend fun getSongDownloadUrl(id: Long, level: String = "standard"): SongDownloadUrlData {
        return SharedJson.decodeBean(SharedNet.get(
                "song/download/url/v1",
                params = listOf(
                    "id" to id,
                    "level" to level
                )
            )
        )
    }

    /**
     * 歌曲能否听/喜爱检查（需登录 cookie）。实测 ids 必须以重复参数传递
     * （ids=a&ids=b），传单个值上游返回 400；返回 ids 为检查结果列表。
     */
    suspend fun checkSongLike(ids: List<Long>): SongLikeCheckData {
        return SharedJson.decodeBean(SharedNet.get(
                "song/like/check",
                params = ids.map { "ids" to it }
            )
        )
    }

    /**
     * 心动模式推荐队列（需登录 + 歌单 pid）。实测模块参数名为 id（任务描述中的 songid
     * 对应此参数），sid 为队列起始歌曲；当前测试账号所有歌单均返回 code 400
     * 「不支持该歌单类型」，调用方需容忍空 data。
     */
    suspend fun getIntelligenceList(
        id: Long,
        pid: Long? = null,
        sid: Long? = null,
    ): PlayModeIntelligenceData {
        return SharedJson.decodeBean(SharedNet.get(
                "playmode/intelligence/list",
                params = listOf(
                    "id" to id,
                    "pid" to pid,
                    "sid" to sid
                )
            )
        )
    }
}

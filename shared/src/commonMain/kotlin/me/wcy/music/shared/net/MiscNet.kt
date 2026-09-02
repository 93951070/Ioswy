package me.wcy.music.shared.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.common.bean.SongData

@Serializable
data class FmTrashData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("songs")
    val songs: List<SongData> = emptyList(),
)

@Serializable
data class ScrobbleData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: String? = null,
)

@Serializable
data class CalendarData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CalendarInner? = null,
)

@Serializable
data class CalendarInner(
    @SerialName("calendarEvents")
    val calendarEvents: List<CalendarEvent> = emptyList(),
)

@Serializable
data class CalendarEvent(
    @SerialName("id")
    val id: String = "",
    @SerialName("eventType")
    val eventType: String = "",
    @SerialName("tag")
    val tag: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("imgUrl")
    val imgUrl: String = "",
    @SerialName("targetUrl")
    val targetUrl: String = "",
    @SerialName("remindText")
    val remindText: String = "",
    @SerialName("canRemind")
    val canRemind: Boolean = false,
    @SerialName("reminded")
    val reminded: Boolean = false,
    @SerialName("onlineTime")
    val onlineTime: Long = 0,
    @SerialName("offlineTime")
    val offlineTime: Long = 0,
    @SerialName("resourceType")
    val resourceType: String = "",
    @SerialName("resourceId")
    val resourceId: String = "",
    @SerialName("eventStatus")
    val eventStatus: String = "",
    @SerialName("alg")
    val alg: String = "",
)

/**
 * 杂项接口：私人FM垃圾桶、听歌打卡、音乐日历。
 */
object MiscNet {

    /**
     * 私人FM 不喜欢（把歌曲丢进垃圾桶，FM 不再推荐）。
     */
    suspend fun fmTrash(id: Long): FmTrashData {
        return SharedJson.decodeBean(SharedNet.post(
                "fm_trash",
                params = listOf("id" to id)
            )
        )
    }

    /**
     * 听歌打卡上报。time 为播放时长（秒），code 200 即成功。
     */
    suspend fun scrobble(
        id: Long,
        sourceid: Long,
        time: Long,
    ): ScrobbleData {
        return SharedJson.decodeBean(SharedNet.get(
                "scrobble",
                params = listOf(
                    "id" to id,
                    "sourceid" to sourceid,
                    "time" to time
                )
            )
        )
    }

    /**
     * 音乐日历。startTime/endTime 为毫秒时间戳（一般取今天 0 点到 23:59:59）。
     */
    suspend fun getCalendar(
        startTime: Long,
        endTime: Long,
    ): CalendarData {
        return SharedJson.decodeBean(SharedNet.get(
                "calendar",
                params = listOf(
                    "startTime" to startTime,
                    "endTime" to endTime
                )
            )
        )
    }
}

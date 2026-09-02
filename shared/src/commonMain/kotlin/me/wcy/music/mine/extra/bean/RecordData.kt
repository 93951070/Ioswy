package me.wcy.music.mine.extra.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.SongData

/**
 * user/record 单条听歌记录：song 为完整歌曲对象，直接复用 SongData。
 * type=0 时 weekData 有值，type=1 时 allData 有值，另一侧为空数组。
 */
@Serializable
data class RecordItem(
    @SerialName("playCount")
    val playCount: Long = 0,
    @SerialName("score")
    val score: Int = 0,
    @SerialName("song")
    val song: SongData = SongData()
)

@Serializable
data class RecordData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("weekData")
    val weekData: List<RecordItem> = listOf(),
    @SerialName("allData")
    val allData: List<RecordItem> = listOf()
)

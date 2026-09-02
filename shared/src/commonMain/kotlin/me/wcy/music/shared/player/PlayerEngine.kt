package me.wcy.music.shared.player

import kotlinx.coroutines.flow.StateFlow
import me.wcy.music.common.bean.SongData

/**
 * 播放引擎抽象：shared UI 层消费的播放能力最小面。
 * Android 壳由 PlayerController(Media3) 桥接实现，iOS 壳由 AVPlayer 实现。
 */
interface PlayerEngine {
    val playlist: StateFlow<List<SongData>>
    val currentSong: StateFlow<SongData?>
    val isPlaying: StateFlow<Boolean>
    val playProgress: StateFlow<Long>
    val bufferingPercent: StateFlow<Int>

    /** 0 Loop / 1 Shuffle / 2 Single，UI 层只做循环切换与图标展示 */
    val playMode: StateFlow<Int>

    fun playPause()
    fun next()
    fun prev()

    /** 播放队列中第 [index] 首 */
    fun playAt(index: Int)

    /** 从队列移除第 [index] 首 */
    fun delete(index: Int)
    fun clearPlaylist()
    fun seekTo(progressMs: Int)
    fun setPlayMode(mode: Int)

    /** 「下一首播放」：插入到当前歌曲之后 */
    fun playNext(song: SongData)

    /** 按当前音质设置重取 url 重新播当前歌（音质切换后调用） */
    fun replayCurrent()
}

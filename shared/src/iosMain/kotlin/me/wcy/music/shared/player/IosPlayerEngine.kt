package me.wcy.music.shared.player

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.SongData
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.net.apiCall
import platform.AVFoundation.AVAudioSession
import platform.AVFoundation.AVAudioSessionCategoryPlayback
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatus
import platform.AVFoundation.AVPlayerTimeControlStatus
import platform.AVFoundation.AVQueuePlayer
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.darwin.NSObjectProtocol
import platform.darwin.dispatch_get_main_queue

/**
 * iOS 播放引擎：AVQueuePlayer + 手动队列管理（队列真值存 Kotlin 侧）。
 *
 * ponytail: 音质锁 exhigh——AVPlayer 播 HTTP FLAC(lossless+) 兼容性存疑，取不到 url 时降级 standard 重试一次；
 * 失败/重连的精细缓冲态未做，buffering 用「100=起播中，0=就绪或失败」两态近似。
 */
@OptIn(ExperimentalForeignApi::class)
class IosPlayerEngine : PlayerEngine {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val player = AVQueuePlayer()

    private val _playlist = MutableStateFlow<List<SongData>>(emptyList())
    override val playlist: StateFlow<List<SongData>> = _playlist.asStateFlow()

    private val _currentSong = MutableStateFlow<SongData?>(null)
    override val currentSong: StateFlow<SongData?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playProgress = MutableStateFlow(0L)
    override val playProgress: StateFlow<Long> = _playProgress.asStateFlow()

    private val _bufferingPercent = MutableStateFlow(0)
    override val bufferingPercent: StateFlow<Int> = _bufferingPercent.asStateFlow()

    private val _playMode = MutableStateFlow(PLAY_MODE_LOOP)
    override val playMode: StateFlow<Int> = _playMode.asStateFlow()

    private var currentIndex = -1

    /** 本地歌曲 id -> 文件路径，命中则直接播文件；在线歌曲走网络取流地址 */
    private val localPaths = mutableMapOf<Long, String>()

    private var endObserver: NSObjectProtocol? = null

    init {
        // 播放类别：静音开关下仍出声；后台播放还需 Info.plist UIBackgroundModes audio（iosApp 已配置）
        runCatching {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, null)
            session.setActive(true, null)
        }

        // 0.5s 周期回调：同步进度、播放态、缓冲态（暂停时也回调，顺带兜底同步 isPlaying）
        player.addPeriodicTimeObserverForInterval(
            CMTimeMake(1, 2), // 0.5s
            dispatch_get_main_queue()
        ) { _ ->
            player.currentTime().useContents {
                _playProgress.value = if (timescale != 0) value * 1000 / timescale else 0
            }
            _isPlaying.value = player.timeControlStatus == AVPlayerTimeControlStatus.Playing
            _bufferingPercent.value = when (player.currentItem?.status) {
                AVPlayerItemStatus.ReadyToPlay, AVPlayerItemStatus.Failed, null -> 0
                else -> 100
            }
        }

        // 单首播完 -> 依 playMode 自动续播；object 传 null 观察所有 item（替换 item 无需重新注册）
        endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _ -> onItemEnded() }
    }

    override fun playPause() {
        if (_playlist.value.isEmpty()) return
        if (player.timeControlStatus == AVPlayerTimeControlStatus.Playing) {
            player.pause()
        } else if (player.currentItem == null) {
            startCurrent()
        } else {
            player.play()
        }
    }

    override fun next() = advance(1)

    override fun prev() = advance(-1)

    override fun playAt(index: Int) {
        if (index !in _playlist.value.indices) return
        currentIndex = index
        startCurrent()
    }

    override fun delete(index: Int) {
        val list = _playlist.value.toMutableList()
        if (index !in list.indices) return
        localPaths.remove(_playlist.value[index].id)
        val removingCurrent = index == currentIndex
        list.removeAt(index)
        _playlist.value = list
        when {
            list.isEmpty() -> stopAndClear()
            removingCurrent -> {
                currentIndex = index.coerceAtMost(list.size - 1)
                startCurrent()
            }
            index < currentIndex -> currentIndex--
        }
    }

    override fun clearPlaylist() = stopAndClear()

    override fun seekTo(progressMs: Int) {
        _playProgress.value = progressMs.toLong()
        player.seekToTime(CMTimeMakeWithSeconds(progressMs / 1000.0, 600))
    }

    override fun setPlayMode(mode: Int) {
        _playMode.value = mode
    }

    override fun playNext(song: SongData) {
        val list = _playlist.value.toMutableList()
        list.add((currentIndex + 1).coerceAtMost(list.size), song)
        _playlist.value = list
    }

    /** 换队列并播放第 [index] 首（iOS 组合根入口） */
    fun playSongList(songs: List<SongData>, index: Int) {
        if (songs.isEmpty()) return
        _playlist.value = songs
        currentIndex = index.coerceIn(songs.indices)
        startCurrent()
    }

    /** 追加到队尾（私人FM 连播用） */
    fun appendSongs(songs: List<SongData>) {
        if (songs.isEmpty()) return
        _playlist.value = _playlist.value + songs
    }

    /** 播放本地文件（本地音乐 tab 扫描沙盒得到），按 [songs] 与 [paths] 一一对应注册 */
    fun playLocalSongs(songs: List<SongData>, paths: List<String>, index: Int) {
        songs.forEachIndexed { i, song -> paths.getOrNull(i)?.let { localPaths[song.id] = it } }
        playSongList(songs, index)
    }

    private fun advance(delta: Int) {
        val list = _playlist.value
        if (list.isEmpty()) return
        currentIndex = when {
            _playMode.value == PLAY_MODE_SHUFFLE && list.size > 1 -> {
                var next = currentIndex
                while (next == currentIndex) next = list.indices.random()
                next
            }
            else -> (currentIndex + delta).mod(list.size)
        }
        startCurrent()
    }

    private fun onItemEnded() {
        when (_playMode.value) {
            PLAY_MODE_SINGLE -> {
                player.seekToTime(CMTimeMakeWithSeconds(0.0, 600))
                player.play()
            }
            else -> advance(1)
        }
    }

    private fun startCurrent() {
        val song = _playlist.value.getOrNull(currentIndex) ?: return stopAndClear()
        _currentSong.value = song
        _playProgress.value = 0
        _bufferingPercent.value = 100
        scope.launch {
            val path = resolveUri(song)
            // 异步取 url 期间用户可能已切歌，过期结果直接丢弃
            if (_currentSong.value !== song) return@launch
            if (path.isNullOrEmpty()) {
                _bufferingPercent.value = 0
                return@launch
            }
            val nsUrl = if (path.startsWith("/")) {
                NSURL.fileURLWithPath(path)
            } else {
                NSURL.URLWithString(path)
            } ?: return@launch
            player.replaceCurrentItem(AVPlayerItem.itemWithURL(nsUrl))
            player.play()
        }
    }

    private suspend fun resolveUri(song: SongData): String? {
        localPaths[song.id]?.let { return it }
        if (song.id <= 0) return null
        val primary = apiCall { DiscoverNet.getSongUrl(song.id, PLAY_LEVEL) }
        val primaryUrl = primary.takeIf { it.isSuccessWithData() }?.data?.firstOrNull()?.url
        if (!primaryUrl.isNullOrEmpty()) return primaryUrl
        // 部分歌曲高音质档返回空 url，降级 standard 再试一次
        val fallback = apiCall { DiscoverNet.getSongUrl(song.id, LEVEL_STANDARD) }
        return fallback.takeIf { it.isSuccessWithData() }?.data?.firstOrNull()?.url
            ?.takeUnless { it.isEmpty() }
    }

    private fun stopAndClear() {
        player.pause()
        player.replaceCurrentItem(null)
        currentIndex = -1
        _playlist.value = emptyList()
        _currentSong.value = null
        _isPlaying.value = false
        _playProgress.value = 0
        _bufferingPercent.value = 0
    }

    companion object {
        const val PLAY_LEVEL = "exhigh"
        const val LEVEL_STANDARD = "standard"
        const val PLAY_MODE_LOOP = 0
        const val PLAY_MODE_SHUFFLE = 1
        const val PLAY_MODE_SINGLE = 2
    }
}

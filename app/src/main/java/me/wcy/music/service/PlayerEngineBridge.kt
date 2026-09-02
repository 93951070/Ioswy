package me.wcy.music.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.wcy.music.common.bean.SongData
import me.wcy.music.service.PlayMode
import me.wcy.music.service.PlayState
import me.wcy.music.service.PlayerController
import me.wcy.music.shared.player.PlayerEngine
import me.wcy.music.utils.toMediaItem
import me.wcy.music.utils.toSongData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PlayerEngine 的 Android 实现：桥接 Media3 的 PlayerController，
 * 对 shared UI 暴露领域对象 SongData。
 */
@Singleton
class PlayerEngineBridge @Inject constructor(
    private val pc: PlayerController
) : PlayerEngine {

    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    override val playlist: StateFlow<List<SongData>> =
        pc.playlist.map { list -> list.map { it.toSongData() } }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val currentSong: StateFlow<SongData?> =
        pc.currentSong.map { it?.toSongData() }
            .stateIn(scope, SharingStarted.Eagerly, null)

    override val isPlaying: StateFlow<Boolean> =
        pc.playState.map { it == PlayState.Playing }
            .stateIn(scope, SharingStarted.Eagerly, false)

    override val playProgress: StateFlow<Long> =
        pc.playProgress.stateIn(scope, SharingStarted.Eagerly, 0L)

    override val bufferingPercent: StateFlow<Int> =
        pc.bufferingPercent.stateIn(scope, SharingStarted.Eagerly, 0)

    override val playMode: StateFlow<Int> =
        pc.playMode.map { it.value }
            .stateIn(scope, SharingStarted.Eagerly, PlayMode.Loop.value)

    override fun playPause() = pc.playPause()
    override fun next() = pc.next()
    override fun prev() = pc.prev()

    override fun playAt(index: Int) {
        val item = pc.playlist.value.getOrNull(index) ?: return
        pc.play(item.mediaId)
    }

    override fun delete(index: Int) {
        val item = pc.playlist.value.getOrNull(index) ?: return
        pc.delete(item)
    }

    override fun clearPlaylist() = pc.clearPlaylist()

    override fun seekTo(progressMs: Int) = pc.seekTo(progressMs)

    override fun setPlayMode(mode: Int) = pc.setPlayMode(PlayMode.valueOf(mode))

    override fun playNext(song: SongData) {
        val controller = pc.mediaController
        controller.addMediaItem(controller.currentMediaItemIndex + 1, song.toMediaItem())
    }

    override fun replayCurrent() {
        val controller = pc.mediaController
        val item = controller.currentMediaItem ?: return
        val position = controller.currentPosition
        // 替换当前 MediaItem 触发重新解析 url（DataSource 按最新音质设置取流）
        controller.replaceMediaItem(controller.currentMediaItemIndex, item)
        controller.seekTo(position)
        controller.prepare()
    }
}

package me.wcy.music.main.playing

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.wcy.music.R
import me.wcy.music.common.BaseMusicActivity
import me.wcy.music.common.bean.SongData
import me.wcy.music.common.dialog.songmenu.SongMoreMenuDialog
import me.wcy.music.common.dialog.songmenu.SimpleMenuItem
import me.wcy.music.common.dialog.songmenu.items.CollectMenuItem
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.PlayingScreen
import me.wcy.music.consts.RoutePath
import me.wcy.router.CRouter
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.discover.comment.MyCommentStore
import me.wcy.music.discover.comment.viewmodel.CommentViewModel
import me.wcy.music.service.PlayerController
import me.wcy.music.service.likesong.LikeSongProcessor
import me.wcy.music.shared.player.PlayerEngine
import me.wcy.music.storage.LrcCache
import me.wcy.music.storage.preference.ConfigPreferences
import me.wcy.music.utils.getSongId
import me.wcy.music.utils.isLocal
import me.wcy.music.utils.toMediaItem
import me.wcy.music.utils.toSongEntity
import me.wcy.router.annotation.Route
import top.wangchenyan.common.ext.showBottomItemsDialog
import top.wangchenyan.common.ext.toast
import java.io.File
import javax.inject.Inject

/**
 * Created by wangchenyan.top on 2023/9/4.
 */
@Route(RoutePath.PLAYING)
@AndroidEntryPoint
class PlayingActivity : BaseMusicActivity() {

    @Inject
    lateinit var playerController: PlayerController

    @Inject
    lateinit var playerEngine: PlayerEngine

    @Inject
    lateinit var likeSongProcessor: LikeSongProcessor

    @Inject
    lateinit var myCommentStore: MyCommentStore

    private val commentViewModel by viewModels<CommentViewModel> {
        viewModelFactory {
            initializer { CommentViewModel(myCommentStore) }
        }
    }

    private val audioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val lrcContent = MutableStateFlow("")
    private val lrcLabel = MutableStateFlow("歌词加载中…")
    private var loadLrcJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            playerController.currentSong.collectLatest { song ->
                if (song != null) {
                    loadLrc(song)
                }
            }
        }

        setContent {
            MusicTheme(darkTheme = false) {
                val content by lrcContent.collectAsState()
                val label by lrcLabel.collectAsState()
                PlayingScreen(
                    playerEngine = playerEngine,
                    commentViewModel = commentViewModel,
                    onClose = { onBackPressed() },
                    isLiked = { songId -> likeSongProcessor.isLiked(songId) },
                    onToggleLike = { songId ->
                        lifecycleScope.launch {
                            val res = likeSongProcessor.like(this@PlayingActivity, songId)
                            if (res.isSuccess()) {
                                // UI 通过 collect 更新
                            } else {
                                toast(res.msg)
                            }
                        }
                    },
                    onShare = { song, songId -> shareSong(song, songId) },
                    onOpenMenu = { song, songId -> showSongMenu(song, songId) },
                    onDownload = { toast("敬请期待") },
                    onMessage = { toast(it) },
                    onOpenFloor = { pid ->
                        val songId = playerEngine.currentSong.value?.id
                        if (songId == null || songId <= 0) return@PlayingScreen
                        CRouter.with(this@PlayingActivity)
                            .url(RoutePath.COMMENT_FLOOR)
                            .extra("resourceId", songId)
                            .extra("resourceType", 0)
                            .extra("parentCommentId", pid)
                            .start()
                    },
                    onPlaylistEmpty = { onBackPressed() },
                    soundQuality = ConfigPreferences.playSoundQuality,
                    onSelectQuality = { level ->
                        if (level != ConfigPreferences.playSoundQuality) {
                            ConfigPreferences.playSoundQuality = level
                            playerEngine.replayCurrent()
                            toast("音质已切换")
                        }
                    },
                    lrcContent = content,
                    onUpdateLrc = {},
                    lrcLabel = label
                )
            }
        }

        configWindowInsets {
            fillNavBar = false
            fillDisplayCutout = false
            statusBarTextDarkStyle = false
            navBarButtonDarkStyle = false
        }
    }

    private fun showSongMenu(song: SongData, songId: Long) {
        val entries = resources.getStringArray(R.array.sound_quality_entries).toList()
        val values = resources.getStringArray(R.array.sound_quality_entry_values)
        val checked = values.indexOf(ConfigPreferences.playSoundQuality).coerceAtLeast(0)
        // 播放页只展示当前歌曲，优先取原 MediaItem 转 Entity，保留本地歌曲信息
        val entity = playerController.currentSong.value?.toSongEntity()
            ?: song.toMediaItem().toSongEntity()
        SongMoreMenuDialog(this, entity)
            .setItems(
                listOf(
                    CollectMenuItem(lifecycleScope, SongData(id = songId)),
                    SimpleMenuItem("下一首播放") {
                        playerEngine.playNext(song)
                        toast("已添加到下一首播放")
                    },
                    SimpleMenuItem("分享") { shareSong(song, songId) },
                    SimpleMenuItem("音质：${entries[checked]}") {
                        showBottomItemsDialog(entries, checked) { _, which ->
                            val level = values.getOrNull(which)
                            if (level == null || level == ConfigPreferences.playSoundQuality) {
                                return@showBottomItemsDialog
                            }
                            ConfigPreferences.playSoundQuality = level
                            val controller = playerController.mediaController
                            val position = controller.currentPosition
                            controller.replaceMediaItem(
                                controller.currentMediaItemIndex,
                                song.toMediaItem()
                            )
                            controller.seekTo(position)
                            toast("音质已切换：${entries[which]}")
                        }
                    }
                )
            )
            .show()
    }

    private fun shareSong(song: SongData, songId: Long) {
        val text = buildString {
            append("分享歌曲：")
            append(song.name)
            append(" - ")
            append(song.ar.joinToString("/") { it.name })
            if (songId > 0) {
                append("\nhttps://music.163.com/song?id=")
                append(songId)
            }
        }
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                },
                "分享"
            )
        )
    }

    private fun loadLrc(song: androidx.media3.common.MediaItem) {
        loadLrcJob?.cancel()
        loadLrcJob = null
        val lrcPath = LrcCache.getLrcFilePath(song)
        if (lrcPath?.isNotEmpty() == true) {
            val file = File(lrcPath)
            if (file.exists()) {
                lrcContent.value = file.readText()
                return
            }
        }
        lrcContent.value = ""
        if (song.isLocal()) {
            lrcLabel.value = "暂无歌词"
        } else {
            lrcLabel.value = "歌词加载中…"
            loadLrcJob = lifecycleScope.launch {
                kotlin.runCatching {
                    val lrcWrap = DiscoverNet.getLrc(song.getSongId())
                    if (lrcWrap.code == 200 && lrcWrap.lrc.isValid()) {
                        lrcWrap.lrc
                    } else {
                        throw IllegalStateException("lrc is invalid")
                    }
                }.onSuccess {
                    val file = LrcCache.saveLrcFile(song, it.lyric)
                    lrcContent.value = file.readText()
                }.onFailure {
                    lrcLabel.value = "歌词加载失败"
                }
            }
        }
    }
}

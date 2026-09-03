package me.wcy.music.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.runtime.snapshots.SnapshotStateObserver
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import android.os.Handler
import android.os.Looper
import me.wcy.music.R
import me.wcy.music.compose.component.desktopLyricsOn
import me.wcy.music.net.datasource.MusicDataSource
import me.wcy.music.service.PlayServiceModule.playerController
import me.wcy.music.utils.MusicUtils
import top.wangchenyan.common.CommonApp

/**
 * Created by wangchenyan.top on 2024/3/26.
 */
class MusicService : MediaSessionService() {
    private lateinit var player: Player
    private lateinit var session: MediaSession
    private var lyricWindow: LyricFloatWindow? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val snapshotObserver = androidx.compose.runtime.snapshots.SnapshotStateObserver { command ->
        mainHandler.post { command() }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(applicationContext)
            // 自动处理音频焦点
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            // 自动暂停播放
            .setHandleAudioBecomingNoisy(true)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(applicationContext)
                    .setDataSourceFactory(MusicDataSource.Factory(applicationContext))
            )
            .build()

        session = MediaSession.Builder(this, player)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    MusicUtils.getStartPlayingPageIntent(this),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(applicationContext).build().apply {
                setSmallIcon(R.drawable.ic_notification)
            }
        )

        snapshotObserver.observeReads(
            scope = "lyric",
            onValueChangedForScope = { windowReady() },
            block = { desktopLyricsOn.value }
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return session
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotObserver.stop()
        snapshotObserver.clear()
        lyricWindow?.destroy()
        lyricWindow = null
        player.release()
        session.release()
    }

    private fun windowReady() {
        syncLyricWindow()
        // SnapshotStateObserver 回调后需重新 register，否则只观察第一次读取（不改会被取消注册）
        snapshotObserver.observeReads(
            scope = "lyric",
            onValueChangedForScope = { windowReady() },
            block = { desktopLyricsOn.value }
        )
    }

    private fun syncLyricWindow() {
        if (desktopLyricsOn.value) {
            if (lyricWindow == null) {
                val controller = runCatching { controller() }.getOrNull() ?: return
                lyricWindow = LyricFloatWindow(applicationContext, controller)
                lyricWindow?.show()
            }
        } else {
            lyricWindow?.destroy()
            lyricWindow = null
        }
    }

    private fun controller(): PlayerController {
        return application.playerController()
    }

    companion object {
        val EXTRA_NOTIFICATION = "${CommonApp.app.packageName}.notification"
    }
}
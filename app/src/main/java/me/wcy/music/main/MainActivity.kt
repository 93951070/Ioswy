package me.wcy.music.main

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.wcy.music.R
import me.wcy.music.account.service.UserService
import me.wcy.music.common.ApiDomainDialog
import me.wcy.music.common.BaseMusicActivity
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.DrawerContent
import me.wcy.music.compose.ui.MainScreen
import me.wcy.music.consts.RoutePath
import me.wcy.music.discover.banner.BannerData
import me.wcy.music.discover.home.viewmodel.DiscoverCacheStore
import me.wcy.music.discover.home.viewmodel.DiscoverViewModel
import me.wcy.music.mine.home.viewmodel.MineViewModel
import me.wcy.music.net.NetCache
import me.wcy.music.storage.preference.ConfigPreferences
import me.wcy.music.service.MusicService
import me.wcy.music.service.PlayServiceModule
import me.wcy.music.service.PlayServiceModule.playerController
import me.wcy.music.shared.player.PlayerEngine
import me.wcy.music.utils.QuitTimer
import me.wcy.music.utils.TimeUtils
import me.wcy.router.CRouter
import top.wangchenyan.common.ext.getColorEx
import top.wangchenyan.common.ext.showConfirmDialog
import top.wangchenyan.common.ext.toast
import javax.inject.Inject

/**
 * Created by wangchenyan.top on 2023/8/21.
 */
@AndroidEntryPoint
class MainActivity : BaseMusicActivity() {
    private val quitTimer by lazy {
        QuitTimer(onTimerListener)
    }
    private var apiDomainDialog: ApiDomainDialog? = null

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var playerEngine: PlayerEngine

    private val discoverCacheStore = object : DiscoverCacheStore {
        override suspend fun getBanners(): List<BannerData>? {
            return NetCache.globalCache.getJsonArray(
                DiscoverViewModel.CACHE_KEY_BANNER,
                BannerData::class.java
            )
        }

        override suspend fun putBanners(value: List<BannerData>) {
            NetCache.globalCache.putJson(DiscoverViewModel.CACHE_KEY_BANNER, value)
        }

        override suspend fun getRecommendPlaylists(): List<PlaylistData>? {
            if (!userService.isLogin()) return null
            return NetCache.userCache.getJsonArray(
                DiscoverViewModel.CACHE_KEY_REC_PLAYLIST,
                PlaylistData::class.java
            )
        }

        override suspend fun putRecommendPlaylists(value: List<PlaylistData>) {
            if (!userService.isLogin()) return
            NetCache.userCache.putJson(DiscoverViewModel.CACHE_KEY_REC_PLAYLIST, value)
        }

        override suspend fun getRankingList(): List<PlaylistData>? {
            return NetCache.globalCache.getJsonArray(
                DiscoverViewModel.CACHE_KEY_RANKING_LIST,
                PlaylistData::class.java
            )
        }

        override suspend fun putRankingList(value: List<PlaylistData>) {
            NetCache.globalCache.putJson(DiscoverViewModel.CACHE_KEY_RANKING_LIST, value)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicTheme {
                val playerReady by PlayServiceModule.isPlayerReady.observeAsState(false)
                if (playerReady) {
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            DrawerContent(
                                userService = userService,
                                onMenuSelect = { itemId ->
                                    onMenuSelect(itemId, drawerState, scope)
                                }
                            )
                        }
                    ) {
                        MainScreen(
                            drawerState = drawerState,
                            discoverViewModel = viewModel {
                                DiscoverViewModel(
                                    profileFlow = userService.profile,
                                    hasApiDomain = { ConfigPreferences.apiDomain.isNotEmpty() },
                                    cache = discoverCacheStore
                                )
                            },
                            mineViewModel = viewModel {
                                MineViewModel(
                                    profileFlow = userService.profile,
                                    readPlaylistCache = {
                                        NetCache.userCache.getJsonArray(
                                            PLAYLIST_CACHE_KEY,
                                            PlaylistData::class.java
                                        )
                                    },
                                    writePlaylistCache = { list ->
                                        NetCache.userCache.putJson(PLAYLIST_CACHE_KEY, list)
                                    }
                                )
                            },
                            playerController = application.playerController(),
                            playerEngine = playerEngine,
                            userService = userService,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onMenuSelect = { itemId ->
                                onMenuSelect(itemId, drawerState, scope)
                            },
                            onOpenPlaylist = { onOpenPlaylist() },
                            onOpenPlaying = {
                                CRouter.with(this@MainActivity).url(RoutePath.PLAYING).start()
                            },
                            onOpenPlaylistDetail = { id ->
                                CRouter.with(this@MainActivity)
                                    .url(RoutePath.PLAYLIST_DETAIL)
                                    .extra("id", id)
                                    .start()
                            },
                            onOpenRanking = {
                                CRouter.with(this@MainActivity).url(RoutePath.RANKING).start()
                            },
                            onOpenPlaylistSquare = {
                                CRouter.with(this@MainActivity).url(RoutePath.PLAYLIST_SQUARE).start()
                            },
                            onOpenRecommendSong = {
                                CRouter.with(this@MainActivity).url(RoutePath.RECOMMEND_SONG).start()
                            },
                            onOpenPersonalFm = {
                                CRouter.with(this@MainActivity).url(RoutePath.PERSONAL_FM).start()
                            },
                            onOpenArtistList = {
                                CRouter.with(this@MainActivity).url(RoutePath.ARTIST_LIST).start()
                            },
                            onOpenNewSong = {
                                CRouter.with(this@MainActivity).url(RoutePath.ALBUM_NEW).start()
                            },
                            onOpenDj = {
                                CRouter.with(this@MainActivity).url(RoutePath.DJ_RECOMMEND).start()
                            },
                            onOpenMvList = {
                                CRouter.with(this@MainActivity).url(RoutePath.MV_LIST).start()
                            },
                            onOpenRecentPlay = {
                                CRouter.with(this@MainActivity)
                                    .url(RoutePath.RECENT_PLAY)
                                    .extra("uid", userService.getUserId())
                                    .start()
                            },
                            onOpenSubList = {
                                CRouter.with(this@MainActivity).url(RoutePath.SUB_LIST).start()
                            },
                            onOpenCloudDisk = {
                                CRouter.with(this@MainActivity).url(RoutePath.CLOUD_DISK).start()
                            },
                            onOpenMsgCenter = {
                                CRouter.with(this@MainActivity).url(RoutePath.MSG_CENTER).start()
                            },
                            onOpenLocalMusic = {
                                CRouter.with(this@MainActivity).url(RoutePath.LOCAL_SONG).start()
                            },
                            onOpenSearch = {
                                CRouter.with(this@MainActivity).url(RoutePath.SEARCH).start()
                            },
                            onOpenLogin = {
                                CRouter.with(this@MainActivity).url(RoutePath.LOGIN).start()
                            }
                        )
                    }
                }
            }
        }

        parseIntent()

        configWindowInsets {
            navBarColor = getColorEx(R.color.tab_bg)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseIntent()
    }

    private fun parseIntent() {
        val intent = intent
        if (intent.hasExtra(MusicService.EXTRA_NOTIFICATION) && PlayServiceModule.isPlayerReady.value == true) {
            if (application.playerController().currentSong.value != null) {
                CRouter.with(this).url(RoutePath.PLAYING).start()
            }
            setIntent(Intent())
        }
    }

    fun openDrawer() {
        // 兼容旧的 View Fragment 调用；Compose 侧通过 drawerState 打开
    }

    private fun onOpenPlaylist() {
        CRouter.with(this).url(RoutePath.PLAYING).start()
    }

    private fun onMenuSelect(
        itemId: Int,
        drawerState: androidx.compose.material3.DrawerState,
        scope: kotlinx.coroutines.CoroutineScope
    ) {
        scope.launch {
            delay(200)
            drawerState.close()
        }
        when (itemId) {
            R.id.action_domain_setting -> {
                if (apiDomainDialog == null) {
                    apiDomainDialog = ApiDomainDialog(this)
                }
                apiDomainDialog?.show()
            }
            R.id.action_setting -> {
                CRouter.with(this).url("/settings").start()
            }
            R.id.action_timer -> {
                timerDialog()
            }
            R.id.action_desktop_lyrics -> {
                toggleDesktopLyrics()
            }
            R.id.action_logout -> {
                logout()
            }
            R.id.action_exit -> {
                exitApp()
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
        }
    }

    private val onTimerListener = object : QuitTimer.OnTimerListener {
        override fun onTick(remain: Long) {
            val title = getString(R.string.menu_timer)
            if (remain == 0L) {
                toast(title)
            } else {
                toast(TimeUtils.formatTime("$title(mm:ss)", remain))
            }
        }

        override fun onTimeEnd() {
            exitApp()
        }
    }

    private fun timerDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle(R.string.menu_timer)
            .setItems(resources.getStringArray(R.array.timer_text)) { dialog: DialogInterface?, which: Int ->
                val times = resources.getIntArray(R.array.timer_int)
                startTimer(times[which])
            }
            .show()
    }

    private fun startTimer(minute: Int) {
        quitTimer.start((minute * 60 * 1000).toLong())
        if (minute > 0) {
            toast(getString(R.string.timer_set, minute.toString()))
        } else {
            toast(R.string.timer_cancel)
        }
    }

    private fun logout() {
        showConfirmDialog(message = "确认退出登录？") {
            lifecycleScope.launch {
                userService.logout()
            }
        }
    }

    private fun toggleDesktopLyrics() {
        val on = !me.wcy.music.compose.component.desktopLyricsOn.value
        if (on && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
            && !android.provider.Settings.canDrawOverlays(this)) {
            toast("开启桌面歌词需先授予悬浮窗权限")
            val intent = Intent(
                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${packageName}")
            )
            startActivity(intent)
            return
        }
        me.wcy.music.compose.component.desktopLyricsOn.value = on
        toast(if (on) "桌面歌词已开启，可在其他应用上方显示" else "桌面歌词已关闭")
    }

    private fun exitApp() {
        application.playerController().stop()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        quitTimer.stop()
    }

    private companion object {
        const val PLAYLIST_CACHE_KEY = "my_playlist"
    }
}

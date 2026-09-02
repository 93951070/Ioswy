package me.wcy.music.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.painter.Painter
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.account.login.phone.PhoneLoginViewModel
import me.wcy.music.account.login.qrcode.QrcodeLoginViewModel
import me.wcy.music.common.bean.AlbumData
import me.wcy.music.common.bean.ArtistData
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.PlayBar
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.DiscoverScreen
import me.wcy.music.compose.ui.LocalMusicScreen
import me.wcy.music.compose.ui.LocalSongData
import me.wcy.music.compose.ui.MineScreen
import me.wcy.music.compose.ui.PersonalFmScreen
import me.wcy.music.compose.ui.PhoneLoginScreen
import me.wcy.music.compose.ui.PlayingScreen
import me.wcy.music.compose.ui.QualitySheet
import me.wcy.music.compose.ui.qualityLabel
import me.wcy.music.compose.ui.PlaylistDetailScreen
import me.wcy.music.compose.ui.PlaylistSquareScreen
import me.wcy.music.compose.ui.QrcodeLoginScreen
import me.wcy.music.compose.ui.RankingScreen
import me.wcy.music.compose.ui.RecommendSongScreen
import me.wcy.music.compose.ui.SearchScreen
import me.wcy.music.search.SearchType
import me.wcy.music.compose.ui.SettingChoice
import me.wcy.music.compose.ui.SettingItem
import me.wcy.music.compose.ui.SettingsScreen
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.discover.comment.viewmodel.CommentViewModel
import me.wcy.music.discover.fm.viewmodel.PersonalFmViewModel
import me.wcy.music.discover.home.viewmodel.DiscoverViewModel
import me.wcy.music.discover.playlist.detail.viewmodel.PlaylistViewModel
import me.wcy.music.discover.playlist.square.viewmodel.PlaylistSquareViewModel
import me.wcy.music.discover.ranking.viewmodel.RankingViewModel
import me.wcy.music.mine.home.viewmodel.MineViewModel
import me.wcy.music.search.SearchViewModel
import me.wcy.music.shared.account.IosUserSession
import me.wcy.music.shared.net.DEFAULT_BASE_URL
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.net.MineNet
import me.wcy.music.shared.net.SharedNet
import me.wcy.music.shared.net.apiCall
import me.wcy.music.shared.player.IosPlayerEngine
import me.wcy.music.album.detail.AlbumDetailScreen
import me.wcy.music.album.detail.viewmodel.AlbumDetailViewModel
import me.wcy.music.album.new.AlbumNewScreen
import me.wcy.music.album.new.viewmodel.AlbumNewViewModel
import me.wcy.music.artist.detail.ArtistDetailScreen
import me.wcy.music.artist.detail.viewmodel.ArtistDetailViewModel
import me.wcy.music.artist.list.ArtistListScreen
import me.wcy.music.artist.list.viewmodel.ArtistListViewModel
import me.wcy.music.dj.detail.DjDetailScreen
import me.wcy.music.dj.detail.viewmodel.DjDetailViewModel
import me.wcy.music.dj.list.DjRecommendScreen
import me.wcy.music.dj.list.viewmodel.DjRecommendViewModel
import me.wcy.music.mine.extra.cloud.CloudDiskScreen
import me.wcy.music.mine.extra.cloud.CloudDiskViewModel
import me.wcy.music.mine.extra.msg.MsgCenterScreen
import me.wcy.music.mine.extra.msg.MsgDetailScreen
import me.wcy.music.compose.ui.VideoScreen
import me.wcy.music.compose.ui.ImportPlaylistScreen
import me.wcy.music.compose.ui.ImportPlaylistViewModel
import me.wcy.music.compose.ui.DjRankScreen
import me.wcy.music.compose.ui.CommentFloorScreen
import me.wcy.music.discover.comment.viewmodel.CommentFloorViewModel
import me.wcy.music.mine.extra.msg.MsgCenterViewModel
import me.wcy.music.mine.extra.recent.RecentPlayScreen
import me.wcy.music.mine.extra.recent.RecentPlayViewModel
import me.wcy.music.mine.extra.sub.SubListScreen
import me.wcy.music.mine.extra.sub.SubListViewModel
import me.wcy.music.mv.MvListScreen
import me.wcy.music.mv.detail.MvDetailScreen
import me.wcy.music.mv.detail.viewmodel.MvDetailViewModel
import me.wcy.music.mv.viewmodel.MvListViewModel
import me.wcy.music.personalnewsong.NewSongScreen
import me.wcy.music.personalnewsong.viewmodel.NewSongViewModel
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.UIKit.UIApplicationDidBecomeActiveNotification

private enum class IosTab(val label: String) {
    Discover("发现"),
    Mine("我的")
}

private const val KEY_API_DOMAIN = "ios_api_domain"

private fun loadApiDomain(): String =
    NSUserDefaults.standardUserDefaults.stringForKey(KEY_API_DOMAIN) ?: ""

private fun saveApiDomain(domain: String) {
    NSUserDefaults.standardUserDefaults.setObject(domain, forKey = KEY_API_DOMAIN)
}

private const val KEY_DARK_MODE = "ios_dark_mode"

private fun loadDarkMode(): String =
    NSUserDefaults.standardUserDefaults.stringForKey(KEY_DARK_MODE) ?: "system"

private fun saveDarkMode(mode: String) {
    NSUserDefaults.standardUserDefaults.setObject(mode, forKey = KEY_DARK_MODE)
}

/** 简单页栈：只允许栈顶页面参与组合，backStack 空时显示首页 3 tab */
private sealed interface IosPage {
    data class PlaylistDetail(
        val id: Long,
        val realtimeData: Boolean,
        val isLike: Boolean
    ) : IosPage

    data object PlaylistSquare : IosPage
    data object Ranking : IosPage
    data object RecommendSong : IosPage
    data object PersonalFm : IosPage
    data object Search : IosPage
    data object Settings : IosPage
    data object Playing : IosPage
    data object Login : IosPage
    data object LocalMusic : IosPage
    data object ArtistList : IosPage
    data class ArtistDetail(val id: Long) : IosPage
    data class AlbumDetail(val id: Long) : IosPage
    data object AlbumNew : IosPage
    data object MvList : IosPage
    data class MvDetail(val id: Long) : IosPage
    data object DjRecommend : IosPage
    data class DjDetail(val rid: Long) : IosPage
    data object NewSong : IosPage
    data object RecentPlay : IosPage
    data object SubList : IosPage
    data object CloudDisk : IosPage
    data object MsgCenter : IosPage
    data class MsgDetail(val uid: Long, val nickname: String) : IosPage
    data object Video : IosPage
    data object DjRank : IosPage
    data object ImportPlaylist : IosPage
    data class CommentFloor(
        val resourceId: Long,
        val resourceType: Int,
        val parentCommentId: Long
    ) : IosPage
}

@Composable
fun IosRoot() {
    val engine = remember { IosPlayerEngine() }
    val session = remember { IosUserSession() }
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(IosTab.Discover) }
    val backStack = remember { mutableStateListOf<IosPage>() }
    var message by remember { mutableStateOf<String?>(null) }

    // 外观与定时停止（设置页/抽屉共用）
    var darkModeOverride by remember { mutableStateOf(loadDarkMode()) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var timerJob by remember { mutableStateOf<Job?>(null) }

    // VM 在组合根创建，构造参数与 commonMain 定义精确对应
    val discoverViewModel = remember {
        DiscoverViewModel(
            profileFlow = session.profile,
            hasApiDomain = { loadApiDomain().isNotEmpty() },
            cache = null
        )
    }
    val mineViewModel = remember { MineViewModel(profileFlow = session.profile) }

    var likeSongIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // 签到状态：按日期本地持久化（上游 daily_signin 返回「功能暂不支持」）
    var todaySignState by remember {
        mutableStateOf(NSUserDefaults.standardUserDefaults.stringForKey(KEY_SIGN_DATE) ?: "")
    }

    fun toast(msg: String) {
        message = msg
    }

    fun applyTimerStop(minutes: Int) {
        timerJob?.cancel()
        timerJob = null
        if (minutes <= 0) {
            toast("已取消定时停止")
        } else {
            timerJob = scope.launch {
                delay(minutes * 60_000L)
                // IosPlayerEngine 无独立 pause：播放中才触发 playPause，保证"到点暂停"语义
                if (engine.isPlaying.value) engine.playPause()
                toast("定时时间到，已停止播放")
            }
            toast("已设置定时停止")
        }
        showTimerDialog = false
    }

    LaunchedEffect(message) {
        if (message != null) {
            delay(2000)
            message = null
        }
    }

    fun push(page: IosPage) {
        backStack.add(page)
    }

    fun pop() {
        backStack.removeLastOrNull()
    }

    fun refreshLikeList() {
        val uid = session.getUserId()
        if (uid <= 0) {
            likeSongIds = emptySet()
            return
        }
        scope.launch {
            val data = runCatching { MineNet.getMyLikeSongList(uid) }.getOrNull()
            if (data?.code == 200) {
                likeSongIds = data.ids.toSet()
            }
        }
    }

    LaunchedEffect(Unit) {
        session.profile.collectLatest { profile ->
            if (profile == null) {
                likeSongIds = emptySet()
            } else {
                refreshLikeList()
            }
        }
    }

    fun toggleLike(songId: Long) {
        if (session.isLogin().not()) {
            toast("请先登录")
            return
        }
        scope.launch {
            val res = apiCall { MineNet.likeSong(songId, songId !in likeSongIds) }
            if (res.isSuccess()) {
                likeSongIds = if (songId in likeSongIds) {
                    likeSongIds - songId
                } else {
                    likeSongIds + songId
                }
            } else {
                toast(res.msg ?: "操作失败")
            }
        }
    }

    fun fetchPlaylistSongs(playlistId: Long, onLoaded: (List<SongData>) -> Unit) {
        scope.launch {
            val data = runCatching { DiscoverNet.getFullPlaylistSongList(playlistId) }.getOrNull()
            if (data != null && data.code == 200) {
                onLoaded(data.songs)
            } else {
                toast("歌单加载失败")
            }
        }
    }

    val onPlayPlaylistSong: (PlaylistData, Int) -> Unit = { playlist, position ->
        fetchPlaylistSongs(playlist.id) { songs ->
            if (songs.isNotEmpty()) {
                engine.playSongList(songs, position.coerceIn(songs.indices))
            }
        }
    }

    val darkTheme = when (darkModeOverride) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    MusicTheme(darkTheme = darkTheme) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        var showDomainDialog by remember { mutableStateOf(false) }
        var domainInput by remember { mutableStateOf(SharedNet.baseUrl) }

        LaunchedEffect(Unit) {
            loadApiDomain().takeIf { it.isNotEmpty() }?.let { SharedNet.baseUrl = it }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = backStack.isEmpty(),
            drawerContent = {
                ModalDrawerSheet {
                    IosDrawerContent(
                        session = session,
                        onClose = { scope.launch { drawerState.close() } },
                        onOpenLogin = {
                            scope.launch { drawerState.close() }
                            push(IosPage.Login)
                        },
                        onLogout = {
                            scope.launch {
                                session.logout()
                                toast("已退出登录")
                                drawerState.close()
                            }
                        },
                        onOpenDomainSettings = {
                            scope.launch { drawerState.close() }
                            domainInput = SharedNet.baseUrl
                            showDomainDialog = true
                        },
                        onOpenSettings = { push(IosPage.Settings) },
                        onOpenTimer = { showTimerDialog = true },
                        onOpenMsgCenter = {
                            scope.launch { drawerState.close() }
                            push(IosPage.MsgCenter)
                        },
                        onMessage = ::toast
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppThemeColor.Background)
            ) {
            val page = backStack.lastOrNull()
            if (page == null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentTab) {
                            IosTab.Discover -> DiscoverScreen(
                                viewModel = discoverViewModel,
                                playerEngine = engine,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onOpenSearch = { push(IosPage.Search) },
                                onOpenPlaylistDetail = { id ->
                                    push(IosPage.PlaylistDetail(id, realtimeData = false, isLike = false))
                                },
                                onOpenRanking = { push(IosPage.Ranking) },
                                onOpenPlaylistSquare = { push(IosPage.PlaylistSquare) },
                                onOpenRecommendSong = { push(IosPage.RecommendSong) },
                                onOpenPersonalFm = { push(IosPage.PersonalFm) },
                                onOpenArtistList = { push(IosPage.ArtistList) },
                                onOpenNewSong = { push(IosPage.NewSong) },
                                onOpenDj = { push(IosPage.DjRecommend) },
                                onOpenMvList = { push(IosPage.MvList) },
                                onOpenPlaying = { push(IosPage.Playing) },
                                onPlaySong = { song -> engine.playSongList(listOf(song), 0) },
                                onPlayPlaylist = { playlist ->
                                    fetchPlaylistSongs(playlist.id) { engine.playSongList(it, 0) }
                                },
                                onPlayPlaylistSong = onPlayPlaylistSong,
                                onPlayDailySong = { songs, index ->
                                    engine.playSongList(songs, index)
                                },
                                onOpenArtist = { id -> push(IosPage.ArtistDetail(id)) },
                                onOpenDjRadio = { id -> push(IosPage.DjDetail(id)) },
                                onOpenMv = { id -> push(IosPage.MvDetail(id)) },
                                onOpenVideo = { push(IosPage.Video) },
                                onOpenDjRank = { push(IosPage.DjRank) }
                            )
                            else -> MineScreen(
                                viewModel = mineViewModel,
                                profileFlow = session.profile,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onOpenSearch = { push(IosPage.Search) },
                                onOpenLogin = { push(IosPage.Login) },
                                onOpenLocalMusic = { push(IosPage.LocalMusic) },
                                onOpenRecentPlay = { push(IosPage.RecentPlay) },
                                onOpenSubList = { push(IosPage.SubList) },
                                onOpenCloudDisk = { push(IosPage.CloudDisk) },
                                onOpenMsgCenter = { push(IosPage.MsgCenter) },
                                onOpenPlaylistDetail = { playlist, realtimeData, isLike ->
                                    push(IosPage.PlaylistDetail(playlist.id, realtimeData, isLike))
                                },
                                onOpenImport = { push(IosPage.ImportPlaylist) },
                                signedToday = todaySignState == todayString(),
                                onSignin = {
                                    NSUserDefaults.standardUserDefaults.setObject(todayString(), forKey = KEY_SIGN_DATE)
                                    todaySignState = todayString()
                                    toast("签到成功，今日已签")
                                }
                            )
                        }
                    }

                    PlayBar(
                        playerEngine = engine,
                        onOpenPlaying = { push(IosPage.Playing) },
                        // TODO 播放列表面板暂并入播放页（内含列表 Sheet）
                        onOpenPlaylist = { push(IosPage.Playing) }
                    )

                    HorizontalDivider(color = AppThemeColor.Divider)

                    BottomTabBar(
                        current = currentTab,
                        onSelect = { currentTab = it }
                    )
                }
            } else {
                when (page) {
                    is IosPage.PlaylistDetail -> PlaylistDetailPage(
                        page = page,
                        engine = engine,
                        onBack = { pop() },
                        onOpenPlaying = { push(IosPage.Playing) }
                    )
                    IosPage.PlaylistSquare -> PlaylistSquarePage(
                        onBack = { pop() },
                        onOpenPlaylistDetail = { id ->
                            push(IosPage.PlaylistDetail(id, realtimeData = false, isLike = false))
                        },
                        onPlayPlaylist = { playlist ->
                            fetchPlaylistSongs(playlist.id) { engine.playSongList(it, 0) }
                        }
                    )
                    IosPage.Ranking -> RankingPage(
                        onBack = { pop() },
                        onOpenPlaylistDetail = { id ->
                            push(IosPage.PlaylistDetail(id, realtimeData = false, isLike = false))
                        },
                        onPlayPlaylistSong = onPlayPlaylistSong
                    )
                    IosPage.RecommendSong -> RecommendSongPage(
                        engine = engine,
                        onBack = { pop() }
                    )
                    IosPage.PersonalFm -> PersonalFmPage(
                        engine = engine,
                        isLiked = { songId -> songId in likeSongIds },
                        onToggleLike = { songId -> toggleLike(songId) },
                        onBack = { pop() }
                    )
                    IosPage.Search -> SearchPage(
                        engine = engine,
                        onBack = { pop() },
                        onOpenPlaylistDetail = { id ->
                            push(IosPage.PlaylistDetail(id, realtimeData = false, isLike = false))
                        },
                        onClickItem = { type, id ->
                            when (type) {
                                SearchType.ARTIST -> push(IosPage.ArtistDetail(id))
                                SearchType.ALBUM -> push(IosPage.AlbumDetail(id))
                                SearchType.PLAYLIST -> push(IosPage.PlaylistDetail(id, realtimeData = false, isLike = false))
                                SearchType.MV -> push(IosPage.MvDetail(id))
                                SearchType.RADIO -> push(IosPage.DjDetail(id))
                                else -> message = "该类型暂不支持跳转"
                            }
                        }
                    )
                    IosPage.LocalMusic -> LocalMusicTab(
                        engine = engine,
                        onBack = { pop() }
                    )
                    IosPage.Settings -> SettingsPage(
                        darkModeOverride = darkModeOverride,
                        onItemChange = { key, value ->
                            if (key == "dark_mode") {
                                darkModeOverride = value
                                saveDarkMode(value)
                                toast("外观已切换")
                            }
                        },
                        onBack = { pop() }
                    )
                    IosPage.Playing -> PlayingPage(
                        engine = engine,
                        isLiked = { songId -> songId in likeSongIds },
                        onToggleLike = { songId -> toggleLike(songId) },
                        onMessage = { toast(it) },
                        onOpenFloor = { pid ->
                            engine.currentSong.value?.let { song ->
                                push(IosPage.CommentFloor(song.id, 0, pid))
                            }
                        },
                        onBack = { pop() }
                    )
                    IosPage.Login -> LoginPage(
                        session = session,
                        onLoginSuccess = { pop() },
                        onMessage = { toast(it) },
                        onBack = { pop() }
                    )
                    IosPage.ArtistList -> {
                        val vm = remember { ArtistListViewModel() }
                        ArtistListScreen(
                            viewModel = vm,
                            onBack = { pop() },
                            onOpenArtist = { push(IosPage.ArtistDetail(it)) }
                        )
                    }
                    is IosPage.ArtistDetail -> {
                        val vm = remember { ArtistDetailViewModel() }
                        ArtistDetailScreen(
                            viewModel = vm,
                            artistId = page.id,
                            onBack = { pop() },
                            onPlaySongs = { songs, index -> engine.playSongList(songs, index) },
                            onOpenAlbum = { push(IosPage.AlbumDetail(it)) },
                            onOpenMv = { push(IosPage.MvDetail(it)) }
                        )
                    }
                    is IosPage.AlbumDetail -> {
                        val vm = remember { AlbumDetailViewModel() }
                        AlbumDetailScreen(
                            viewModel = vm,
                            albumId = page.id,
                            onBack = { pop() },
                            onOpenArtist = { push(IosPage.ArtistDetail(it)) },
                            onPlaySongs = { songs, index -> engine.playSongList(songs, index) }
                        )
                    }
                    IosPage.AlbumNew -> {
                        val vm = remember { AlbumNewViewModel() }
                        AlbumNewScreen(
                            viewModel = vm,
                            onBack = { pop() },
                            onOpenAlbum = { push(IosPage.AlbumDetail(it)) }
                        )
                    }
                    IosPage.MvList -> {
                        val vm = remember { MvListViewModel() }
                        MvListScreen(
                            viewModel = vm,
                            onBack = { pop() },
                            onOpenMv = { push(IosPage.MvDetail(it)) }
                        )
                    }
                    is IosPage.MvDetail -> {
                        val vm = remember { MvDetailViewModel() }
                        // 进 MV 页先停音乐，避免视频声音与音乐叠加
                        LaunchedEffect(Unit) { engine.pause() }
                        MvDetailScreen(
                            viewModel = vm,
                            mvid = page.id,
                            onBack = { pop() },
                            onOpenFloor = { pid ->
                                push(IosPage.CommentFloor(page.id, 1, pid))
                            },
                            onOpenMv = { mvId -> push(IosPage.MvDetail(mvId)) }
                        )
                    }
                    IosPage.DjRecommend -> {
                        val vm = remember { DjRecommendViewModel() }
                        DjRecommendScreen(
                            viewModel = vm,
                            onBack = { pop() },
                            onOpenDj = { push(IosPage.DjDetail(it)) }
                        )
                    }
                    is IosPage.DjDetail -> {
                        val vm = remember { DjDetailViewModel() }
                        DjDetailScreen(
                            viewModel = vm,
                            rid = page.rid,
                            onBack = { pop() },
                            onPlaySongs = { songs, index -> engine.playSongList(songs, index) },
                            onOpenFloor = { pid ->
                                push(IosPage.CommentFloor(page.rid, 4, pid))
                            }
                        )
                    }
                    IosPage.NewSong -> {
                        val vm = remember { NewSongViewModel() }
                        NewSongScreen(
                            viewModel = vm,
                            onBack = { pop() },
                            onPlaySongs = { songs, index -> engine.playSongList(songs, index) }
                        )
                    }
                    IosPage.RecentPlay -> {
                        val vm = remember { RecentPlayViewModel() }
                        vm.uid = session.getUserId()
                        RecentPlayScreen(
                            viewModel = vm,
                            onBack = { pop() },
                            onPlaySongs = { songs, index -> engine.playSongList(songs, index) }
                        )
                    }
                    IosPage.SubList -> {
                        val vm = remember { SubListViewModel() }
                        SubListScreen(
                            viewModel = vm,
                            onBack = { pop() },
                            onOpenArtist = { push(IosPage.ArtistDetail(it)) },
                            onOpenAlbum = { push(IosPage.AlbumDetail(it)) },
                            onOpenMv = { push(IosPage.MvDetail(it)) }
                        )
                    }
                    IosPage.CloudDisk -> {
                        val vm = remember { CloudDiskViewModel() }
                        CloudDiskScreen(
                            viewModel = vm,
                            onBack = { pop() },
                            onPlaySongs = { songs, index -> engine.playSongList(songs, index) },
                            onDelete = { item -> scope.launch { vm.delete(item) } }
                        )
                    }
                    IosPage.MsgCenter -> {
                        val vm = remember { MsgCenterViewModel() }
                        MsgCenterScreen(
                            viewModel = vm,
                            onBack = { pop() },
                            onOpenMsgDetail = { uid, nickname ->
                                push(IosPage.MsgDetail(uid, nickname))
                            }
                        )
                    }

                    is IosPage.MsgDetail -> MsgDetailScreen(
                        uid = page.uid,
                        nickname = page.nickname,
                        onBack = { pop() }
                    )

                    IosPage.Video -> VideoScreen(onBack = { pop() })

                    IosPage.DjRank -> DjRankScreen(
                        onBack = { pop() },
                        onOpenRadio = { id -> push(IosPage.DjDetail(id)) }
                    )

                    IosPage.ImportPlaylist -> ImportPlaylistScreen(
                        viewModel = remember { ImportPlaylistViewModel() },
                        onBack = { pop() }
                    )

                    is IosPage.CommentFloor -> CommentFloorScreen(
                        viewModel = remember { CommentFloorViewModel() },
                        resourceId = page.resourceId,
                        resourceType = page.resourceType,
                        parentCommentId = page.parentCommentId,
                        onBack = { pop() }
                    )
                }
            }

            ToastOverlay(message)
        }
        }
        if (showDomainDialog) {
            AlertDialog(
                onDismissRequest = { showDomainDialog = false },
                title = { Text("接口设置") },
                text = {
                    Column {
                        Text(
                            text = "后端接口域名，留空恢复默认。修改后立即生效。",
                            color = AppThemeColor.TextH2,
                            fontSize = 13.sp
                        )
                        OutlinedTextField(
                            value = domainInput,
                            onValueChange = { domainInput = it },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        var domain = domainInput.trim().trimEnd('/')
                        if (domain.isNotEmpty() &&
                            !domain.startsWith("http://") &&
                            !domain.startsWith("https://")
                        ) {
                            domain = "https://$domain"
                        }
                        saveApiDomain(domain)
                        SharedNet.baseUrl = domain.ifEmpty { DEFAULT_BASE_URL }
                        showDomainDialog = false
                        discoverViewModel.refresh()
                        toast("接口设置已保存")
                    }) { Text("保存", color = AppThemeColor.ThemeColor) }
                },
                dismissButton = {
                    TextButton(onClick = { showDomainDialog = false }) {
                        Text("取消", color = AppThemeColor.TextH2)
                    }
                }
            )
        }
        if (showTimerDialog) {
            AlertDialog(
                onDismissRequest = { showTimerDialog = false },
                title = { Text("定时停止播放") },
                text = {
                    Column {
                        listOf("不停止" to 0, "15分钟" to 15, "30分钟" to 30, "60分钟" to 60, "90分钟" to 90)
                            .forEach { (label, minutes) ->
                                TextButton(
                                    onClick = { applyTimerStop(minutes) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(label, color = AppThemeColor.TextH1)
                                }
                            }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

fun MainViewController() = ComposeUIViewController {
    IosRoot()
}

@Composable
private fun BottomTabBar(
    current: IosTab,
    onSelect: (IosTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(AppThemeColor.Card)
    ) {
        IosTab.entries.forEach { tab ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    color = if (current == tab) AppThemeColor.ThemeColor else AppThemeColor.TextH2,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ToastOverlay(message: String?) {
    message?.let { msg ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = msg,
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(bottom = 100.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun PlaylistSquarePage(
    onBack: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onPlayPlaylist: (PlaylistData) -> Unit
) {
    val viewModel = remember { PlaylistSquareViewModel() }
    PlaylistSquareScreen(
        viewModel = viewModel,
        onBack = onBack,
        onOpenPlaylistDetail = onOpenPlaylistDetail,
        onPlayPlaylist = onPlayPlaylist
    )
}

@Composable
private fun RankingPage(
    onBack: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onPlayPlaylistSong: (PlaylistData, Int) -> Unit
) {
    val viewModel = remember { RankingViewModel() }
    RankingScreen(
        viewModel = viewModel,
        onBack = onBack,
        onOpenPlaylistDetail = onOpenPlaylistDetail,
        onPlayPlaylistSong = onPlayPlaylistSong
    )
}

@Composable
private fun RecommendSongPage(
    engine: IosPlayerEngine,
    onBack: () -> Unit
) {
    RecommendSongScreen(
        onBack = onBack,
        onPlayAll = { songs -> engine.playSongList(songs, 0) },
        onPlaySong = { songs, position -> engine.playSongList(songs, position) }
    )
}

@Composable
private fun PlaylistDetailPage(
    page: IosPage.PlaylistDetail,
    engine: IosPlayerEngine,
    onBack: () -> Unit,
    onOpenPlaying: () -> Unit
) {
    val viewModel = remember { PlaylistViewModel() }
    PlaylistDetailScreen(
        viewModel = viewModel,
        playlistId = page.id,
        realtimeData = page.realtimeData,
        isLike = page.isLike,
        onBack = onBack,
        onOpenPlaying = onOpenPlaying,
        onPlayAll = { songs -> engine.playSongList(songs, 0) },
        onPlaySong = { index, songs -> engine.playSongList(songs, index) }
    )
}

@Composable
private fun SearchPage(
    engine: IosPlayerEngine,
    onBack: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onClickItem: (SearchType, Long) -> Unit
) {
    val viewModel = remember { SearchViewModel(IosSearchHistoryStore()) }
    SearchScreen(
        viewModel = viewModel,
        onBack = onBack,
        onOpenPlaylistDetail = onOpenPlaylistDetail,
        onPlayAll = { songs -> engine.playSongList(songs, 0) },
        onPlaySong = { song -> engine.playSongList(listOf(song), 0) },
        onClickItem = onClickItem
    )
}

@Composable
private fun PersonalFmPage(
    engine: IosPlayerEngine,
    isLiked: (Long) -> Boolean,
    onToggleLike: (Long) -> Unit,
    onBack: () -> Unit
) {
    val viewModel = remember { PersonalFmViewModel() }

    fun loadMore() {
        viewModel.loadFm { songs ->
            if (engine.currentSong.value == null) {
                engine.playSongList(songs, 0)
            } else {
                engine.appendSongs(
                    songs.filter { song -> engine.playlist.value.none { it.id == song.id } }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        loadMore()
    }

    PersonalFmScreen(
        currentSong = engine.currentSong,
        isPlaying = engine.isPlaying,
        playProgress = engine.playProgress,
        fmError = viewModel.error,
        onPlayPause = { engine.playPause() },
        onSeekTo = { engine.seekTo(it) },
        onNext = {
            engine.next()
            loadMore()
        },
        isLiked = isLiked,
        onLike = onToggleLike,
        onErrorRetry = { loadMore() },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayingPage(
    engine: IosPlayerEngine,
    isLiked: (Long) -> Boolean,
    onToggleLike: (Long) -> Unit,
    onMessage: (String) -> Unit,
    onOpenFloor: (Long) -> Unit,
    onBack: () -> Unit
) {
    val commentViewModel = remember { CommentViewModel(IosMyCommentStore) }
    val currentSong by engine.currentSong.collectAsState()

    // 切后台回来后同步真实播放态：中断/后台期间 AVPlayer 状态漂移，周期观察器停摆，StateFlow 可能残留旧值
    DisposableEffect(Unit) {
        val token = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _ -> engine.refreshPlayingState() }
        onDispose { NSNotificationCenter.defaultCenter.removeObserver(token) }
    }

    var lrcContent by remember { mutableStateOf("") }
    var lrcLabel by remember { mutableStateOf("歌词加载中…") }
    var menuSong by remember { mutableStateOf<SongData?>(null) }
    var showQualitySheet by remember { mutableStateOf(false) }
    var playQuality by remember {
        mutableStateOf(
            NSUserDefaults.standardUserDefaults.stringForKey(IosPlayerEngine.PLAY_QUALITY_KEY)
                ?: IosPlayerEngine.PLAY_LEVEL
        )
    }

    LaunchedEffect(currentSong?.id) {
        val songId = currentSong?.id ?: return@LaunchedEffect
        lrcContent = ""
        lrcLabel = "歌词加载中…"
        val lrc = runCatching { DiscoverNet.getLrc(songId) }.getOrNull()
        if (lrc != null && lrc.code == 200 && lrc.lrc.isValid()) {
            lrcContent = lrc.lrc.lyric
        } else {
            lrcLabel = "暂无歌词"
        }
    }

    PlayingScreen(
        playerEngine = engine,
        commentViewModel = commentViewModel,
        onClose = onBack,
        isLiked = isLiked,
        onToggleLike = onToggleLike,
        onShare = { song, songId ->
            IosShareHelper.shareText("分享歌曲：${song.name}\nhttps://music.163.com/song?id=$songId")
        },
        onOpenMenu = { song, _ -> menuSong = song },
        onDownload = { onMessage("敬请期待") },
        onMessage = onMessage,
        onOpenFloor = onOpenFloor,
        onPlaylistEmpty = onBack,
        soundQuality = playQuality,
        onSelectQuality = {},
        lrcContent = lrcContent,
        onUpdateLrc = {},
        lrcLabel = lrcLabel
    )

    menuSong?.let { song ->
        ModalBottomSheet(
            onDismissRequest = { menuSong = null },
            containerColor = Color.White
        ) {
            IosMenuRow(Icons.Filled.FavoriteBorder, "收藏到我喜欢") {
                onToggleLike(song.id)
                menuSong = null
            }
            IosMenuRow(Icons.Filled.QueueMusic, "下一首播放") {
                engine.playNext(song)
                menuSong = null
            }
            IosMenuRow(Icons.Filled.Settings, "音质：${qualityLabel(playQuality)}") {
                menuSong = null
                showQualitySheet = true
            }
        }
    }

    if (showQualitySheet) {
        QualitySheet(
            currentQuality = playQuality,
            onSelectQuality = { level ->
                NSUserDefaults.standardUserDefaults.setObject(level, forKey = IosPlayerEngine.PLAY_QUALITY_KEY)
                playQuality = level
                engine.replayCurrent()
            },
            onDismiss = { showQualitySheet = false }
        )
    }
}

@Composable
private fun SettingsPage(
    darkModeOverride: String,
    onItemChange: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val items = listOf(
        SettingItem(
            key = "dark_mode",
            category = "通用",
            title = "外观",
            dialogTitle = "外观",
            value = darkModeOverride,
            options = listOf(
                SettingChoice("跟随系统", "system"),
                SettingChoice("浅色模式", "light"),
                SettingChoice("深色模式", "dark")
            )
        )
    )
    SettingsScreen(
        items = items,
        onItemChange = onItemChange,
        onOpenSoundEffect = {},
        onBack = onBack
    )
}

@Composable
private fun LoginPage(
    session: IosUserSession,
    onLoginSuccess: () -> Unit,
    onMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    var showQrcode by remember { mutableStateOf(false) }
    if (showQrcode) {
        val viewModel = remember { QrcodeLoginViewModel(session) }
        val qrUrl by viewModel.qrUrl.collectAsState()
        val qrContent: String? = qrUrl
        val qrPainter: Painter? = if (qrContent.isNullOrEmpty()) null else rememberQrCodePainter(qrContent)
        QrcodeLoginScreen(
            viewModel = viewModel,
            qrCodeImage = null,
            qrPainter = qrPainter,
            onLoginSuccess = onLoginSuccess,
            onSwitchPhone = { showQrcode = false },
            onMessage = onMessage,
            onBack = onBack
        )
    } else {
        val viewModel = remember { PhoneLoginViewModel(session) }
        PhoneLoginScreen(
            viewModel = viewModel,
            onLoginSuccess = onLoginSuccess,
            onSwitchQrcode = { showQrcode = true },
            onMessage = onMessage,
            onBack = onBack
        )
    }
}

@Composable
private fun LocalMusicTab(
    engine: IosPlayerEngine,
    onBack: () -> Unit
) {
    var songs by remember { mutableStateOf<List<LocalSongData>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        songs = withContext(Dispatchers.Default) { scanLocalAudio() }
        loaded = true
    }

    fun playAt(index: Int) {
        val list = songs
        if (index in list.indices) {
            engine.playLocalSongs(
                songs = list.map { it.toEngineSong() },
                paths = list.map { it.path },
                index = index
            )
        }
    }

    LocalMusicScreen(
        songs = songs,
        loaded = loaded,
        onPlaySong = { index -> playAt(index) },
        onPlayAll = { playAt(0) },
        onBack = onBack
    )
}

private fun LocalSongData.toEngineSong(): SongData = SongData(
    id = id,
    name = title,
    ar = listOf(ArtistData(name = artist)),
    al = AlbumData(name = album),
    dt = duration
)

private val AUDIO_EXTENSIONS = setOf("mp3", "flac", "m4a", "aac", "wav", "ogg", "wma")

/** 扫描沙盒 Documents 目录下的音频文件（无 MediaStore 等价物，暂只扫用户目录） */
@OptIn(ExperimentalForeignApi::class)
private fun scanLocalAudio(): List<LocalSongData> {
    // 沙盒容器内 Documents 固定为 $NSHomeDirectory/Documents
    val docDir = NSHomeDirectory() + "/Documents"
    val fileManager = NSFileManager.defaultManager
    return fileManager.subpathsAtPath(docDir)
        ?.filterIsInstance<String>()
        ?.filter { it.substringAfterLast('.', "").lowercase() in AUDIO_EXTENSIONS }
        ?.map { relativePath ->
            val path = "$docDir/$relativePath"
            val size = runCatching {
                (fileManager.attributesOfItemAtPath(path, null)?.get("NSFileSize") as? Number)
                    ?.toLong() ?: 0L
            }.getOrDefault(0L)
            val fileName = relativePath.substringAfterLast('/')
            LocalSongData(
                id = path.hashCode().toLong(),
                title = fileName.substringBeforeLast('.'),
                artist = "本地音乐",
                album = "",
                cover = "",
                duration = 0L,
                fileName = fileName,
                fileSize = size,
                path = path
            )
        } ?: emptyList()
}

@Composable
private fun IosDrawerContent(
    session: IosUserSession,
    onClose: () -> Unit,
    onOpenLogin: () -> Unit,
    onLogout: () -> Unit,
    onOpenDomainSettings: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenMsgCenter: () -> Unit,
    onMessage: (String) -> Unit
) {
    val profile by session.profile.collectAsState()

    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(AppThemeColor.Card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(AppThemeColor.ThemeColor)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val p: ProfileData? = profile
            if (p != null) {
                CoverImage(
                    url = p.avatarUrl,
                    cornerRadius = 28.dp,
                    modifier = Modifier.size(56.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "未登录",
                    tint = Color.White,
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(onClick = onOpenLogin)
                )
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    text = profile?.nickname ?: "点击登录",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable(enabled = profile == null, onClick = onOpenLogin)
                )
                Text(
                    text = profile?.nickname?.let { "网易云音乐" } ?: "登录后同步收藏歌单",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        IosMenuRow(Icons.Filled.Public, "接口设置") {
            onClose(); onOpenDomainSettings()
        }
        IosMenuRow(Icons.Filled.Settings, "功能设置") {
            onClose(); onOpenSettings()
        }
        IosMenuRow(Icons.Filled.Timer, "定时停止播放") {
            onClose(); onOpenTimer()
        }
        IosMenuRow(Icons.Filled.Email, "我的消息") {
            onClose(); onOpenMsgCenter()
        }
        if (profile != null) {
            IosMenuRow(Icons.Filled.ExitToApp, "退出登录") {
                onClose(); onLogout()
            }
        }
        IosMenuRow(Icons.Filled.Info, "关于") {
            onClose(); onMessage("PonyMusic 仿网易云音乐 · Compose Multiplatform 版")
        }
    }
}

@Composable
private fun IosMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AppThemeColor.TextH1,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = AppThemeColor.TextH1,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

private const val KEY_SIGN_DATE = "ios_sign_date"

private fun todayString(): String =
    kotlinx.datetime.Clock.System.now().toString().take(10)

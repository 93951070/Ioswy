package me.wcy.music.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import me.wcy.music.compose.ui.PlaylistDetailScreen
import me.wcy.music.compose.ui.PlaylistSquareScreen
import me.wcy.music.compose.ui.QrcodeLoginScreen
import me.wcy.music.compose.ui.RankingScreen
import me.wcy.music.compose.ui.RecommendSongScreen
import me.wcy.music.compose.ui.SearchScreen
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
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.net.MineNet
import me.wcy.music.shared.net.apiCall
import me.wcy.music.shared.player.IosPlayerEngine
import org.jetbrains.skia.Image
import org.jetbrains.skia.image.toComposeImageBitmap
import platform.CoreGraphics.*
import platform.CoreImage.*
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.UIKit.*
import platform.posix.memcpy

private enum class IosTab(val label: String) {
    Discover("发现"),
    Mine("我的")
}

/** iOS 端二维码位图：CIFilter(qrCodeGenerator) -> CGImage -> PNG -> Skia ImageBitmap */
@OptIn(ExperimentalForeignApi::class)
private fun generateQrBitmap(content: String): ImageBitmap? = runCatching {
    val filter = CIFilter.qrCodeGenerator()
    filter.message = content.encodeToByteArray().toNSData()
    val output = filter.outputImage
        ?.imageByApplyingTransform(CGAffineTransformMakeScale(10.0, 10.0))
        ?: return@runCatching null
    val context = CIContext()
    val cgImage = context.createCGImage(output, fromRect = output.extent)
        ?: return@runCatching null
    val pngData = UIImage(cgImage = cgImage).PNGData() ?: return@runCatching null
    Image.makeFromEncoded(pngData.toKotlinBytes()).toComposeImageBitmap()
}.getOrNull()

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData =
    usePinned { pinned -> NSData.create(bytes = pinned.addressOf(0), length = size.toULong()) }

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toKotlinBytes(): ByteArray {
    val size = length.toInt()
    if (size <= 0) return ByteArray(0)
    val dst = ByteArray(size)
    dst.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, size.toULong()) }
    return dst
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
    data object Playing : IosPage
    data object Login : IosPage
    data object LocalMusic : IosPage
}

@Composable
fun IosRoot() {
    val engine = remember { IosPlayerEngine() }
    val session = remember { IosUserSession() }
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(IosTab.Discover) }
    val backStack = remember { mutableStateListOf<IosPage>() }
    var message by remember { mutableStateOf<String?>(null) }

    // VM 在组合根创建，构造参数与 commonMain 定义精确对应
    val discoverViewModel = remember {
        DiscoverViewModel(
            profileFlow = session.profile,
            hasApiDomain = { false },
            cache = null
        )
    }
    val mineViewModel = remember { MineViewModel(profileFlow = session.profile) }

    var likeSongIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    fun toast(msg: String) {
        message = msg
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

    MusicTheme {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        ModalNavigationDrawer(
            drawerState = drawerState,
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
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onOpenSearch = { push(IosPage.Search) },
                                onOpenPlaylistDetail = { id ->
                                    push(IosPage.PlaylistDetail(id, realtimeData = false, isLike = false))
                                },
                                onOpenRanking = { push(IosPage.Ranking) },
                                onOpenPlaylistSquare = { push(IosPage.PlaylistSquare) },
                                onOpenRecommendSong = { push(IosPage.RecommendSong) },
                                onOpenPersonalFm = { push(IosPage.PersonalFm) },
                                onOpenPlaying = { push(IosPage.Playing) },
                                onPlaySong = { song -> engine.playSongList(listOf(song), 0) },
                                onPlayPlaylist = { playlist ->
                                    fetchPlaylistSongs(playlist.id) { engine.playSongList(it, 0) }
                                },
                                onPlayPlaylistSong = onPlayPlaylistSong
                            )
                            else -> MineScreen(
                                viewModel = mineViewModel,
                                profileFlow = session.profile,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onOpenSearch = { push(IosPage.Search) },
                                onOpenLogin = { push(IosPage.Login) },
                                onOpenLocalMusic = { push(IosPage.LocalMusic) },
                                onOpenPlaylistDetail = { playlist, realtimeData, isLike ->
                                    push(IosPage.PlaylistDetail(playlist.id, realtimeData, isLike))
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
                        }
                    )
                    IosPage.LocalMusic -> LocalMusicTab(
                        engine = engine,
                        onBack = { pop() }
                    )
                    IosPage.Playing -> PlayingPage(
                        engine = engine,
                        isLiked = { songId -> songId in likeSongIds },
                        onToggleLike = { songId -> toggleLike(songId) },
                        onMessage = { toast(it) },
                        onBack = { pop() }
                    )
                    IosPage.Login -> LoginPage(
                        session = session,
                        onLoginSuccess = { pop() },
                        onMessage = { toast(it) },
                        onBack = { pop() }
                    )
                }
            }

            ToastOverlay(message)
        }
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
    onOpenPlaylistDetail: (Long) -> Unit
) {
    val viewModel = remember { SearchViewModel(IosSearchHistoryStore()) }
    SearchScreen(
        viewModel = viewModel,
        onBack = onBack,
        onOpenPlaylistDetail = onOpenPlaylistDetail,
        onPlayAll = { songs -> engine.playSongList(songs, 0) },
        onPlaySong = { song -> engine.playSongList(listOf(song), 0) }
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
    onBack: () -> Unit
) {
    val commentViewModel = remember { CommentViewModel() }
    val currentSong by engine.currentSong.collectAsState()
    var lrcContent by remember { mutableStateOf("") }
    var lrcLabel by remember { mutableStateOf("歌词加载中…") }
    var menuSong by remember { mutableStateOf<SongData?>(null) }

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
        // TODO iOS 系统分享面板（UIActivityViewController）未接，先以消息展示分享链接
        onShare = { _, songId ->
            onMessage("分享链接：https://music.163.com/song?id=$songId")
        },
        onOpenMenu = { song, _ -> menuSong = song },
        onDownload = { onMessage("敬请期待") },
        onMessage = onMessage,
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
        }
    }
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
        var qrCodeImage by remember { mutableStateOf<ImageBitmap?>(null) }
        val qrUrl by viewModel.qrUrl.collectAsState()
        LaunchedEffect(qrUrl) {
            qrCodeImage = withContext(Dispatchers.Default) { qrUrl?.let { generateQrBitmap(it) } }
        }
        QrcodeLoginScreen(
            viewModel = viewModel,
            qrCodeImage = qrCodeImage,
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
            if (profile != null) {
                CoverImage(
                    url = profile.avatarUrl,
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

        IosMenuRow(Icons.Filled.Email, "我的消息") {
            onClose(); onMessage("功能开发中")
        }
        IosMenuRow(Icons.Filled.ShoppingCart, "云贝商城") {
            onClose(); onMessage("功能开发中")
        }
        IosMenuRow(Icons.Filled.Info, "我的等级") {
            onClose(); onMessage("功能开发中")
        }
        IosMenuRow(Icons.Filled.Star, "会员中心") {
            onClose(); onMessage("功能开发中")
        }
        if (profile != null) {
            IosMenuRow(Icons.Filled.ExitToApp, "退出登录") {
                onClose(); onLogout()
            }
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

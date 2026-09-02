package me.wcy.music.compose.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.SongData
import androidx.compose.runtime.toMutableStateList
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.theme.Red500
import me.wcy.music.compose.ui.PlayingScreen
import me.wcy.music.discover.comment.viewmodel.CommentViewModel
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.net.MineNet
import me.wcy.music.shared.net.PlayExtraNet
import me.wcy.music.shared.player.PlayerEngine
import me.wcy.music.shared.player.downloadSongAsync

/** 播放模式随机，PlayerEngine 约定 0 Loop / 1 Shuffle / 2 Single */
private const val MODE_SHUFFLE = 1

/** 心动 tab 阶段 */
private const val PHASE_INIT = 0
private const val PHASE_LOADING = 1
private const val PHASE_READY = 2
private const val PHASE_EMPTY = 3
private const val PHASE_FAIL = 4

/**
 * 心动 tab 状态放进程级全局：底部 tab 切换会整体销毁 DiscoverScreen，
 * Compose 侧 remember 会丢状态导致每次切回都重新随机换歌。
 */
private val heartPhase = mutableStateOf(PHASE_INIT)
private var heartEmptyMessage = ""
private var heartFailMessage = ""
private val heartLikedIds = mutableStateOf<Set<Long>>(emptySet())

/**
 * 心动 tab：直接复用播放页界面（网易云心动模式）。
 * 初始化：likelist 拉喜欢列表 -> 随机取 50 首查详情 -> 整组随机模式交给全局播放器。
 * ponytail: 绕过 playmode/intelligence/list（其 pid 依赖 user/playlist 链路，
 * 部分账号返回 400）；纯喜欢列表随机播放，要相似推荐时再接回 intelligence。
 */
@Composable
fun HeartTab(
    playerEngine: PlayerEngine,
    onPlayQueue: (List<SongData>, Int) -> Unit,
    onExitHeart: () -> Unit,
    onMessage: (String) -> Unit,
    onOpenRecommendSong: () -> Unit
) {
    val currentSong by playerEngine.currentSong.collectAsState()
    val scope = rememberCoroutineScope()
    val phase = heartPhase.value
    val commentViewModel = remember { CommentViewModel() }

    // 每次重组进入都尝试初始化，initHeart 内部幂等：已完成过则直接跳过
    LaunchedEffect(Unit) {
        initHeart(playerEngine, onPlayQueue)
    }

    // 歌词加载（PlayingScreen 渲染用）
    var lrcContent by remember { mutableStateOf("") }
    var lrcLabel by remember { mutableStateOf("歌词加载中…") }
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

    if (phase == PHASE_EMPTY) {
        BlackStateView(heartEmptyMessage, "去听歌", onOpenRecommendSong)
        return
    }
    if (phase == PHASE_FAIL) {
        BlackStateView(
            "心动模式加载失败" + if (heartFailMessage.isNotBlank()) "\n$heartFailMessage" else "，请稍后重试",
            "重试"
        ) {
            scope.launch {
                heartPhase.value = PHASE_INIT
                initHeart(playerEngine, onPlayQueue)
            }
        }
        return
    }

    // 播放页主体：PlayingScreen 的空态回调要区分「还在加载」与「真的没有歌」
    PlayingScreen(
        playerEngine = playerEngine,
        commentViewModel = commentViewModel,
        onClose = onExitHeart,
        isLiked = { songId -> songId in heartLikedIds.value },
        onToggleLike = { songId ->
            val liked = songId !in heartLikedIds.value
            heartLikedIds.value =
                if (liked) heartLikedIds.value + songId else heartLikedIds.value - songId
            scope.launch {
                runCatching { MineNet.likeSong(songId, liked) }
            }
        },
        onShare = { _, _ -> onMessage("分享功能开发中") },
        // ponytail: 心动页暂不提供更多菜单（音质/下载在完整播放页用），需要时把 PlayingPage 的菜单胶水抽公共
        onOpenMenu = { _, _ -> onMessage("更多操作请在播放页使用") },
        onDownload = {
            currentSong?.let { downloadSongAsync(it, onMessage) }
        },
        onMessage = onMessage,
        soundQuality = "standard",
        onSelectQuality = {},
        lrcContent = lrcContent,
        onUpdateLrc = {},
        lrcLabel = lrcLabel,
        onOpenFloor = {},
        onPlaylistEmpty = {
            if (heartPhase.value == PHASE_READY) {
                heartEmptyMessage = "还没有喜欢的歌曲，先去听听看吧"
                heartPhase.value = PHASE_EMPTY
            }
        }
    )
}

/**
 * 心动队列初始化（幂等）：likelist 拿喜欢 id -> 随机 50 首查详情 ->
 * 设随机模式并整组交给播放器。
 */
private suspend fun initHeart(
    playerEngine: PlayerEngine,
    onPlayQueue: (List<SongData>, Int) -> Unit
) {
    if (heartPhase.value != PHASE_INIT) return
    heartPhase.value = PHASE_LOADING
    runCatching {
        val likeIds = MineNet.getMyLikeSongList(0).ids.toList()
        if (likeIds.isEmpty()) {
            heartEmptyMessage = "还没有喜欢的歌曲，先去听听看吧"
            heartPhase.value = PHASE_EMPTY
            return
        }
        heartLikedIds.value = likeIds.toSet()
        val queue = PlayExtraNet.getSongDetail(likeIds.shuffled().take(50))
            .songs.filter { it.id > 0 }
        if (queue.isNotEmpty()) {
            playerEngine.setPlayMode(MODE_SHUFFLE)
            onPlayQueue(queue, 0)
            heartPhase.value = PHASE_READY
        } else {
            heartFailMessage = "喜欢列表歌曲详情获取失败"
            heartPhase.value = PHASE_FAIL
        }
    }.onFailure {
        heartFailMessage = it.message?.take(80) ?: it::class.simpleName ?: "网络异常"
        heartPhase.value = PHASE_FAIL
    }
}

/** 空态/失败态：黑底提示 + 操作按钮 */
@Composable
private fun BlackStateView(
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(top = 20.dp)
                .background(Red500, CircleShape)
                .clickable(onClick = onAction)
                .padding(horizontal = 28.dp, vertical = 10.dp)
        ) {
            Text(
                text = actionLabel,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** 区块标题，onMore 为 null 时隐藏「更多」入口 */
@Composable
internal fun HomeSectionHeader(
    title: String,
    onMore: (() -> Unit)?
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = AppThemeColor.TextH1,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        if (onMore != null) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(AppThemeColor.ThemeColor.copy(alpha = 0.1f), CircleShape)
                    .clickable(onClick = onMore)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "更多",
                    color = AppThemeColor.ThemeColor,
                    fontSize = 12.sp
                )
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "更多",
                    tint = AppThemeColor.ThemeColor,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

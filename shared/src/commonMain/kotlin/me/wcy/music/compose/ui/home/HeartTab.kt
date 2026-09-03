package me.wcy.music.compose.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.theme.Red500
import me.wcy.music.compose.ui.LyricsPanel
import me.wcy.music.compose.ui.VinylCover
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.net.MineNet
import me.wcy.music.shared.net.PlayExtraNet
import me.wcy.music.shared.player.PlayerEngine
import me.wcy.music.shared.player.fetchLyrics

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
/** 进程级歌词面板开关：切 tab 回来保持视图 */
private val heartShowLrc = mutableStateOf(false)

/**
 * 心动 tab：独立设计的心动模式界面（黑胶封面 + 歌词 + 精简控制条）。
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
    val isPlaying by playerEngine.isPlaying.collectAsState()
    val progress by playerEngine.playProgress.collectAsState()
    val playMode by playerEngine.playMode.collectAsState()
    val scope = rememberCoroutineScope()

    // 每次重组进入都尝试初始化，initHeart 内部幂等：已完成过则直接跳过
    LaunchedEffect(Unit) {
        initHeart(playerEngine, onPlayQueue)
    }

    val phase = heartPhase.value
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
    if (phase == PHASE_LOADING) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("正在准备心动模式…", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
        }
        return
    }

    // 歌词
    var lrcContent by remember { mutableStateOf("") }
    var lrcLabel by remember { mutableStateOf("暂无歌词") }
    LaunchedEffect(currentSong?.id) {
        val song = currentSong ?: return@LaunchedEffect
        lrcContent = ""
        lrcLabel = "歌词加载中…"
        lrcContent = fetchLyrics(song) ?: ""
        if (lrcContent.isBlank()) lrcLabel = "暂无歌词"
    }

    val song = currentSong
    val liked = song != null && song.id in heartLikedIds.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // 顶部标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("心动模式", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "随机播放喜欢的歌曲",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 11.sp
                    )
                }
            }

            // 中部：封面 ⇄ 歌词
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (heartShowLrc.value) {
                    LyricsPanel(
                        lrcContent = lrcContent,
                        progressMs = progress,
                        label = lrcLabel,
                        modifier = Modifier.fillMaxSize(),
                        onSeek = { playerEngine.seekTo(it) },
                        onEmptyTap = { heartShowLrc.value = false }
                    )
                } else if (song != null) {
                    VinylCover(
                        coverUrl = song.al.getLargeCover(),
                        isPlaying = isPlaying,
                        onClick = { heartShowLrc.value = true },
                        modifier = Modifier.size(280.dp)
                    )
                }
            }

            // 歌名 + 喜欢
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        song?.name ?: "准备中",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        song?.ar?.joinToString("/") { it.name } ?: "",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
                Icon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "喜欢",
                    tint = if (liked) Red500 else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(26.dp)
                        .clickable {
                            if (song == null) return@clickable
                            val target = !liked
                            heartLikedIds.value =
                                if (target) heartLikedIds.value + song.id
                                else heartLikedIds.value - song.id
                            scope.launch { runCatching { MineNet.likeSong(song.id, target) } }
                        }
                )
            }

            // 进度条 + 时间
            Column(modifier = Modifier.padding(top = 4.dp)) {
                var dragValue by remember(song?.id) { mutableStateOf<Float?>(null) }
                val totalMs = (song?.dt ?: 0L).coerceAtLeast(1L)
                Slider(
                    value = (dragValue ?: (progress.toFloat() / totalMs)).coerceIn(0f, 1f),
                    onValueChange = { dragValue = it },
                    onValueChangeFinished = {
                        dragValue?.let { playerEngine.seekTo((it * totalMs).toInt()) }
                        dragValue = null
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatMs(dragValue?.let { (it * totalMs).toLong() } ?: progress),
                        color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
                    Text(formatMs(totalMs), color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
                }
            }

            // 控制条
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (playMode) {
                        1 -> Icons.Filled.Shuffle
                        2 -> Icons.Filled.RepeatOne
                        else -> Icons.Filled.Repeat
                    },
                    contentDescription = "播放模式",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { playerEngine.setPlayMode((playMode + 1) % 3) }
                )
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "上一首",
                    tint = Color.White,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { playerEngine.prev() }
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White, CircleShape)
                        .clickable { playerEngine.playPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "播放/暂停",
                        tint = Color.Black,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "下一首",
                    tint = Color.White,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { playerEngine.next() }
                )
                Box(modifier = Modifier.size(22.dp))
            }
        }
    }
}

private fun formatMs(milli: Long): String {
    val m = (milli / 60000).toString().padStart(2, '0')
    val s = (milli / 1000 % 60).toString().padStart(2, '0')
    return "$m:$s"
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
        Box(
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
    Row(
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
            Row(
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
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "更多",
                    tint = AppThemeColor.ThemeColor,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

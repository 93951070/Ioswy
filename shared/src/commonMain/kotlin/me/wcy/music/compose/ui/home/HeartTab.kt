package me.wcy.music.compose.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.theme.Red500
import me.wcy.music.shared.net.AccountNet
import me.wcy.music.shared.net.MineNet
import me.wcy.music.shared.net.PlayExtraNet
import me.wcy.music.shared.player.PlayerEngine

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

/**
 * 心动 tab：直接是播放器页面（仿网易云心动模式）。
 * 首次进入拉喜欢列表 -> 随机种子 -> 心动模式推荐队列交给全局 PlayerEngine 播放。
 * 实测：likelist/intelligence 均需登录 cookie；playmode/intelligence/list 的 pid
 * 必须传「喜欢的音乐」歌单 id（user/playlist 中 specialType=5 的歌单，与 uid 不同），
 * 传 uid 返回 400「不支持该歌单类型」。
 */
@Composable
fun HeartTab(
    playerEngine: PlayerEngine,
    onPlayQueue: (List<SongData>, Int) -> Unit,
    onOpenPlaying: () -> Unit,
    onOpenRecommendSong: () -> Unit
) {
    val currentSong by playerEngine.currentSong.collectAsState()
    val isPlaying by playerEngine.isPlaying.collectAsState()
    val playProgress by playerEngine.playProgress.collectAsState()
    val playMode by playerEngine.playMode.collectAsState()
    val scope = rememberCoroutineScope()
    val phase = heartPhase.value

    // 每次重组进入都尝试初始化，initHeart 内部幂等：已完成过则直接跳过
    LaunchedEffect(Unit) {
        initHeart(playerEngine, onPlayQueue)
    }

    val song = currentSong
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            phase == PHASE_FAIL -> HeartStateView(
                "心动模式加载失败" + if (heartFailMessage.isNotBlank()) "\n$heartFailMessage" else "，请稍后重试",
                "重试"
            ) {
                scope.launch {
                    heartPhase.value = PHASE_INIT
                    initHeart(playerEngine, onPlayQueue)
                }
            }
            phase == PHASE_EMPTY -> HeartStateView(heartEmptyMessage, "去听歌", onOpenRecommendSong)
            phase == PHASE_READY && song == null ->
                HeartStateView("播放队列为空", "重新开启心动模式") {
                    scope.launch {
                        heartPhase.value = PHASE_INIT
                        initHeart(playerEngine, onPlayQueue)
                    }
                }
            song == null -> HeartLoadingView()
            else -> HeartPlayerContent(
                song = song,
                playerEngine = playerEngine,
                isPlaying = isPlaying,
                playProgress = playProgress,
                playMode = playMode,
                onOpenPlaying = onOpenPlaying
            )
        }
    }
}

/**
 * 心动队列初始化（幂等）：login/status 拿 uid -> likelist 拿喜欢 id -> 随机种子 ->
 * intelligence 拿推荐队列 -> 设随机模式并整组交给播放器。
 */
private suspend fun initHeart(
    playerEngine: PlayerEngine,
    onPlayQueue: (List<SongData>, Int) -> Unit
) {
    if (heartPhase.value != PHASE_INIT) return
    heartPhase.value = PHASE_LOADING
    runCatching {
        val uid = AccountNet.getLoginStatus().data.account.id
        if (uid <= 0) {
            heartEmptyMessage = "登录后即可开启心动模式"
            heartPhase.value = PHASE_EMPTY
            return
        }
        val likeIds = MineNet.getMyLikeSongList(uid).ids.toList()
        if (likeIds.isEmpty()) {
            heartEmptyMessage = "还没有喜欢的歌曲，先去听听看吧"
            heartPhase.value = PHASE_EMPTY
            return
        }
        val likePid = MineNet.getUserPlaylist(uid).playlists
            .firstOrNull { it.specialType == 5 }?.id ?: uid
        val seed = likeIds.random()
        val res = PlayExtraNet.getIntelligenceList(seed, pid = likePid, sid = seed)
        val queue = res.data.orEmpty().mapNotNull { it.songInfo }.distinctBy { it.id }
        if (res.code == 200 && queue.isNotEmpty()) {
            playerEngine.setPlayMode(MODE_SHUFFLE)
            onPlayQueue(queue, 0)
            heartPhase.value = PHASE_READY
        } else {
            heartFailMessage = "接口返回 code=${res.code}, 队列=${queue.size}首"
            heartPhase.value = PHASE_FAIL
        }
    }.onFailure {
        heartFailMessage = it.message?.take(80) ?: it::class.simpleName ?: "网络异常"
        heartPhase.value = PHASE_FAIL
    }
}

/** 播放器主体：模糊封面背景 + 大封面圆角卡 + 歌曲信息 + 红色进度条 + 控制按钮行 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeartPlayerContent(
    song: SongData,
    playerEngine: PlayerEngine,
    isPlaying: Boolean,
    playProgress: Long,
    playMode: Int,
    onOpenPlaying: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var liked by remember { mutableStateOf(false) }

    // 心形初始态来自真实喜欢检查，避免本进程外已喜欢的歌曲显示错误
    LaunchedEffect(song.id) {
        liked = runCatching { PlayExtraNet.checkSongLike(listOf(song.id)) }
            .getOrNull()?.takeIf { it.code == 200 }
            ?.let { song.id in it.ids } ?: false
    }

    val coverUrl = song.al.getLargeCover()
    Box(modifier = Modifier.fillMaxSize()) {
        if (coverUrl.isNotBlank()) {
            CoverImage(
                url = coverUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(50.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "心动模式",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CoverImage(
                    url = coverUrl,
                    contentDescription = song.name,
                    cornerRadius = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .aspectRatio(1f)
                        .clickable(onClick = onOpenPlaying)
                )
            }
            Text(
                text = song.name,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.ar.joinToString("/") { it.name },
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            HeartSlider(
                playProgress = playProgress,
                durationMs = song.dt,
                onSeek = { playerEngine.seekTo(it) },
                modifier = Modifier.padding(top = 16.dp)
            )
            HeartControlsRow(
                isPlaying = isPlaying,
                playMode = playMode,
                liked = liked,
                onToggleMode = { playerEngine.setPlayMode(nextMode(playMode)) },
                onPlayPause = { playerEngine.playPause() },
                onPrev = { playerEngine.prev() },
                onNext = { playerEngine.next() },
                onToggleLike = {
                    liked = !liked
                    scope.launch {
                        runCatching { MineNet.likeSong(song.id, liked) }
                    }
                },
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
        }
    }
}

/** 红色进度条：拖动中暂停刷新，松手 seek */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeartSlider(
    playProgress: Long,
    durationMs: Long,
    onSeek: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val duration = durationMs.toFloat().coerceAtLeast(1f)
    val progress = playProgress.toFloat().coerceIn(0f, duration)
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val sliderValue = dragValue ?: progress

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = sliderValue,
            onValueChange = { dragValue = it },
            onValueChangeFinished = {
                dragValue?.let { onSeek(it.toInt()) }
                dragValue = null
            },
            valueRange = 0f..duration,
            thumb = {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            },
            track = { state ->
                val range = state.valueRange
                val fraction =
                    ((state.value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .background(Red500)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(sliderValue.toLong()),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = formatTime(duration.toLong()),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

/** 控制按钮行：播放模式 / 上一首 / 播放暂停 / 下一首 / 喜欢 */
@Composable
private fun HeartControlsRow(
    isPlaying: Boolean,
    playMode: Int,
    liked: Boolean,
    onToggleMode: () -> Unit,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToggleLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Icon(
            imageVector = modeIcon(playMode),
            contentDescription = "播放模式",
            tint = Color.White,
            modifier = Modifier
                .size(26.dp)
                .clickable(onClick = onToggleMode)
        )
        Icon(
            imageVector = Icons.Filled.SkipPrevious,
            contentDescription = "上一首",
            tint = Color.White,
            modifier = Modifier
                .size(34.dp)
                .clickable(onClick = onPrev)
        )
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "播放",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Icon(
            imageVector = Icons.Filled.SkipNext,
            contentDescription = "下一首",
            tint = Color.White,
            modifier = Modifier
                .size(34.dp)
                .clickable(onClick = onNext)
        )
        Icon(
            imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "喜欢",
            tint = if (liked) Red500 else Color.White,
            modifier = Modifier
                .size(26.dp)
                .clickable(onClick = onToggleLike)
        )
    }
}

/** 加载中 */
@Composable
private fun HeartLoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

/** 空态/失败态：提示 + 操作按钮 */
@Composable
private fun HeartStateView(
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                .clip(CircleShape)
                .background(Red500)
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

private fun modeIcon(mode: Int): androidx.compose.ui.graphics.vector.ImageVector {
    return when (mode) {
        1 -> Icons.Filled.Shuffle
        2 -> Icons.Filled.RepeatOne
        else -> Icons.Filled.Repeat
    }
}

private fun nextMode(mode: Int): Int {
    return when (mode) {
        1 -> 2
        2 -> 0
        else -> 1
    }
}

private fun formatTime(milli: Long): String {
    val m = (milli / 60000).toString().padStart(2, '0')
    val s = (milli / 1000 % 60).toString().padStart(2, '0')
    return "$m:$s"
}

/** 区块标题，onMore 为 null 时隐藏「更多」入口 */
@Composable
internal fun HomeSectionHeader(
    title: String,
    onMore: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppThemeColor.ThemeColor.copy(alpha = 0.1f))
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
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

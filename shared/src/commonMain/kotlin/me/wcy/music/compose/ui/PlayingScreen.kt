package me.wcy.music.compose.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.theme.Red500
import me.wcy.music.discover.comment.viewmodel.CommentViewModel
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.player.PlayerEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayingScreen(


    playerEngine: PlayerEngine,
    commentViewModel: CommentViewModel,
    onClose: () -> Unit,
    isLiked: (Long) -> Boolean,
    onToggleLike: (Long) -> Unit,
    onShare: (SongData, Long) -> Unit,
    onOpenMenu: (SongData, Long) -> Unit,
    onDownload: () -> Unit,
    onMessage: (String) -> Unit,
    soundQuality: String,
    onSelectQuality: (String) -> Unit,
    lrcContent: String,
    onUpdateLrc: (Long) -> Unit,
    lrcLabel: String,
    onOpenFloor: (parentCommentId: Long) -> Unit = {},
    onPlaylistEmpty: () -> Unit = {},
) {
    val currentSong by playerEngine.currentSong.collectAsState()
    val isPlaying by playerEngine.isPlaying.collectAsState()
    val playProgress by playerEngine.playProgress.collectAsState()
    val buffering by playerEngine.bufferingPercent.collectAsState()
    val playMode by playerEngine.playMode.collectAsState()

    // 播放列表被清空时退出播放页，避免整页空白
    LaunchedEffect(currentSong) {
        if (currentSong == null) onPlaylistEmpty()
    }
    val song = currentSong ?: return
    val songId = song.id
    val coverUrl = song.al.getLargeCover()
    val artist = song.ar.joinToString("/") { it.name }
    // ponytail: PlayState.Preparing 近似为「未播放且在缓冲」，极早期缓冲(0%)会显示为暂停态，升级需引擎暴露 preparing 状态流
    val isPreparing = !isPlaying && buffering > 0

    var showCover by remember { mutableStateOf(true) }
    var showPlaylistSheet by remember { mutableStateOf(false) }
    var showCommentSheet by remember { mutableStateOf(false) }
    var commentSongId by remember { mutableStateOf(0L) }
    var commentCount by remember { mutableStateOf(0L) }

    LaunchedEffect(songId) {
        commentCount = 0
        if (songId > 0) {
            runCatching { DiscoverNet.getCommentMusic(songId, 1, 0) }
                .getOrNull()?.takeIf { it.code == 200 }?.let { commentCount = it.total }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
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
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    contentDescription = "关闭",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onClose)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = song.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        maxLines = 1
                    )
                    Text(
                        text = artist,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "分享",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(start = 4.dp)
                        .clickable { onShare(song, songId) }
                )
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "更多",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(start = 4.dp)
                        .clickable { onOpenMenu(song, songId) }
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (showCover) {
                    VinylCover(
                        coverUrl = coverUrl,
                        isPlaying = isPlaying,
                        onClick = { showCover = false },
                        modifier = Modifier.size(maxWidth - 100.dp)
                    )
                } else {
                    LyricsPanel(
                        lrcContent = lrcContent,
                        progressMs = playProgress,
                        label = lrcLabel,
                        onSeek = { playerEngine.seekTo(it) },
                        onEmptyTap = { showCover = true },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp)
                    )
                }
                Text(
                    text = "词",
                    color = if (showCover) Color.White.copy(alpha = 0.7f) else Red500,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clickable { showCover = !showCover }
                )
            }

            ProgressAndControls(
                song = song,
                isPlaying = isPlaying,
                isPreparing = isPreparing,
                playProgress = playProgress,
                playMode = playMode,
                isLiked = isLiked(songId),
                onSeek = { playerEngine.seekTo(it) },
                onPlayPause = { playerEngine.playPause() },
                onPrev = { playerEngine.prev() },
                onNext = { playerEngine.next() },
                onToggleMode = { playerEngine.setPlayMode(nextMode(playMode)) },
                onToggleLike = { onToggleLike(songId) },
                onOpenComment = {
                    commentSongId = songId
                    if (commentSongId > 0) showCommentSheet = true
                },
                onOpenMenu = { onOpenMenu(song, songId) },
                onOpenPlaylist = { showPlaylistSheet = true },
                onDownload = onDownload,
                commentCount = commentCount
            )
        }
    }

    if (showPlaylistSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlaylistSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            PlaylistSheet(playerEngine)
        }
    }

    if (showCommentSheet) {
        LaunchedEffect(commentSongId) {
            commentViewModel.init(commentSongId)
            commentViewModel.loadMore()
        }
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            CommentPanel(commentViewModel, onMessage, onOpenFloor = onOpenFloor)
        }
    }

}

/**
 * 音质选择弹层（播放页「更多」菜单里触发）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualitySheet(
    currentQuality: String,
    onSelectQuality: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            QUALITY_LEVELS.forEach { (level, label) ->
                val checked = level == currentQuality
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            if (!checked) onSelectQuality(level)
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        color = if (checked) Red500 else AppThemeColor.TextH1,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (checked) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "已选",
                            tint = Red500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistSheet(playerEngine: PlayerEngine) {
    val playlist by playerEngine.playlist.collectAsState()
    val currentSong by playerEngine.currentSong.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 460.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "当前播放 (${playlist.size})",
                color = AppThemeColor.TextH1,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.DeleteSweep,
                contentDescription = "清空",
                tint = AppThemeColor.TextH2,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { playerEngine.clearPlaylist() }
            )
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(playlist, key = { _, song -> song.id }) { index, song ->
                val playing = song.id == currentSong?.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { playerEngine.playAt(index) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (playing) {
                        Icon(
                            imageVector = Icons.Filled.GraphicEq,
                            contentDescription = "正在播放",
                            tint = AppThemeColor.ThemeColor,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = song.name,
                        color = if (playing) AppThemeColor.ThemeColor else AppThemeColor.TextH1,
                        fontSize = 14.sp
                    )
                    Text(
                        text = " - ${song.ar.joinToString("/") { it.name }}",
                        color = AppThemeColor.TextH2,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "删除",
                        tint = AppThemeColor.TextH2,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { playerEngine.delete(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VinylCover(
    coverUrl: String,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 15s 一圈；graphicsLayer 内读 state，暂停时冻结当前角度，只重绘不重组
    val rotation = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var last = 0L
            while (isActive) {
                withFrameNanos { now ->
                    if (last != 0L) {
                        rotation.floatValue =
                            (rotation.floatValue + (now - last) * 0.000000024f) % 360f
                    }
                    last = now
                }
            }
        }
    }
    // 唱针：播放时正角搭在唱片上，暂停时负角抬起
    val needleAngle by animateFloatAsState(
        targetValue = if (isPlaying) 8f else -32f,
        animationSpec = tween(300),
        label = "needle"
    )
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation.floatValue },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4A4A4A),
                            Color(0xFF2A2A2A),
                            Color(0xFF050505),
                            Color(0xFF2A2A2A),
                            Color(0xFF0A0A0A)
                        ),
                        center = center,
                        radius = size.minDimension / 2f
                    )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize(0.68f)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                CoverImage(
                    url = coverUrl,
                    contentDescription = "封面",
                    cornerRadius = 999.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            )
        }
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-14).dp)
                .size(96.dp, 130.dp)
        ) {
            val pivot = Offset(size.width * 0.42f, 18.dp.toPx())
            rotate(needleAngle, pivot) {
                drawLine(
                    color = Color(0xFF616161),
                    start = pivot,
                    end = Offset(pivot.x, pivot.y + size.height * 0.62f),
                    strokeWidth = 9f
                )
                drawLine(
                    color = Color(0xFFBDBDBD),
                    start = Offset(pivot.x, pivot.y + size.height * 0.62f),
                    end = Offset(pivot.x + 14f, pivot.y + size.height * 0.72f),
                    strokeWidth = 6f
                )
            }
            drawCircle(Color(0xFF8D8D8D), radius = 15f, center = pivot)
            drawCircle(Color(0xFF424242), radius = 7f, center = pivot)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressAndControls(
    song: SongData,
    isPlaying: Boolean,
    isPreparing: Boolean,
    playProgress: Long,
    playMode: Int,
    isLiked: Boolean,
    onSeek: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToggleMode: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenComment: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenPlaylist: () -> Unit,
    onDownload: () -> Unit,
    commentCount: Long
) {
    val duration = song.dt.toFloat().coerceAtLeast(1f)
    val progress = playProgress.toFloat().coerceIn(0f, duration)
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val sliderValue = dragValue ?: progress

    Column(modifier = Modifier.fillMaxWidth()) {
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
                            .background(Color.White)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onToggleMode) {
                Icon(
                    imageVector = modeIcon(playMode),
                    contentDescription = "模式",
                    tint = Color.White
                )
            }
            IconButton(onClick = onPrev) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "上一首",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center
            ) {
                if (isPreparing) {
                    Text(text = "...", color = Color.White)
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "播放",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "下一首",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = onOpenPlaylist) {
                Icon(
                    imageVector = Icons.Filled.List,
                    contentDescription = "列表",
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "喜欢",
                tint = if (isLiked) Red500 else Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onToggleLike)
            )
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = "下载",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onDownload)
            )
            BadgedBox(
                badge = {
                    if (commentCount > 0) {
                        Badge(containerColor = Color(0xFFEC4141)) {
                            Text(
                                text = formatCommentCount(commentCount),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Comment,
                    contentDescription = "评论",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onOpenComment)
                )
            }
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "更多",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onOpenMenu)
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

private val QUALITY_LEVELS = listOf(
    "standard" to "标准",
    "higher" to "较高",
    "exhigh" to "极高",
    "lossless" to "无损",
    "hires" to "Hi-Res"
)

fun qualityLabel(level: String): String =
    QUALITY_LEVELS.firstOrNull { it.first == level }?.second ?: "标准"

private fun formatCommentCount(count: Long): String {
    return if (count >= 1000) "999+" else count.toString()
}

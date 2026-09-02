package me.wcy.music.mv.detail

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.ui.CommentPanel
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.comment.viewmodel.CommentViewModel
import me.wcy.music.mv.MvNet
import me.wcy.music.mv.bean.MvItem
import me.wcy.music.mv.detail.viewmodel.MvDetailViewModel
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.util.formatPlayCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MvDetailScreen(

    viewModel: MvDetailViewModel,
    mvid: Long,
    onBack: () -> Unit,
    onMessage: (String) -> Unit = {},
    onOpenFloor: (parentCommentId: Long) -> Unit = {},
    onOpenMv: (Long) -> Unit = {},
) {
    LaunchedEffect(mvid) {
        viewModel.init(mvid)
        viewModel.loadData()
    }

    val mv by viewModel.mv.collectAsState()
    val mvUrl by viewModel.mvUrl.collectAsState()
    val isSub by viewModel.isSub.collectAsState()
    val scope = rememberCoroutineScope()
    var showCommentSheet by remember { mutableStateOf(false) }
    val commentViewModel = remember { CommentViewModel() }
    var related by remember(mvid) { mutableStateOf<List<RelatedMv>>(emptyList()) }

    LaunchedEffect(mvid) {
        related = loadRelatedMv(mvid)
    }

    // 弹幕池来自评论区；开关状态放顶层，全屏布局切换时不重置
    var danmaku by remember(mvid) { mutableStateOf<List<String>>(emptyList()) }
    var danmakuOn by remember { mutableStateOf(true) }

    LaunchedEffect(mvid) {
        danmaku = loadDanmaku(mvid)
    }

    var isFullscreen by remember { mutableStateOf(false) }

    if (isFullscreen) {
        // 全屏：页面内布局切换，只渲染播放器 + 左上角退出按钮，其余内容不渲染
        // ponytail: 全屏切换会重建播放器（进度从头播）；要保进度需把 player 提升为跨布局共享状态
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            MvPlayerArea(
                url = mvUrl,
                isFullscreen = true,
                onToggleFullscreen = { isFullscreen = false },
                danmaku = danmaku,
                danmakuOn = danmakuOn,
                onToggleDanmaku = { danmakuOn = !danmakuOn },
                modifier = Modifier.fillMaxSize()
            )
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "退出全屏",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(28.dp)
                    .clickable { isFullscreen = false }
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                TitleBar(title = mv?.name ?: "MV", onBack = onBack)
            }
            item {
                // 有地址直接内嵌自动播放，无地址显示封面
                if (mvUrl.isNotBlank()) {
                    MvPlayerArea(
                        url = mvUrl,
                        isFullscreen = false,
                        onToggleFullscreen = { isFullscreen = true },
                        danmaku = danmaku,
                        danmakuOn = danmakuOn,
                        onToggleDanmaku = { danmakuOn = !danmakuOn },
                        modifier = Modifier.fillMaxWidth().height(210.dp)
                    )
                } else {
                    MvPlayerCover(cover = mv?.cover ?: "")
                }
            }
            mv?.let { data ->
                item {
                    MvInfo(
                        mv = data,
                        isSub = isSub,
                        onCollect = { scope.launch { viewModel.collect() } },
                        onOpenComment = { showCommentSheet = true }
                    )
                }
            }
            if (related.isNotEmpty()) {
                item {
                    Text(
                        text = "相关推荐",
                        color = AppThemeColor.TextH1,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                    )
                }
                item {
                    // 2 行 x 3 列网格 x 2 组，共 12 个 MV
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        related.chunked(3).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowItems.forEach { data ->
                                    RelatedMvCard(
                                        item = data,
                                        onClick = { onOpenMv(data.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCommentSheet) {
        LaunchedEffect(mvid) {
            // MV 评论走 comment/mv
            commentViewModel.init(mvid, source = "mv")
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

@Composable
private fun MvPlayerCover(cover: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        CoverImage(
            url = cover,
            cornerRadius = 0.dp,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MvInfo(
    mv: MvItem,
    isSub: Boolean,
    onCollect: () -> Unit,
    onOpenComment: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = mv.name,
            color = AppThemeColor.TextH1,
            fontSize = 16.sp
        )
        Row(
            modifier = Modifier.padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mv.artistName,
                color = AppThemeColor.TextH2,
                fontSize = 13.sp
            )
            Text(
                text = buildList {
                    mv.publishTime.takeIf { it.isNotBlank() }?.let { add(it) }
                    if (mv.playCount > 0) add("${formatPlayCount(mv.playCount)}次播放")
                }.joinToString(" · "),
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
        Row(
            modifier = Modifier.padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(AppThemeColor.ThemeColor, RoundedCornerShape(12.dp))
                    .clickable(onClick = onCollect)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isSub) "已收藏" else "收藏",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .background(AppThemeColor.ThemeColor, RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenComment)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "评论",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
        val desc = mv.desc.ifBlank { mv.briefDesc }
        if (desc.isNotBlank()) {
            Text(
                text = desc,
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

/** GET /comment/mv 返回 hotComments + comments，弹幕池取第一页 20 条评论内容 */
private suspend fun loadDanmaku(mvid: Long): List<String> {
    return runCatching {
        DiscoverNet.getCommentMv(mvid, limit = 20)
    }.getOrNull()?.let { data ->
        (data.hotComments + data.comments)
            .map { it.content.trim() }
            .filter { it.isNotBlank() }
    }.orEmpty()
}

/** 相关推荐条目：personalized/mv（picUrl）与 mv/first（cover）的统一展示模型 */
private data class RelatedMv(
    val id: Long,
    val name: String,
    val cover: String,
    val playCount: Long
)

/**
 * 相关推荐：personalized/mv（编辑推荐，实测仅 2 条）+ mv/first(limit=20) 兜底，
 * 按 id 去重、排除当前 MV，取前 12 条铺 2 行 x 3 列网格 x 2 组
 */
private suspend fun loadRelatedMv(mvid: Long): List<RelatedMv> {
    val personalized = runCatching {
        MvNet.getPersonalizedMv().takeIf { it.code == 200 }?.result.orEmpty()
    }.getOrDefault(emptyList())
        .map { RelatedMv(it.id, it.name, it.picUrl, it.playCount) }
    val first = runCatching {
        MvNet.getMvFirst(limit = 20).data
    }.getOrDefault(emptyList())
        .map { RelatedMv(it.id, it.name, it.cover, it.playCount) }
    return (personalized + first)
        .filter { it.id > 0 && it.id != mvid && it.cover.isNotBlank() }
        .distinctBy { it.id }
        .take(12)
}

/**
 * 播放器容器：视频层 + 弹幕层（顶部 30% 轨道）+「弹」开关按钮（全屏/非全屏共用）。
 * 弹幕层与开关按钮均为纯绘制/自身点击，其余手势穿透到播放器。
 */
@Composable
private fun MvPlayerArea(
    url: String,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    danmaku: List<String>,
    danmakuOn: Boolean,
    onToggleDanmaku: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(Color.Black)) {
        MvPlayerSurface(
            url = url,
            isFullscreen = isFullscreen,
            onToggleFullscreen = onToggleFullscreen,
            modifier = Modifier.fillMaxSize()
        )
        if (danmakuOn && danmaku.isNotEmpty()) {
            DanmakuOverlay(
                danmaku = danmaku,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 44.dp)
                .size(26.dp)
                .background(
                    if (danmakuOn) Color(0xFFEC4141) else Color.Black.copy(alpha = 0.35f),
                    CircleShape
                )
                .clickable(onClick = onToggleDanmaku),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "弹", color = Color.White, fontSize = 11.sp)
        }
    }
}

/** 顶部弹幕区：2-3 行轨道，池子不足时行数随之减少 */
@Composable
private fun DanmakuOverlay(danmaku: List<String>, modifier: Modifier = Modifier) {
    val rows = danmaku.chunked(7).take(3)
    Box(modifier = modifier.clipToBounds()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            rows.forEachIndexed { index, row ->
                DanmakuRow(
                    text = row.map { it.take(24) }.joinToString("　　　　"),
                    durationMillis = 16000 + index * 2000,
                    startOffsetMillis = index * 6000
                )
            }
        }
    }
}

/** 单条弹幕轨道：infiniteTransition 驱动文本从右侧匀速平移到左侧循环，各行起始 offset 错开 */
@Composable
private fun DanmakuRow(text: String, durationMillis: Int, startOffsetMillis: Int) {
    val transition = rememberInfiniteTransition(label = "danmaku")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            initialStartOffset = StartOffset(startOffsetMillis)
        ),
        label = "danmaku-progress"
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val travel = maxWidth
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            maxLines = 1,
            style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), blurRadius = 2f)),
            modifier = Modifier
                .offset(x = travel)
                .graphicsLayer { translationX = -(travel.toPx() + size.width) * progress }
        )
    }
}

@Composable
/** 相关推荐网格卡片：16:9 封面 + 标题 2 行截断 + 播放量，宽度由网格 weight 决定 */
private fun RelatedMvCard(item: RelatedMv, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        CoverImage(
            url = item.cover,
            cornerRadius = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )
        Text(
            text = item.name,
            color = AppThemeColor.TextH1,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = "${formatPlayCount(item.playCount)}次播放",
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

package me.wcy.music.mv.detail

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.component.DanmakuBar
import me.wcy.music.compose.component.DanmakuOverlay
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

    // 发送弹幕 = 发评论（comment 接口 type=1，与评论面板一致），成功乐观插到弹幕池头部
    val onSendDanmaku: (String) -> Unit = { content ->
        scope.launch {
            val ok = runCatching {
                DiscoverNet.addComment(mvid.toString(), type = 1, content = content).code == 200
            }.getOrDefault(false)
            if (ok) {
                onMessage("弹幕发送成功")
                danmaku = listOf(content) + danmaku
            } else {
                onMessage("发送失败")
            }
        }
    }

    var isFullscreen by remember { mutableStateOf(false) }

    // 控制层（返回键+弹幕条）显隐：点画面切换，播放中 3.5s 自动隐藏，暂停/输入聚焦保持显示
    var controlsVisible by remember(isFullscreen) { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var danmakuInputFocused by remember { mutableStateOf(false) }
    LaunchedEffect(controlsVisible, isPlaying, danmakuInputFocused) {
        if (controlsVisible && isPlaying && !danmakuInputFocused) {
            delay(3500)
            controlsVisible = false
        }
    }
    val onToggleControls: () -> Unit = { controlsVisible = !controlsVisible }

    if (isFullscreen) {
        // 全屏：页面内布局切换，只渲染播放器 + 左上角退出按钮 + 底部弹幕条，其余内容不渲染
        // ponytail: 全屏切换会重建播放器（进度从头播）；要保进度需把 player 提升为跨布局共享状态
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            MvPlayerArea(
                url = mvUrl,
                isFullscreen = true,
                onToggleFullscreen = { isFullscreen = false },
                danmaku = danmaku,
                danmakuOn = danmakuOn,
                onTap = onToggleControls,
                onPlayingChange = { isPlaying = it },
                modifier = Modifier.fillMaxSize()
            )
            if (controlsVisible) {
                IconButton(
                    onClick = { isFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "退出全屏",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                DanmakuBar(
                    danmakuOn = danmakuOn,
                    onToggle = { danmakuOn = !danmakuOn },
                    onSend = onSendDanmaku,
                    onInputFocusChanged = { danmakuInputFocused = it },
                    dark = true,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .navigationBarsPadding()
                )
            }
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
                        onTap = onToggleControls,
                        onPlayingChange = { isPlaying = it },
                        modifier = Modifier.fillMaxWidth().height(210.dp)
                    )
                    if (controlsVisible) {
                        DanmakuBar(
                            danmakuOn = danmakuOn,
                            onToggle = { danmakuOn = !danmakuOn },
                            onSend = onSendDanmaku,
                            onInputFocusChanged = { danmakuInputFocused = it },
                            dark = false
                        )
                    }
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
 * 播放器容器：视频层 + 弹幕层（顶部 30% 轨道）。
 * 弹幕层为纯绘制，手势穿透到播放器；弹幕开关/输入在播放器外的 DanmakuBar。
 */
@Composable
private fun MvPlayerArea(
    url: String,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    danmaku: List<String>,
    danmakuOn: Boolean,
    onTap: () -> Unit,
    onPlayingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(Color.Black)) {
        MvPlayerSurface(
            url = url,
            isFullscreen = isFullscreen,
            onToggleFullscreen = onToggleFullscreen,
            onTap = onTap,
            onPlayingChange = onPlayingChange,
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

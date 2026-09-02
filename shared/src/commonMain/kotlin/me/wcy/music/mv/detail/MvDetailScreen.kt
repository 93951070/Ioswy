package me.wcy.music.mv.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.ui.CommentPanel
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.comment.viewmodel.CommentViewModel
import me.wcy.music.mv.MvNet
import me.wcy.music.mv.bean.MvItem
import me.wcy.music.mv.detail.viewmodel.MvDetailViewModel
import me.wcy.music.shared.net.MvVideoExtraNet
import me.wcy.music.shared.net.SharedNet
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

    var isFullscreen by remember { mutableStateOf(false) }

    if (isFullscreen) {
        // 全屏：页面内布局切换，只渲染播放器 + 左上角退出按钮，其余内容不渲染
        // ponytail: 全屏切换会重建播放器（进度从头播）；要保进度需把 player 提升为跨布局共享状态
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            MvPlayerSurface(
                url = mvUrl,
                isFullscreen = true,
                onToggleFullscreen = { isFullscreen = false },
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
                    MvPlayerSurface(
                        url = mvUrl,
                        isFullscreen = false,
                        onToggleFullscreen = { isFullscreen = true },
                        modifier = Modifier.fillMaxWidth().height(210.dp).background(Color.Black)
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
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(related) { data ->
                            RelatedMvCard(item = data, onClick = { onOpenMv(data.id) })
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

/** GET /simi/mv 返回 {code, mvdata: List<MvItem>}（MvNet 无封装，本地接口实测键名为 mvdata） */
@Serializable
private data class MvSimiData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("mvdata")
    val mvdata: List<MvItem> = listOf()
)

/** 相关推荐条目：融合 simi/mv 与 related/allvideo 的统一展示模型 */
private data class RelatedMv(
    val id: Long,
    val name: String,
    val cover: String,
    val playCount: Long
)

/**
 * 相关推荐：首选 simi/mv（MV 专用）；为空时降级 personalized/mv（官方 MV 推荐流）；
 * 再为空时降级 related/allvideo（视频接口，vid 为纯数字的条目才是 MV）
 */
private suspend fun loadRelatedMv(mvid: Long): List<RelatedMv> {
    runCatching {
        SharedJson.decodeBean<MvSimiData>(
            SharedNet.get("simi/mv", params = listOf("mvid" to mvid))
        )
    }.getOrNull()?.mvdata?.takeIf { it.isNotEmpty() }?.let { list ->
        return list
            .filter { it.id != mvid }
            .map { RelatedMv(it.id, it.name, it.cover, it.playCount) }
    }
    runCatching {
        MvNet.getPersonalizedMv().takeIf { it.code == 200 }?.result
    }.getOrNull()?.takeIf { it.isNotEmpty() }?.let { list ->
        return list
            .filter { it.id != mvid }
            .map { RelatedMv(it.id, it.name, it.picUrl, it.playCount) }
    }
    return runCatching {
        MvVideoExtraNet.getRelatedVideos(mvid).takeIf { it.isSuccessWithData() }?.data?.data
    }.getOrNull().orEmpty()
        .mapNotNull { video ->
            val mvId = video.vid.toLongOrNull() ?: return@mapNotNull null
            RelatedMv(mvId, video.title, video.coverUrl, video.playTime)
        }
}

@Composable
/** 相关推荐横向卡片：16:9 封面 + 标题 + 播放量 */
private fun RelatedMvCard(item: RelatedMv, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
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

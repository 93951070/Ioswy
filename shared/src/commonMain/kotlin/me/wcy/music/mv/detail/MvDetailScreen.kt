package me.wcy.music.mv.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.ui.CommentPanel
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.comment.viewmodel.CommentViewModel
import me.wcy.music.mv.bean.MvItem
import me.wcy.music.mv.detail.viewmodel.MvDetailViewModel
import me.wcy.music.shared.util.formatPlayCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MvDetailScreen(
    viewModel: MvDetailViewModel,
    mvid: Long,
    onBack: () -> Unit,
    onMessage: (String) -> Unit = {}
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

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TitleBar(title = mv?.name ?: "MV", onBack = onBack)
        }
        item {
            // 有地址直接内嵌自动播放，无地址显示封面
            if (mvUrl.isNotBlank()) {
                MvPlayerSurface(
                    url = mvUrl,
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
            CommentPanel(commentViewModel, onMessage)
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
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .background(AppThemeColor.ThemeColor, RoundedCornerShape(12.dp))
                    .clickable(onClick = onCollect)
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isSub) "已收藏" else "收藏",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(AppThemeColor.ThemeColor, RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenComment)
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "评论",
                    color = Color.White,
                    fontSize = 11.sp
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

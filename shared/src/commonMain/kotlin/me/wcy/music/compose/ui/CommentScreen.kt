package me.wcy.music.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.comment.bean.CommentItem
import me.wcy.music.discover.comment.viewmodel.CommentViewModel

@Composable
fun CommentPanel(
    viewModel: CommentViewModel,
    onMessage: (String) -> Unit = {},
    onOpenFloor: (parentCommentId: Long) -> Unit = {}
) {
    val total by viewModel.total.collectAsState()
    val hotComments by viewModel.hotComments.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // 滚动到接近底部时加载下一页；loadMore 内部有 loading 防重入
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { last ->
                val total = listState.layoutInfo.totalItemsCount
                if (total > 0 && last >= total - 3) viewModel.loadMore()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "评论 ($total)",
                color = AppThemeColor.TextH1,
                fontSize = 16.sp
            )
        }
        SortTabs(
            selected = sortType,
            onSelect = { viewModel.setSortType(it) }
        )
        LazyColumn(modifier = Modifier.weight(1f), state = listState) {
            if (hotComments.isNotEmpty()) {
                item { SectionHeader("精彩评论") }
                items(hotComments, key = { "hot_${it.commentId}" }) { comment ->
                    CommentRow(comment = comment, onLikeClick = {
                        viewModel.toggleLike(comment) { ok, msg ->
                            if (!ok) onMessage(msg ?: "操作失败")
                        }
                    }, onOpenFloor = { onOpenFloor(comment.commentId) })
                }
            }
            if (comments.isNotEmpty()) {
                item { SectionHeader("最新评论") }
                items(comments, key = { it.commentId }) { comment ->
                    CommentRow(comment = comment, onLikeClick = {
                        viewModel.toggleLike(comment) { ok, msg ->
                            if (!ok) onMessage(msg ?: "操作失败")
                        }
                    }, onOpenFloor = { onOpenFloor(comment.commentId) })
                }
            }
            if (loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("加载中…", color = AppThemeColor.TextH2, fontSize = 13.sp)
                    }
                }
            } else if (comments.isEmpty() && hotComments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无评论", color = AppThemeColor.TextH2, fontSize = 13.sp)
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                singleLine = true,
                placeholder = { Text("随乐而起，有感而发", fontSize = 13.sp) },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.send(inputText) { success, msg ->
                            if (success) {
                                inputText = ""
                                focusManager.clearFocus()
                            } else {
                                onMessage(msg ?: "发送失败")
                            }
                        }
                    }
                }
            ) {
                Text("发送", color = AppThemeColor.ThemeColor)
            }
        }
    }
}

@Composable
internal fun CommentRow(
    comment: CommentItem,
    onLikeClick: () -> Unit,
    showLike: Boolean = true,
    onOpenFloor: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        CoverImage(
            url = comment.user?.avatarUrl.orEmpty(),
            contentDescription = "头像",
            modifier = Modifier.size(36.dp),
            cornerRadius = 18.dp
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.user?.nickname ?: "",
                    color = AppThemeColor.TextH2,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showLike) {
                    Icon(
                        imageVector = if (comment.liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "点赞",
                        tint = if (comment.liked) Color(0xFFEC4141) else AppThemeColor.TextH2,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(onClick = onLikeClick)
                    )
                    Text(
                        text = formatCount(comment.likedCount),
                        color = AppThemeColor.TextH2,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Text(
                text = comment.content,
                color = AppThemeColor.TextH1,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.timeStr,
                    color = AppThemeColor.TextH2,
                    fontSize = 11.sp
                )
                if (comment.replyCount > 0) {
                    Text(
                        text = "共 ${comment.replyCount} 条回复 >",
                        color = AppThemeColor.ThemeColor,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clickable(onClick = onOpenFloor)
                    )
                }
            }
        }
    }
}

@Composable
internal fun SectionHeader(title: String) {
    Text(
        text = title,
        color = AppThemeColor.TextH1,
        fontSize = 15.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SortTabs(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tabs = listOf(
            "推荐" to CommentViewModel.SORT_RECOMMEND,
            "最热" to CommentViewModel.SORT_HOT,
            "最新" to CommentViewModel.SORT_TIME
        )
        tabs.forEachIndexed { index, (label, type) ->
            Text(
                text = label,
                color = if (selected == type) AppThemeColor.ThemeColor else AppThemeColor.TextH2,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable(onClick = { onSelect(type) })
                    .padding(vertical = 6.dp)
            )
            if (index < tabs.lastIndex) {
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
    }
}

internal fun formatCount(count: Long): String {
    return when {
        count >= 100_000 -> "${count / 10_000}万"
        else -> count.toString()
    }
}

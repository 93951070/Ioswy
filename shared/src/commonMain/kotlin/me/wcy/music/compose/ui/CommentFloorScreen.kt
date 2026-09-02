package me.wcy.music.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.comment.viewmodel.CommentFloorViewModel

@Composable
fun CommentFloorScreen(
    viewModel: CommentFloorViewModel,
    resourceId: Long,
    resourceType: Int,
    parentCommentId: Long,
    onBack: () -> Unit,
    onMessage: (String) -> Unit = {}
) {
    LaunchedEffect(parentCommentId) {
        viewModel.load(resourceId, resourceType, parentCommentId)
    }

    val ownerComment by viewModel.ownerComment.collectAsState()
    val bestComments by viewModel.bestComments.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val loading by viewModel.loading.collectAsState()
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

    Column(modifier = Modifier.fillMaxWidth()) {
        TitleBar(title = "回复", onBack = onBack)
        LazyColumn(modifier = Modifier.weight(1f), state = listState) {
            ownerComment?.let { owner ->
                item(key = "owner_${owner.commentId}") { SectionHeader("楼主评论") }
                item(key = "owner_row_${owner.commentId}") {
                    CommentRow(comment = owner, onLikeClick = {}, showLike = false)
                }
            }
            if (bestComments.isNotEmpty()) {
                item { SectionHeader("精彩回复") }
                items(bestComments, key = { "best_${it.commentId}" }) {
                    CommentRow(comment = it, onLikeClick = {}, showLike = false)
                }
            }
            if (comments.isNotEmpty()) {
                item { SectionHeader("共 ${totalCount} 条回复") }
                items(comments, key = { it.commentId }) {
                    CommentRow(comment = it, onLikeClick = {}, showLike = false)
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
            } else if (comments.isEmpty() && ownerComment == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无回复", color = AppThemeColor.TextH2, fontSize = 13.sp)
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
                placeholder = { Text("回复", fontSize = 13.sp) },
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

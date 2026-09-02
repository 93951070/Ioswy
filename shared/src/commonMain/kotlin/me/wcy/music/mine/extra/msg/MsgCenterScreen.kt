package me.wcy.music.mine.extra.msg

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.mine.extra.TabChip
import me.wcy.music.mine.extra.bean.MsgItem
import me.wcy.music.shared.util.formatMsgTime

@Composable
fun MsgCenterScreen(
    viewModel: MsgCenterViewModel,
    onBack: () -> Unit,
    onOpenMsgDetail: (Long, String) -> Unit = { _, _ -> }
) {
    val state by viewModel.state.collectAsState()
    var tab by remember { mutableStateOf(0) }

    LaunchedEffect(tab) {
        viewModel.load(tab)
    }

    val msgs = when (tab) {
        0 -> state.privateMsgs
        1 -> state.commentMsgs
        else -> state.noticeMsgs
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TitleBar(title = "我的消息", onBack = onBack)
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                TabChip(label = "私信", selected = tab == 0, onClick = { tab = 0 })
                TabChip(label = "评论", selected = tab == 1, onClick = { tab = 1 })
                TabChip(label = "通知", selected = tab == 2, onClick = { tab = 2 })
            }
        }
        itemsIndexed(msgs) { _, msg ->
            MsgRow(
                msg = msg,
                onClick = {
                    if (tab == 0) {
                        val peer = msg.peer()
                        if (peer != null && peer.userId > 0) {
                            onOpenMsgDetail(peer.userId, peer.nickname)
                        }
                    }
                }
            )
        }
        if (tab in state.loadedTabs && msgs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无消息",
                        color = AppThemeColor.TextH2,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MsgRow(msg: MsgItem, onClick: () -> Unit = {}) {
    val user = msg.peer()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        CoverImage(
            url = user?.avatarUrl ?: "",
            cornerRadius = 20.dp,
            modifier = Modifier.size(40.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = user?.nickname?.ifBlank { "系统通知" } ?: "系统通知",
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = msg.message().ifBlank { "暂无内容" },
                color = AppThemeColor.TextH2,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = formatMsgTime(msg.timestamp()),
            color = AppThemeColor.TextH2,
            fontSize = 11.sp
        )
    }
}

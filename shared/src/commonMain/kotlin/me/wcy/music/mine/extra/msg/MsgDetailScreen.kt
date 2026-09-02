package me.wcy.music.mine.extra.msg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.mine.extra.MineExtraNet
import me.wcy.music.mine.extra.bean.MsgItem
import me.wcy.music.shared.util.formatMsgTime

/**
 * 私信详情：与指定用户的聊天记录列表。uid 为对方 userId。
 * 正文已在 MsgItem.message() 里解析 lastMsg 的 JSON 串。
 */
@Composable
fun MsgDetailScreen(
    uid: Long,
    nickname: String,
    onBack: () -> Unit
) {
    var msgs by remember { mutableStateOf(listOf<MsgItem>()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        if (uid > 0) {
            val data = runCatching { MineExtraNet.getPrivateMsgHistory(uid) }.getOrNull()
            if (data?.code == 200) {
                msgs = data.msgs
            }
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColor.Background)
    ) {
        TitleBar(title = nickname.ifBlank { "私信" }, onBack = onBack)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("加载中…", color = AppThemeColor.TextH2, fontSize = 13.sp)
            }
        } else if (msgs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无聊天记录", color = AppThemeColor.TextH2, fontSize = 13.sp)
            }
        } else {
            // 接口返回新→旧，倒序展示成聊天时间线
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(msgs.asReversed()) { _, msg ->
                    MsgBubble(msg = msg)
                }
            }
        }
    }
}

@Composable
private fun MsgBubble(msg: MsgItem) {
    val fromUser = msg.fromUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            CoverImage(
                url = fromUser?.avatarUrl ?: "",
                cornerRadius = 20.dp,
                modifier = Modifier.size(40.dp)
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .widthIn(max = 260.dp)
            ) {
                Text(
                    text = fromUser?.nickname ?: "",
                    color = AppThemeColor.TextH2,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppThemeColor.Card)
                ) {
                    Text(
                        text = msg.message(),
                        color = AppThemeColor.TextH1,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
        Text(
            text = formatMsgTime(msg.timestamp()),
            color = AppThemeColor.TextH2,
            fontSize = 10.sp,
            modifier = Modifier.padding(start = 72.dp, top = 2.dp)
        )
    }
}

package me.wcy.music.mine.extra

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.shared.bean.home.RecentResourceItem
import me.wcy.music.shared.net.ListenDataNet
import me.wcy.music.shared.util.formatMsgTime

/**
 * 听歌排行：累计听歌时长 + 最近听的歌曲。作为 MineScreen 内部覆盖层显示，无需路由。
 */
@Composable
fun ListenRankScreen(
    onBack: () -> Unit,
    onPlaySongs: (List<SongData>, Int) -> Unit = { _, _ -> }
) {
    var songs by remember { mutableStateOf<List<RecentResourceItem<SongData>>>(emptyList()) }
    var totalDuration by remember { mutableStateOf(0L) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        totalDuration = kotlin.runCatching { ListenDataNet.getListenDataTotal() }
            .getOrNull()?.data?.totalDuration ?: 0L
        songs = kotlin.runCatching { ListenDataNet.getRecentSongs(limit = 30) }
            .getOrNull()?.data?.list.orEmpty().filter { it.data != null && it.data.id > 0 }
        loaded = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColor.Background)
    ) {
        item {
            TitleBar(title = "听歌排行", onBack = onBack)
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppThemeColor.Card)
                    .padding(20.dp)
            ) {
                Text(
                    text = if (totalDuration > 0) {
                        "累计听歌 ${totalDuration / 3_600_000}小时${totalDuration % 3_600_000 / 60_000}分钟"
                    } else {
                        "暂无听歌数据"
                    },
                    color = AppThemeColor.TextH1,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "最近听过的歌曲",
                    color = AppThemeColor.TextH2,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        if (loaded && songs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "登录后可查看听歌排行",
                        color = AppThemeColor.TextH2,
                        fontSize = 13.sp
                    )
                }
            }
        }
        itemsIndexed(songs) { index, item ->
            val song = item.data ?: return@itemsIndexed
            ListenRankRow(
                index = index,
                song = song,
                playTime = item.playTime,
                onClick = { onPlaySongs(songs.mapNotNull { it.data }, index) }
            )
        }
    }
}

@Composable
private fun ListenRankRow(
    index: Int,
    song: SongData,
    playTime: Long,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (index + 1).toString(),
                color = AppThemeColor.TextH2,
                fontSize = 13.sp
            )
        }
        CoverImage(
            url = song.al.picUrl,
            cornerRadius = 4.dp,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(48.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = song.name,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.ar.joinToString("/") { it.name },
                color = AppThemeColor.TextH2,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = formatMsgTime(playTime),
            color = AppThemeColor.TextH2,
            fontSize = 12.sp
        )
    }
}

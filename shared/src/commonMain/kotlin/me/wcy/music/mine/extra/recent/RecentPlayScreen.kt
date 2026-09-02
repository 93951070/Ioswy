package me.wcy.music.mine.extra.recent

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
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.ui.PlayAllRow
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.mine.extra.TabChip
import me.wcy.music.mine.extra.bean.RecordItem
import me.wcy.music.shared.util.formatPlayCount

@Composable
fun RecentPlayScreen(
    viewModel: RecentPlayViewModel,
    onBack: () -> Unit,
    onPlaySongs: (List<SongData>, Int) -> Unit
) {
    val items by viewModel.items.collectAsState()
    val loaded by viewModel.loaded.collectAsState()
    var type by remember { mutableStateOf("1") }

    LaunchedEffect(type) {
        viewModel.load(type)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TitleBar(title = "最近播放", onBack = onBack)
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                TabChip(label = "周排行", selected = type == "0", onClick = { type = "0" })
                TabChip(label = "累计", selected = type == "1", onClick = { type = "1" })
            }
        }
        if (loaded && items.isNotEmpty()) {
            item {
                PlayAllRow(
                    songCount = items.size,
                    onPlayAll = { onPlaySongs(items.map { it.song }, 0) }
                )
            }
        }
        itemsIndexed(items) { index, record ->
            RecentPlayRow(
                index = index,
                record = record,
                onClick = { onPlaySongs(items.map { it.song }, index) }
            )
        }
        if (loaded && items.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无播放记录",
                        color = AppThemeColor.TextH2,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentPlayRow(
    index: Int,
    record: RecordItem,
    onClick: () -> Unit
) {
    val song = record.song
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
            text = formatPlayCount(record.playCount),
            color = AppThemeColor.TextH2,
            fontSize = 12.sp
        )
    }
}

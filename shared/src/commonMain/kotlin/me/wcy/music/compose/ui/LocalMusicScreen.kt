package me.wcy.music.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor

data class LocalSongData(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val cover: String,
    val duration: Long,
    val fileName: String,
    val fileSize: Long,
    val path: String
)

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun LocalMusicScreen(
    songs: List<LocalSongData>,
    loaded: Boolean,
    onPlaySong: (Int) -> Unit,
    onPlayAll: () -> Unit,
    onBack: () -> Unit
) {
    var infoSong by remember { mutableStateOf<LocalSongData?>(null) }
    val title = if (loaded && songs.isNotEmpty()) "本地音乐(${songs.size})" else "本地音乐"

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            TitleBar(title = title, onBack = onBack)
        }
        if (loaded && songs.isNotEmpty()) {
            item {
                PlayAllRow(
                    songCount = songs.size,
                    onPlayAll = onPlayAll
                )
            }
        }
        itemsIndexed(songs) { index, song ->
            LocalSongRow(
                song = song,
                onClick = { onPlaySong(index) },
                onMoreClick = { infoSong = song }
            )
        }
    }

    infoSong?.let { song ->
        ModalBottomSheet(
            onDismissRequest = { infoSong = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
                InfoRow("文件名称: ${song.fileName}")
                InfoRow("播放时长: ${formatMs(song.duration)}")
                InfoRow("文件大小: ${formatFileSize(song.fileSize)}")
                InfoRow("文件路径: ${song.path}")
            }
        }
    }
}

@Composable
private fun LocalSongRow(
    song: LocalSongData,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = song.cover,
            cornerRadius = 6.dp,
            modifier = Modifier.size(48.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = song.title,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} - ${song.album}",
                color = AppThemeColor.TextH2,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = formatMs(song.duration),
            color = AppThemeColor.TextH2,
            fontSize = 13.sp
        )
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "更多",
            tint = AppThemeColor.TextH2,
            modifier = Modifier
                .width(20.dp)
                .size(20.dp)
                .padding(start = 8.dp)
                .clickable(onClick = onMoreClick)
        )
    }
}

@Composable
private fun InfoRow(text: String) {
    Text(
        text = text,
        color = AppThemeColor.TextH1,
        fontSize = 14.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

private fun formatMs(milli: Long): String {
    val m = (milli / 60000).toString().padStart(2, '0')
    val s = (milli / 1000 % 60).toString().padStart(2, '0')
    return "$m:$s"
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "${bytes}B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${(kb * 10).roundToInt() / 10.0}KB"
    val mb = kb / 1024.0
    if (mb < 1024) return "${(mb * 10).roundToInt() / 10.0}MB"
    return "${(mb / 1024.0 * 10).roundToInt() / 10.0}GB"
}

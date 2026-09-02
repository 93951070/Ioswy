package me.wcy.music.mine.extra.cloud

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.mine.extra.bean.CloudItem

@Composable
fun CloudDiskScreen(
    viewModel: CloudDiskViewModel,
    onBack: () -> Unit,
    onPlaySongs: (List<SongData>, Int) -> Unit,
    onDelete: (CloudItem) -> Unit
) {
    val items by viewModel.items.collectAsState()
    val loaded by viewModel.loaded.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            val title = if (loaded) "音乐云盘(${items.size})" else "音乐云盘"
            TitleBar(title = title, onBack = onBack)
        }
        itemsIndexed(items) { index, item ->
            CloudSongRow(
                item = item,
                onClick = {
                    val songs = items.map { it.simpleSong }
                    if (index in songs.indices) {
                        onPlaySongs(songs, index)
                    }
                },
                onDelete = { onDelete(item) }
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
                        text = "云盘空空如也",
                        color = AppThemeColor.TextH2,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CloudSongRow(
    item: CloudItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val song = item.simpleSong
    val title = song.name.ifBlank { item.fileName }
    val subtitle = if (song.name.isBlank()) {
        "云盘文件"
    } else {
        song.ar.joinToString("/") { it.name }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = song.al.picUrl,
            cornerRadius = 4.dp,
            modifier = Modifier.size(48.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = title,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = AppThemeColor.TextH2,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = "删除",
            tint = AppThemeColor.TextH2,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onDelete)
        )
    }
}

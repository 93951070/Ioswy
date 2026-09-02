package me.wcy.music.album.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch
import me.wcy.music.album.bean.AlbumInfo
import me.wcy.music.album.detail.viewmodel.AlbumDetailViewModel
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.component.SongRow
import me.wcy.music.compose.ui.PlayAllRow
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.compose.theme.AppThemeColor

@Composable
fun AlbumDetailScreen(
    viewModel: AlbumDetailViewModel,
    albumId: Long,
    onBack: () -> Unit,
    onOpenArtist: (Long) -> Unit,
    onPlaySongs: (List<SongData>, Int) -> Unit
) {
    LaunchedEffect(albumId) {
        viewModel.init(albumId)
        viewModel.loadData()
    }

    val album by viewModel.album.collectAsState()
    val songList by viewModel.songList.collectAsState()
    val isSub by viewModel.isSub.collectAsState()
    var descExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TitleBar(title = album?.name ?: "专辑", onBack = onBack)
        }
        album?.let { data ->
            item {
                AlbumHeader(
                    album = data,
                    isSub = isSub,
                    onCollect = { scope.launch { viewModel.collect() } },
                    onOpenArtist = onOpenArtist
                )
            }
            if (data.description.isNotBlank()) {
                item {
                    AlbumDesc(
                        desc = data.description,
                        expanded = descExpanded,
                        onToggle = { descExpanded = !descExpanded }
                    )
                }
            }
            if (songList.isNotEmpty()) {
                item {
                    PlayAllRow(
                        songCount = songList.size,
                        onPlayAll = { onPlaySongs(songList, 0) }
                    )
                }
            }
            itemsIndexed(songList) { index, song ->
                SongRow(
                    song = song,
                    index = index + 1,
                    onClick = { onPlaySongs(songList, index) }
                )
            }
        }
    }
}

@Composable
private fun AlbumHeader(
    album: AlbumInfo,
    isSub: Boolean,
    onCollect: () -> Unit,
    onOpenArtist: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = album.getLargeCover(),
            cornerRadius = 4.dp,
            modifier = Modifier.size(120.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = album.name,
                color = AppThemeColor.TextH1,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = album.artist.name,
                color = AppThemeColor.TextH2,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable(enabled = album.artist.id > 0) { onOpenArtist(album.artist.id) }
            )
            Text(
                text = buildList {
                    formatDate(album.publishTime).takeIf { it.isNotBlank() }?.let { add(it) }
                    album.company.takeIf { it.isNotBlank() }?.let { add(it) }
                }.joinToString(" / "),
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp)
            )
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .background(AppThemeColor.ThemeColor, RoundedCornerShape(14.dp))
                    .clickable(onClick = onCollect)
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isSub) "已收藏" else "收藏",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun AlbumDesc(
    desc: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = "简介",
            color = AppThemeColor.TextH1,
            fontSize = 14.sp
        )
        Text(
            text = desc,
            color = AppThemeColor.TextH2,
            fontSize = 12.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = if (expanded) "收起" else "展开",
            color = AppThemeColor.TextH2,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(top = 2.dp)
                .clickable(onClick = onToggle)
        )
    }
}

private fun formatDate(milli: Long): String {
    if (milli <= 0) return ""
    val date = Instant.fromEpochMilliseconds(milli)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
}

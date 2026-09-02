package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.component.SongRow
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.playlist.detail.viewmodel.PlaylistViewModel
import me.wcy.music.shared.util.formatPlayCount

@Composable
fun PlaylistDetailScreen(
    viewModel: PlaylistViewModel,
    playlistId: Long,
    realtimeData: Boolean,
    isLike: Boolean,
    onBack: () -> Unit,
    onOpenPlaying: () -> Unit,
    onPlayAll: (List<SongData>) -> Unit,
    onPlaySong: (Int, List<SongData>) -> Unit
) {
    LaunchedEffect(playlistId) {
        viewModel.init(playlistId, realtimeData, isLike)
        viewModel.loadData()
    }

    val playlistData by viewModel.playlistData.collectAsState()
    val songList by viewModel.songList.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            TitleBar(
                title = playlistData?.name ?: "",
                onBack = onBack
            )
        }
        playlistData?.let { data ->
            item {
                PlaylistHeader(
                    playlist = data,
                    coverUrl = data.getLargeCover()
                )
            }
        }
        if (songList.isNotEmpty()) {
            item {
                PlayAllRow(
                    songCount = songList.size,
                    onPlayAll = { onPlayAll(songList) }
                )
            }
        }
        itemsIndexed(songList) { index, song ->
            SongRow(
                song = song,
                index = index + 1,
                onClick = {
                    onPlaySong(index, songList)
                }
            )
        }
    }
}

@Composable
private fun PlaylistHeader(
    playlist: PlaylistData,
    coverUrl: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = coverUrl,
            cornerRadius = 8.dp,
            modifier = Modifier.size(120.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = playlist.name,
                color = AppThemeColor.TextH1,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "· ${formatPlayCount(playlist.playCount)} plays",
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoverImage(
                    url = playlist.creator.avatarUrl,
                    cornerRadius = 12.dp,
                    modifier = Modifier
                        .size(24.dp)
                        .background(AppThemeColor.ThemeColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                )
                Text(
                    text = playlist.creator.nickname,
                    color = AppThemeColor.TextH2,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

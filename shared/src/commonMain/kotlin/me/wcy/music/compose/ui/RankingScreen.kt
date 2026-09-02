package me.wcy.music.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.ranking.viewmodel.RankingViewModel

@Composable
fun RankingScreen(
    viewModel: RankingViewModel,
    onBack: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onPlayPlaylistSong: (playlist: PlaylistData, position: Int) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    val rankingList by viewModel.rankingList.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            TitleBar(title = "排行榜", onBack = onBack)
        }
        rankingList.forEach { data ->
            when (data) {
                is RankingViewModel.TitleData -> item {
                    Text(
                        text = data.title,
                        color = AppThemeColor.TextH1,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                is RankingViewModel.RankingPlaylist -> item {
                    RankingBanner(
                        playlist = data.playlist,
                        songs = data.songs,
                        onOpenDetail = { onOpenPlaylistDetail(data.playlist.id) },
                        onSongClick = { pos ->
                            onPlayPlaylistSong(data.playlist, pos)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingBanner(
    playlist: PlaylistData,
    songs: List<SongData>,
    onOpenDetail: () -> Unit,
    onSongClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onOpenDetail),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = playlist.getSmallCover(),
            cornerRadius = 10.dp,
            modifier = Modifier.size(100.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = playlist.name,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = playlist.updateFrequency,
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            songs.take(3).forEachIndexed { index, song ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSongClick(index) }
                        .padding(vertical = 3.dp)
                ) {
                    Text(
                        text = "${index + 1}",
                        color = AppThemeColor.TextH2,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "${song.name} - ${song.ar.joinToString("/") { it.name }}",
                        color = AppThemeColor.TextH2,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

package me.wcy.music.mine.extra.sub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import me.wcy.music.mine.extra.bean.AlbumSubItem
import me.wcy.music.mine.extra.bean.ArtistSubItem
import me.wcy.music.mine.extra.bean.MvSubItem

@Composable
fun SubListScreen(
    viewModel: SubListViewModel,
    onBack: () -> Unit,
    onOpenArtist: (Long) -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenMv: (Long) -> Unit
) {
    val artists by viewModel.artists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val mvs by viewModel.mvs.collectAsState()
    val loaded by viewModel.loaded.collectAsState()
    var tab by remember { mutableStateOf(0) }

    LaunchedEffect(tab) {
        viewModel.load(tab)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TitleBar(title = "我的收藏", onBack = onBack)
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                TabChip(label = "歌手", selected = tab == 0, onClick = { tab = 0 })
                TabChip(label = "专辑", selected = tab == 1, onClick = { tab = 1 })
                TabChip(label = "MV", selected = tab == 2, onClick = { tab = 2 })
            }
        }
        when (tab) {
            0 -> itemsIndexed(artists) { _, artist ->
                ArtistRow(
                    artist = artist,
                    onClick = { onOpenArtist(artist.id) }
                )
            }
            1 -> itemsIndexed(albums) { _, album ->
                AlbumRow(
                    album = album,
                    onClick = { onOpenAlbum(album.id) }
                )
            }
            else -> itemsIndexed(mvs) { _, mv ->
                MvRow(
                    mv = mv,
                    onClick = { onOpenMv(mv.id) }
                )
            }
        }
        val isEmpty = when (tab) {
            0 -> artists.isEmpty()
            1 -> albums.isEmpty()
            else -> mvs.isEmpty()
        }
        if (loaded && isEmpty) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无收藏",
                        color = AppThemeColor.TextH2,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistRow(
    artist: ArtistSubItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = artist.picUrl,
            cornerRadius = 24.dp,
            modifier = Modifier.size(48.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = artist.name,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val aliasText = artist.aliasText()
            if (aliasText.isNotBlank()) {
                Text(
                    text = aliasText,
                    color = AppThemeColor.TextH2,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AlbumRow(
    album: AlbumSubItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = album.picUrl,
            cornerRadius = 4.dp,
            modifier = Modifier.size(48.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = album.name,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val aliasText = album.aliasText()
            if (aliasText.isNotBlank()) {
                Text(
                    text = aliasText,
                    color = AppThemeColor.TextH2,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MvRow(
    mv: MvSubItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = mv.coverUrl(),
            cornerRadius = 4.dp,
            modifier = Modifier
                .width(120.dp)
                .height(68.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = mv.name,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${mv.artistName} · ${formatMs(mv.durationValue())}",
                color = AppThemeColor.TextH2,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatMs(milli: Long): String {
    if (milli <= 0) return ""
    val m = (milli / 60000).toString().padStart(2, '0')
    val s = (milli / 1000 % 60).toString().padStart(2, '0')
    return "$m:$s"
}

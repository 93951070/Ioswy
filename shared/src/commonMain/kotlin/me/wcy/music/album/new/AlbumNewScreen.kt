package me.wcy.music.album.new

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.album.new.viewmodel.AlbumNewViewModel
import me.wcy.music.compose.component.CategoryChip
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.shared.util.CoverUtils.asSmallCover

@Composable
fun AlbumNewScreen(
    viewModel: AlbumNewViewModel,
    onBack: () -> Unit,
    onOpenAlbum: (Long) -> Unit
) {
    var selectedArea by remember { mutableStateOf("ALL") }
    val albums by viewModel.albums.collectAsState()

    LaunchedEffect(selectedArea) {
        viewModel.loadAlbums(selectedArea)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TitleBar(title = "新碟上架", onBack = onBack)
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(viewModel.areaList.size) { index ->
                    val (label, value) = viewModel.areaList[index]
                    CategoryChip(
                        label = label,
                        selected = value == selectedArea,
                        onClick = { selectedArea = value }
                    )
                }
            }
        }
        val rows = albums.chunked(3)
        items(rows.size) { rowIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rows[rowIndex].forEach { album ->
                    AlbumCell(
                        album = album,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenAlbum(album.albumId) }
                    )
                }
                repeat(3 - rows[rowIndex].size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AlbumCell(
    album: me.wcy.music.album.bean.NewAlbumItem,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        CoverImage(
            url = album.coverUrl.asSmallCover(),
            cornerRadius = 4.dp,
            modifier = Modifier.fillMaxWidth().size(120.dp)
        )
        Text(
            text = album.albumName,
            color = AppThemeColor.TextH1,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
        Text(
            text = album.artistName,
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

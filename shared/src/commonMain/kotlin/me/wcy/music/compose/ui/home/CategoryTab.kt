package me.wcy.music.compose.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.compose.component.PlaylistCard
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.playlist.square.bean.CatTag
import me.wcy.music.shared.net.DiscoverNet

@Composable
fun CategoryTab(
    onOpenPlaylistDetail: (Long) -> Unit,
    onPlayPlaylist: (PlaylistData) -> Unit
) {
    var catTags by remember { mutableStateOf(emptyList<CatTag>()) }
    var selectedCat by remember { mutableIntStateOf(-1) }
    var playlists by remember { mutableStateOf(emptyList<PlaylistData>()) }

    LaunchedEffect(Unit) {
        runCatching {
            DiscoverNet.getCatlist()
        }.onSuccess {
            catTags = it.sub
            if (selectedCat < 0 && it.sub.isNotEmpty()) {
                selectedCat = 0
            }
        }
    }

    LaunchedEffect(selectedCat) {
        val tag = catTags.getOrNull(selectedCat) ?: return@LaunchedEffect
        val result = runCatching {
            DiscoverNet.getPlaylistList(cat = tag.name, limit = 30, offset = 0)
        }.getOrNull() ?: return@LaunchedEffect
        playlists = result.playlists
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        if (catTags.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    catTags.forEachIndexed { index, tag ->
                        val isSelected = index == selectedCat
                        Text(
                            text = tag.name,
                            color = if (isSelected) AppThemeColor.ThemeColor else AppThemeColor.TextH2,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) AppThemeColor.ThemeColor.copy(alpha = 0.12f)
                                    else AppThemeColor.Card
                                )
                                .clickable { selectedCat = index }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        val chunkSize = 3
        val chunked = playlists.chunked(chunkSize)
        items(chunked.size) { rowIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val rowPlaylists = chunked[rowIndex]
                rowPlaylists.forEach { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenPlaylistDetail(playlist.id) },
                        onPlayClick = { onPlayPlaylist(playlist) }
                    )
                }
                repeat(chunkSize - rowPlaylists.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

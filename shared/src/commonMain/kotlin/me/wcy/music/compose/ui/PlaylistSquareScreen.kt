package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.playlist.square.viewmodel.PlaylistSquareViewModel
import me.wcy.music.shared.net.PlaylistApi
import me.wcy.music.shared.util.formatPlayCount

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaylistSquareScreen(
    viewModel: PlaylistSquareViewModel,
    onBack: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onPlayPlaylist: (PlaylistData) -> Unit
) {
    var selectedTag by remember { mutableStateOf("全部") }
    val tagList by viewModel.tagList.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var playlists by remember { mutableStateOf<List<PlaylistData>>(emptyList()) }
    var showAllCategories by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTagList()
    }

    LaunchedEffect(selectedTag) {
        kotlin.runCatching {
            PlaylistApi.getTopPlaylists(selectedTag, 30, 0)
        }.onSuccess {
            if (it.code == 200) {
                playlists = it.playlists
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TitleBar(title = "歌单广场", onBack = onBack)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(tagList.size) { index ->
                        CategoryChip(
                            label = tagList[index],
                            selected = tagList[index] == selectedTag,
                            onClick = { selectedTag = tagList[index] }
                        )
                    }
                }
                Text(
                    text = if (showAllCategories) "收起" else "全部分类",
                    color = AppThemeColor.TextH2,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { showAllCategories = !showAllCategories }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
                Icon(
                    imageVector = if (showAllCategories) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (showAllCategories) "收起" else "展开",
                    tint = AppThemeColor.TextH2,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable { showAllCategories = !showAllCategories }
                )
            }
        }
        if (showAllCategories) {
            items(categories.size) { index ->
                val group = categories[index]
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(
                        text = group.name,
                        color = AppThemeColor.TextH2,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        group.tags.forEach { tag ->
                            CategoryChip(
                                label = tag,
                                selected = tag == selectedTag,
                                onClick = {
                                    selectedTag = tag
                                    showAllCategories = false
                                }
                            )
                        }
                    }
                }
            }
        }
        val rows = playlists.chunked(3)
        items(rows.size) { rowIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rows[rowIndex].forEach { playlist ->
                    PlaylistCell(
                        playlist = playlist,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenPlaylistDetail(playlist.id) },
                        onPlayClick = { onPlayPlaylist(playlist) }
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
private fun PlaylistCell(
    playlist: PlaylistData,
    modifier: Modifier,
    onClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box {
            CoverImage(
                url = playlist.getSmallCover(),
                cornerRadius = 10.dp,
                modifier = Modifier.fillMaxWidth().size(120.dp)
            )
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "播放",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clickable(onClick = onPlayClick)
            )
        }
        Text(
            text = formatPlayCount(playlist.playCount),
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
        Text(
            text = playlist.name,
            color = AppThemeColor.TextH1,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(
                if (selected) AppThemeColor.ThemeColor else AppThemeColor.ThemeColor.copy(alpha = 0.1f),
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else AppThemeColor.TextH1,
            fontSize = 13.sp
        )
    }
}

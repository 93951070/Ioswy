package me.wcy.music.artist.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.artist.bean.ArtistInfo
import me.wcy.music.artist.list.viewmodel.ArtistListViewModel
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.compose.theme.AppThemeColor

@Composable
fun ArtistListScreen(
    viewModel: ArtistListViewModel,
    onBack: () -> Unit,
    onOpenArtist: (Long) -> Unit
) {
    val categories = viewModel.categories
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    val artists by viewModel.artists.collectAsState()

    LaunchedEffect(selectedCategory) {
        viewModel.loadArtists(selectedCategory)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(title = "歌手", onBack = onBack)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(categories.size) { index ->
                CategoryChip(
                    label = categories[index].name,
                    selected = categories[index] == selectedCategory,
                    onClick = { selectedCategory = categories[index] }
                )
            }
        }
        if (artists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "加载中...",
                    color = AppThemeColor.TextH2,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(artists.size) { index ->
                    ArtistCell(
                        artist = artists[index],
                        onClick = { onOpenArtist(artists[index].id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistCell(
    artist: ArtistInfo,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CoverImage(
            url = artist.getCoverUrl(),
            cornerRadius = 100.dp,
            modifier = Modifier.size(88.dp)
        )
        Text(
            text = artist.name,
            color = AppThemeColor.TextH1,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
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

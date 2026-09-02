package me.wcy.music.artist.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.wcy.music.artist.bean.ArtistAlbumItem
import me.wcy.music.artist.bean.ArtistDescData
import me.wcy.music.artist.bean.ArtistInfo
import me.wcy.music.artist.bean.MvItem
import me.wcy.music.artist.detail.viewmodel.ArtistDetailViewModel
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.component.SongRow
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.ui.PlayAllRow
import me.wcy.music.shared.util.CoverUtils.asSmallCover
import me.wcy.music.shared.util.formatPlayCount

private val TAB_TITLES = listOf("热门歌曲", "专辑", "MV", "简介")

@Composable
fun ArtistDetailScreen(
    viewModel: ArtistDetailViewModel,
    artistId: Long,
    onBack: () -> Unit,
    onPlaySongs: (List<SongData>, Int) -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenMv: (Long) -> Unit
) {
    val artist by viewModel.artist.collectAsState()
    val hotSongs by viewModel.hotSongs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val mvs by viewModel.mvs.collectAsState()
    val desc by viewModel.desc.collectAsState()
    val subscribed by viewModel.subscribed.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(artistId) {
        viewModel.init(artistId)
        launch { viewModel.loadDetail() }
        launch { viewModel.loadAlbums() }
        launch { viewModel.loadMvs() }
        launch { viewModel.loadDesc() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ArtistHeader(
            artist = artist,
            subscribed = subscribed,
            onBack = onBack,
            onToggleSub = { viewModel.toggleSub() }
        )
        TabBar(selected = selectedTab, onSelect = { selectedTab = it })
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> HotSongsTab(songs = hotSongs, onPlaySongs = onPlaySongs)
                1 -> AlbumsTab(albums = albums, onOpenAlbum = onOpenAlbum)
                2 -> MvsTab(mvs = mvs, onOpenMv = onOpenMv)
                else -> DescTab(desc = desc)
            }
        }
    }
}

@Composable
private fun ArtistHeader(
    artist: ArtistInfo?,
    subscribed: Boolean,
    onBack: () -> Unit,
    onToggleSub: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        CoverImage(
            url = artist?.getCoverUrl() ?: "",
            cornerRadius = 0.dp,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.55f))
                    )
                )
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "返回",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(32.dp)
                .clickable(onClick = onBack)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = artist?.name ?: "",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            val aliasText = artist?.alias?.joinToString(" / ") ?: ""
            if (aliasText.isNotBlank()) {
                Text(
                    text = aliasText,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .background(
                        if (subscribed) Color.White.copy(alpha = 0.2f) else AppThemeColor.ThemeColor,
                        RoundedCornerShape(50)
                    )
                    .clickable(onClick = onToggleSub)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (subscribed) "已收藏" else "+ 收藏",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun TabBar(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        TAB_TITLES.forEachIndexed { index, title ->
            Text(
                text = title,
                color = if (index == selected) AppThemeColor.ThemeColor else AppThemeColor.TextH1,
                fontSize = 15.sp,
                fontWeight = if (index == selected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp)
                    .wrapContentSize(Alignment.Center)
            )
        }
    }
}

@Composable
private fun HotSongsTab(
    songs: List<SongData>,
    onPlaySongs: (List<SongData>, Int) -> Unit
) {
    if (songs.isEmpty()) {
        EmptyTip(text = "暂无热门歌曲")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            PlayAllRow(
                songCount = songs.size,
                onPlayAll = { onPlaySongs(songs, 0) }
            )
        }
        itemsIndexed(songs) { index, song ->
            SongRow(
                song = song,
                index = index + 1,
                onClick = { onPlaySongs(songs, index) }
            )
        }
    }
}

@Composable
private fun AlbumsTab(
    albums: List<ArtistAlbumItem>,
    onOpenAlbum: (Long) -> Unit
) {
    if (albums.isEmpty()) {
        EmptyTip(text = "暂无专辑")
        return
    }
    LazyRow(
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(albums.size) { index ->
            val album = albums[index]
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .clickable { onOpenAlbum(album.id) }
            ) {
                CoverImage(
                    url = album.picUrl.asSmallCover(),
                    cornerRadius = 4.dp,
                    modifier = Modifier.size(110.dp)
                )
                Text(
                    text = album.name,
                    color = AppThemeColor.TextH1,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "${album.size}首",
                    color = AppThemeColor.TextH2,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun MvsTab(
    mvs: List<MvItem>,
    onOpenMv: (Long) -> Unit
) {
    if (mvs.isEmpty()) {
        EmptyTip(text = "暂无MV")
        return
    }
    LazyRow(
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(mvs.size) { index ->
            val mv = mvs[index]
            Column(
                modifier = Modifier
                    .width(150.dp)
                    .clickable { onOpenMv(mv.id) }
            ) {
                CoverImage(
                    url = mv.img,
                    cornerRadius = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                )
                Text(
                    text = mv.name,
                    color = AppThemeColor.TextH1,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = formatPlayCount(mv.playCount),
                    color = AppThemeColor.TextH2,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DescTab(desc: ArtistDescData?) {
    val brief = desc?.briefDesc ?: ""
    val introduction = desc?.introduction ?: listOf()
    if (brief.isBlank() && introduction.isEmpty()) {
        EmptyTip(text = "暂无简介")
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (brief.isNotBlank()) {
            Text(
                text = "歌手简介",
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = brief,
                color = AppThemeColor.TextH1,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        introduction.forEach { item ->
            if (item.ti.isNotBlank()) {
                Text(
                    text = item.ti,
                    color = AppThemeColor.TextH1,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            Text(
                text = item.txt,
                color = AppThemeColor.TextH1,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun EmptyTip(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = AppThemeColor.TextH2,
            fontSize = 14.sp
        )
    }
}

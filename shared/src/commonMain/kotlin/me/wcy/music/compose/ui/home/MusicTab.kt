package me.wcy.music.compose.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.AlbumData
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.shared.net.SharedNet

/** GET album/new 返回 {total, albums: [...]}，album 结构复用 AlbumData */
@Serializable
private data class AlbumNewWrap(
    @SerialName("total")
    val total: Int = 0,
    @SerialName("albums")
    val albums: List<AlbumData> = emptyList()
)

@Composable
fun MusicTab(
    onOpenNewSong: () -> Unit,
    onOpenArtistList: () -> Unit,
    onOpenRanking: () -> Unit,
    onOpenPlaylistSquare: () -> Unit,
    onOpenMvList: () -> Unit,
    onOpenVideo: () -> Unit,
    onOpenDjRank: () -> Unit
) {
    var newAlbums by remember { mutableStateOf(emptyList<AlbumData>()) }

    LaunchedEffect(Unit) {
        runCatching {
            SharedJson.decodeBean<AlbumNewWrap>(
                SharedNet.post(
                    "album/new",
                    params = listOf(
                        "limit" to 10,
                        "area" to "ALL"
                    )
                )
            )
        }.onSuccess {
            newAlbums = it.albums
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        item {
            EntranceCard(
                icon = Icons.Filled.Star,
                title = "新歌速递",
                subtitle = "抢先听最新发行",
                onClick = onOpenNewSong
            )
        }

        if (newAlbums.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "新碟上架", onMore = null)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(newAlbums, key = { it.id }) { album ->
                        AlbumShelfCard(album = album)
                    }
                }
            }
        }

        item {
            EntranceCard(
                icon = Icons.Filled.Person,
                title = "歌手",
                subtitle = "热门歌手分类浏览",
                onClick = onOpenArtistList
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EntranceItem(icon = Icons.Filled.BarChart, label = "金榜", onClick = onOpenRanking)
                EntranceItem(icon = Icons.Filled.QueueMusic, label = "歌单", onClick = onOpenPlaylistSquare)
                EntranceItem(icon = Icons.Filled.Slideshow, label = "MV", onClick = onOpenMvList)
                EntranceItem(icon = Icons.Filled.PlayArrow, label = "视频", onClick = onOpenVideo)
                EntranceItem(icon = Icons.Filled.PlayCircle, label = "电台榜", onClick = onOpenDjRank)
            }
        }
    }
}

/** 入口大卡：图标 + 标题 + 副标题 */
@Composable
private fun EntranceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppThemeColor.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppThemeColor.ThemeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AppThemeColor.ThemeColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = AppThemeColor.TextH2,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** 新碟上架展示卡 */
@Composable
private fun AlbumShelfCard(album: AlbumData) {
    Column(
        modifier = Modifier.width(120.dp)
    ) {
        CoverImage(
            url = album.getSmallCover(),
            cornerRadius = 10.dp,
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = album.name,
            color = AppThemeColor.TextH1,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
        )
    }
}

/** 小圆底入口 */
@Composable
private fun EntranceItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(AppThemeColor.Card),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AppThemeColor.ThemeColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = AppThemeColor.TextH2,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

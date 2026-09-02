package me.wcy.music.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.search.bean.SearchAlbumData
import me.wcy.music.search.bean.SearchArtistData
import me.wcy.music.search.bean.SearchUserData
import me.wcy.music.dj.bean.DjRadioData
import me.wcy.music.mv.bean.MvItem

/**
 * 搜索类型 tab 行（单曲/歌手/专辑/歌单/MV/电台/用户）。
 */
@Composable
fun SearchTypeTabRow(
    selected: SearchType,
    onSelect: (SearchType) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(SearchType.entries) { type ->
            val isSelected = type == selected
            Text(
                text = type.label,
                color = if (isSelected) AppThemeColor.ThemeColor else AppThemeColor.TextH2,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clickable { onSelect(type) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun SearchArtistRow(
    item: SearchArtistData,
    onClick: () -> Unit
) {
    SearchItemRow(
        onClick = onClick,
        cover = {
            CoverImage(
                url = item.picUrl,
                cornerRadius = 24.dp,
                modifier = Modifier.size(48.dp)
            )
        },
        title = item.name,
        subtitle = item.alias.joinToString("/")
    )
}

@Composable
fun SearchAlbumRow(
    item: SearchAlbumData,
    onClick: () -> Unit
) {
    SearchItemRow(
        onClick = onClick,
        cover = {
            CoverImage(
                url = item.picUrl,
                cornerRadius = 6.dp,
                modifier = Modifier.size(48.dp)
            )
        },
        title = item.name,
        subtitle = item.artist.name
    )
}

@Composable
fun SearchPlaylistRow(
    item: PlaylistData,
    onClick: () -> Unit
) {
    SearchItemRow(
        onClick = onClick,
        cover = {
            CoverImage(
                url = item.coverImgUrl,
                cornerRadius = 6.dp,
                modifier = Modifier.size(48.dp)
            )
        },
        title = item.name,
        subtitle = "${item.trackCount}首",
        trailing = "${formatPlayCount(item.playCount)}次播放"
    )
}

@Composable
fun SearchMvRow(
    item: MvItem,
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
            url = item.cover,
            cornerRadius = 6.dp,
            modifier = Modifier
                .size(width = 96.dp, height = 54.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = item.name,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.artistName,
                color = AppThemeColor.TextH2,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = "${formatPlayCount(item.playCount)}播放",
            color = AppThemeColor.TextH2,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SearchDjRow(
    item: DjRadioData,
    onClick: () -> Unit
) {
    SearchItemRow(
        onClick = onClick,
        cover = {
            CoverImage(
                url = item.picUrl,
                cornerRadius = 12.dp,
                modifier = Modifier.size(48.dp)
            )
        },
        title = item.name,
        subtitle = item.dj.nickname
    )
}

@Composable
fun SearchUserRow(
    item: SearchUserData,
    onClick: () -> Unit
) {
    SearchItemRow(
        onClick = onClick,
        cover = {
            CoverImage(
                url = item.avatarUrl,
                cornerRadius = 24.dp,
                modifier = Modifier.size(48.dp)
            )
        },
        title = item.nickname,
        subtitle = item.signature
    )
}

@Composable
private fun SearchItemRow(
    onClick: () -> Unit,
    cover: @Composable () -> Unit,
    title: String,
    subtitle: String,
    trailing: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        cover()
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = title,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = AppThemeColor.TextH2,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (trailing.isNotEmpty()) {
            Text(
                text = trailing,
                color = AppThemeColor.TextH2,
                fontSize = 12.sp
            )
        }
    }
}

fun formatPlayCount(count: Long): String = when {
    count >= 100_000_000 -> "${count / 100_000_000}亿"
    count >= 10_000 -> "${count / 10_000}万"
    else -> count.toString()
}

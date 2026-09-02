package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.component.PlaylistCard
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.banner.BannerData
import me.wcy.music.discover.home.viewmodel.DiscoverViewModel

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onOpenRanking: () -> Unit,
    onOpenPlaylistSquare: () -> Unit,
    onOpenRecommendSong: () -> Unit,
    onOpenPersonalFm: () -> Unit,
    onOpenPlaying: () -> Unit,
    onPlaySong: (SongData) -> Unit,
    onPlayPlaylist: (PlaylistData) -> Unit,
    onPlayPlaylistSong: (PlaylistData, Int) -> Unit
) {
    val bannerList by viewModel.bannerList.collectAsState()
    val recommendPlaylist by viewModel.recommendPlaylist.collectAsState()
    val rankingList by viewModel.rankingList.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        item {
            DiscoverTitleBar(
                onOpenDrawer = onOpenDrawer,
                onOpenSearch = onOpenSearch
            )
        }

        item {
            if (bannerList.isNotEmpty()) {
                BannerPager(bannerList) { banner ->
                    banner.song?.let(onPlaySong)
                }
            }
        }

        item {
            EntranceRow(
                onOpenRecommendSong = onOpenRecommendSong,
                onOpenPersonalFm = onOpenPersonalFm,
                onOpenPlaylistSquare = onOpenPlaylistSquare,
                onOpenRanking = onOpenRanking
            )
        }

        if (recommendPlaylist.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "推荐歌单",
                    onMore = onOpenPlaylistSquare
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(recommendPlaylist, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            modifier = Modifier.width(120.dp),
                            onClick = { onOpenPlaylistDetail(playlist.id) },
                            onPlayClick = {
                                onPlayPlaylist(playlist)
                            }
                        )
                    }
                }
            }
        }

        if (rankingList.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "排行榜",
                    onMore = onOpenRanking
                )
            }
            items(rankingList, key = { it.id }) { playlist ->
                RankingItem(
                    playlist = playlist,
                    onOpenDetail = { onOpenPlaylistDetail(playlist.id) },
                    onSongClick = { pos ->
                        onPlayPlaylistSong(playlist, pos)
                    }
                )
            }
        }
    }
}

@Composable
private fun DiscoverTitleBar(
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "菜单",
            tint = AppThemeColor.TextH1,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onOpenDrawer)
        )
        Row(
            modifier = Modifier
                .padding(start = 16.dp)
                .height(36.dp)
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(AppThemeColor.ThemeColor.copy(alpha = 0.1f))
                .clickable(onClick = onOpenSearch),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "搜索",
                tint = AppThemeColor.TextH2,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "  搜索",
                color = AppThemeColor.TextH2,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun BannerPager(
    banners: List<BannerData>,
    onBannerClick: (BannerData) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(120.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
        pageSpacing = 0.dp
    ) { page ->
        val banner = banners[page]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onBannerClick(banner) }
        ) {
            CoverImage(
                url = banner.pic,
                contentDescription = "Banner",
                cornerRadius = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
private fun EntranceRow(
    onOpenRecommendSong: () -> Unit,
    onOpenPersonalFm: () -> Unit,
    onOpenPlaylistSquare: () -> Unit,
    onOpenRanking: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        EntranceItem(
            icon = Icons.Filled.AudioFile,
            label = "每日推荐",
            onClick = onOpenRecommendSong
        )
        EntranceItem(
            icon = Icons.Filled.VideoLibrary,
            label = "私人FM",
            onClick = onOpenPersonalFm
        )
        EntranceItem(
            icon = Icons.Filled.SurroundSound,
            label = "歌单",
            onClick = onOpenPlaylistSquare
        )
        EntranceItem(
            icon = Icons.Filled.Leaderboard,
            label = "排行榜",
            onClick = onOpenRanking
        )
    }
}

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
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppThemeColor.ThemeColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AppThemeColor.ThemeColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = label,
            color = AppThemeColor.TextH1,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = AppThemeColor.TextH1,
            fontSize = 17.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onMore)
        ) {
            Text(
                text = "更多",
                color = AppThemeColor.TextH2,
                fontSize = 13.sp
            )
            Icon(
                imageVector = Icons.Filled.MoreHoriz,
                contentDescription = "更多",
                tint = AppThemeColor.TextH2,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun RankingItem(
    playlist: PlaylistData,
    onOpenDetail: () -> Unit,
    onSongClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppThemeColor.Card)
            .clickable(onClick = onOpenDetail),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = playlist.getSmallCover(),
            cornerRadius = 10.dp,
            modifier = Modifier
                .padding(12.dp)
                .size(100.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = playlist.name,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            playlist.songList.take(3).forEachIndexed { index, song ->
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
                        text = song.name,
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

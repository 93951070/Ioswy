package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.ui.home.CategoryTab
import me.wcy.music.compose.ui.home.HeartTab
import me.wcy.music.compose.ui.home.MusicTab
import me.wcy.music.compose.ui.home.PodcastTab
import me.wcy.music.compose.ui.home.RecommendTab
import me.wcy.music.discover.home.viewmodel.DiscoverViewModel
import me.wcy.music.shared.player.PlayerEngine

private val HOME_TABS = listOf("心动", "推荐", "音乐", "播客", "分类")

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    playerEngine: PlayerEngine,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onOpenRanking: () -> Unit,
    onOpenPlaylistSquare: () -> Unit,
    onOpenRecommendSong: () -> Unit,
    onOpenPersonalFm: () -> Unit,
    onOpenArtistList: () -> Unit,
    onOpenNewSong: () -> Unit,
    onOpenDj: () -> Unit,
    onOpenMvList: () -> Unit,
    onOpenPlaying: () -> Unit,
    onPlaySong: (SongData) -> Unit,
    onPlayPlaylist: (PlaylistData) -> Unit,
    onPlayPlaylistSong: (PlaylistData, Int) -> Unit,
    onPlayDailySong: (List<SongData>, Int) -> Unit = { songs, index ->
        songs.getOrNull(index)?.let(onPlaySong)
    },
    onOpenArtist: (Long) -> Unit = {},
    onOpenDjRadio: (Long) -> Unit = {},
    onOpenMv: (Long) -> Unit = {},
    onOpenVideo: () -> Unit = {},
    onOpenDjRank: () -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { HOME_TABS.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HomeTitleBar(
            onOpenDrawer = onOpenDrawer,
            onOpenSearch = onOpenSearch
        )

        HomeTabRow(
            tabs = HOME_TABS,
            selected = pagerState.currentPage,
            onSelect = { index ->
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> HeartTab(
                    playerEngine = playerEngine,
                    onPlayQueue = onPlayDailySong,
                    onOpenPlaying = onOpenPlaying,
                    onOpenRecommendSong = onOpenRecommendSong
                )
                1 -> RecommendTab(
                    viewModel = viewModel,
                    onPlaySong = onPlaySong,
                    onOpenPlaylistDetail = onOpenPlaylistDetail,
                    onOpenRanking = onOpenRanking,
                    onOpenPlaylistSquare = onOpenPlaylistSquare,
                    onOpenDjRadio = onOpenDjRadio,
                    onOpenRecommendSong = onOpenRecommendSong,
                    onOpenMv = onOpenMv
                )
                2 -> MusicTab(
                    onOpenNewSong = onOpenNewSong,
                    onOpenArtistList = onOpenArtistList,
                    onOpenRanking = onOpenRanking,
                    onOpenPlaylistSquare = onOpenPlaylistSquare,
                    onOpenMvList = onOpenMvList,
                    onOpenVideo = onOpenVideo,
                    onOpenDjRank = onOpenDjRank
                )
                3 -> PodcastTab(
                    viewModel = viewModel,
                    onOpenDj = onOpenDj,
                    onOpenDjRadio = onOpenDjRadio,
                    onOpenDjRank = onOpenDjRank
                )
                else -> CategoryTab(
                    onOpenPlaylistDetail = onOpenPlaylistDetail,
                    onPlayPlaylist = onPlayPlaylist
                )
            }
        }
    }
}

/** 标题行：汉堡菜单 + 「首页」标题 + 搜索按钮 */
@Composable
private fun HomeTitleBar(
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
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "首页",
            color = AppThemeColor.TextH1,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AppThemeColor.SearchBar)
                .clickable(onClick = onOpenSearch),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "搜索",
                tint = AppThemeColor.TextH2,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** 顶部文字 tab，选中高亮 + 下划线 */
@Composable
private fun HomeTabRow(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onSelect(index) }
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Text(
                    text = title,
                    color = if (isSelected) AppThemeColor.ThemeColor else AppThemeColor.TextH2,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .width(20.dp)
                        .height(if (isSelected) 3.dp else 0.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppThemeColor.ThemeColor)
                )
            }
        }
    }
}

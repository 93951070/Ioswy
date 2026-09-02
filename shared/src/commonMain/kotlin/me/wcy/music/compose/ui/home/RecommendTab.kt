package me.wcy.music.compose.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.ui.ArtistCircleItem
import me.wcy.music.compose.component.PlaylistCard
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.banner.BannerData
import me.wcy.music.discover.home.viewmodel.DiscoverViewModel
import me.wcy.music.shared.net.SharedNet

/**
 * GET personalized/djprogram 返回 {code, category, result: [{id, name, copywriter, picUrl, program: {radio: {...}}}]}
 * program.radio 复用 DjRadioData 解析。
 */
@Serializable
private data class DjProgramWrap(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("result")
    val result: List<DjProgramItem> = emptyList()
)

@Serializable
private data class DjProgramItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("copywriter")
    val copywriter: String = "",
    @SerialName("picUrl")
    val picUrl: String = "",
    @SerialName("program")
    val program: DjProgramInner = DjProgramInner()
)

@Serializable
private data class DjProgramInner(
    @SerialName("radio")
    val radio: RadioRef = RadioRef()
)

@Serializable
private data class RadioRef(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = ""
)

@Composable
fun RecommendTab(
    viewModel: DiscoverViewModel,
    onPlaySong: (SongData) -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onPlayPlaylist: (PlaylistData) -> Unit,
    onPlayPlaylistSong: (PlaylistData, Int) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onOpenArtistList: () -> Unit,
    onOpenRanking: () -> Unit,
    onOpenPlaylistSquare: () -> Unit,
    onOpenDjRadio: (Long) -> Unit
) {
    val bannerList by viewModel.bannerList.collectAsState()
    val recommendPlaylist by viewModel.recommendPlaylist.collectAsState()
    val rankingList by viewModel.rankingList.collectAsState()
    val hotArtistList by viewModel.hotArtistList.collectAsState()
    val highQualityPlaylists by viewModel.highQualityPlaylists.collectAsState()
    var djPrograms by remember { mutableStateOf(emptyList<DjProgramItem>()) }

    LaunchedEffect(Unit) {
        runCatching {
            SharedJson.decodeBean<DjProgramWrap>(
                SharedNet.post("personalized/djprogram", params = listOf("limit" to 6))
            )
        }.onSuccess {
            if (it.code == 200) {
                djPrograms = it.result
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        if (bannerList.isNotEmpty()) {
            item {
                HomeBannerPager(bannerList) { banner ->
                    banner.song?.let(onPlaySong)
                }
            }
        }

        if (rankingList.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "排行榜", onMore = onOpenRanking)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(rankingList, key = { it.id }) { playlist ->
                        RankingCard(
                            playlist = playlist,
                            modifier = Modifier.width(280.dp),
                            onOpenDetail = { onOpenPlaylistDetail(playlist.id) },
                            onSongClick = { pos -> onPlayPlaylistSong(playlist, pos) }
                        )
                    }
                }
            }
        }

        if (recommendPlaylist.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "推荐歌单", onMore = onOpenPlaylistSquare)
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
                            onPlayClick = { onPlayPlaylist(playlist) }
                        )
                    }
                }
            }
        }

        if (hotArtistList.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "热门歌手", onMore = onOpenArtistList)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(hotArtistList, key = { it.id }) { artist ->
                        ArtistCircleItem(
                            artist = artist,
                            onClick = { onOpenArtist(artist.id) }
                        )
                    }
                }
            }
        }

        if (djPrograms.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "热门节目", onMore = null)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(djPrograms, key = { it.id }) { program ->
                        DjProgramCard(
                            item = program,
                            onClick = {
                                val radioId = program.program.radio.id
                                if (radioId > 0) {
                                    onOpenDjRadio(radioId)
                                }
                            }
                        )
                    }
                }
            }
        }

        if (highQualityPlaylists.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "热门精选", onMore = onOpenPlaylistSquare)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(highQualityPlaylists, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            modifier = Modifier.width(120.dp),
                            onClick = { onOpenPlaylistDetail(playlist.id) },
                            onPlayClick = { onPlayPlaylist(playlist) }
                        )
                    }
                }
            }
        }
    }
}

/** 轮播图 */
@Composable
private fun HomeBannerPager(
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
            Text(
                text = "${pagerState.currentPage + 1}/${banners.size}",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

/** 排行榜 280dp 大卡：封面 + 榜名 + 前 3 首歌 */
@Composable
private fun RankingCard(
    playlist: PlaylistData,
    modifier: Modifier = Modifier,
    onOpenDetail: () -> Unit,
    onSongClick: (Int) -> Unit
) {
    Row(
        modifier = modifier
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

/** 热门节目卡 */
@Composable
private fun DjProgramCard(
    item: DjProgramItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        CoverImage(
            url = item.picUrl,
            cornerRadius = 10.dp,
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = item.name,
            color = AppThemeColor.TextH1,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
        )
        Text(
            text = item.copywriter,
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}

package me.wcy.music.compose.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.dj.bean.DjRadioData
import me.wcy.music.mv.MvNet
import me.wcy.music.mv.bean.PersonalizedMvCard
import me.wcy.music.shared.bean.home.ToplistV2ItemData
import me.wcy.music.shared.net.AccountNet
import me.wcy.music.shared.net.DjRadioExtraNet
import me.wcy.music.shared.net.HomepageNet
import me.wcy.music.shared.net.ListenDataNet
import me.wcy.music.shared.net.MineNet
import me.wcy.music.shared.net.PlayExtraNet
import me.wcy.music.shared.net.PlaylistApi
import me.wcy.music.discover.home.viewmodel.DiscoverViewModel

@Composable
fun RecommendTab(
    viewModel: DiscoverViewModel,
    onPlaySong: (SongData) -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onOpenRanking: () -> Unit,
    onOpenPlaylistSquare: () -> Unit,
    onOpenDjRadio: (Long) -> Unit,
    onOpenRecommendSong: () -> Unit,
    onOpenMv: (Long) -> Unit
) {
    val recommendPlaylist by viewModel.recommendPlaylist.collectAsState()
    var dailySongs by remember { mutableStateOf(emptyList<SongData>()) }
    var likeSongs by remember { mutableStateOf(emptyList<SongData>()) }
    var folkPlaylists by remember { mutableStateOf(emptyList<PlaylistData>()) }
    var rankingList by remember { mutableStateOf(emptyList<ToplistV2ItemData>()) }
    var djRadioList by remember { mutableStateOf(emptyList<DjRadioData>()) }
    var mvList by remember { mutableStateOf(emptyList<PersonalizedMvCard>()) }

    // 每日推荐：history/recommend/songs 匿名/当日无数据时 songs 为 null，需取 dates[0] 调详情接口
    LaunchedEffect(Unit) {
        runCatching {
            val wrap = HomepageNet.getHistoryRecommendSongs()
            wrap.data?.songs?.takeIf { it.isNotEmpty() }
                ?: wrap.data?.dates?.firstOrNull()?.let { date ->
                    HomepageNet.getHistoryRecommendSongsDetail(date).data?.songs
                }
        }.onSuccess { songs ->
            dailySongs = songs.orEmpty()
        }
    }

    // 喜爱歌曲：likelist 只有 id 列表，需登录拿 uid 后随机取 10 首查 song/detail
    LaunchedEffect(Unit) {
        runCatching {
            val uid = AccountNet.getLoginStatus().data.profile?.userId ?: 0L
            if (uid == 0L) return@runCatching emptyList<SongData>()
            val ids = MineNet.getMyLikeSongList(uid).ids.shuffled().take(10)
            if (ids.isEmpty()) emptyList<SongData>()
            else PlayExtraNet.getSongDetail(ids.toList()).songs
        }.onSuccess {
            likeSongs = it
        }
    }

    // 民谣歌单
    LaunchedEffect(Unit) {
        runCatching {
            PlaylistApi.getTopPlaylists(cat = "民谣", limit = 10, offset = 0)
        }.onSuccess {
            folkPlaylists = it.playlists
        }
    }

    // 排行榜：getTopList(id) 需逐榜请求，toplist/detail/v2 一次返回榜单名/封面/前三歌
    LaunchedEffect(Unit) {
        runCatching {
            ListenDataNet.getToplistDetailV2()
        }.onSuccess {
            rankingList = it.data.firstOrNull()?.list.orEmpty()
        }
    }

    // 节目推荐：getDjProgramToplistHours 的 program 无 radio 字段无法跳电台，
    // 实测 getDjPersonalizeRecommend 返回电台列表（data: List<DjRadioData>）有数据，选它
    LaunchedEffect(Unit) {
        runCatching {
            DjRadioExtraNet.getDjPersonalizeRecommend(limit = 6)
        }.onSuccess {
            djRadioList = it.data
        }
    }

    // MV 推荐
    LaunchedEffect(Unit) {
        runCatching {
            MvNet.getPersonalizedMv()
        }.onSuccess {
            mvList = it.result
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        if (dailySongs.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "每日推荐", onMore = onOpenRecommendSong)
            }
            item {
                SongSectionCard(
                    songs = dailySongs.take(5),
                    onPlaySong = onPlaySong,
                    onViewAll = onOpenRecommendSong
                )
            }
        }

        if (likeSongs.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "喜爱歌曲推荐", onMore = null)
            }
            item {
                SongSectionCard(
                    songs = likeSongs,
                    onPlaySong = onPlaySong,
                    onViewAll = null
                )
            }
        }

        if (folkPlaylists.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "民谣推荐", onMore = onOpenPlaylistSquare)
            }
            item {
                TwoRowPlaylistFlow(
                    playlists = folkPlaylists,
                    onOpenPlaylistDetail = onOpenPlaylistDetail
                )
            }
        }

        if (recommendPlaylist.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "歌单推荐", onMore = onOpenPlaylistSquare)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(recommendPlaylist.take(10), key = { it.id }) { playlist ->
                        TopPlaylistCard(
                            playlist = playlist,
                            onClick = { onOpenPlaylistDetail(playlist.id) }
                        )
                    }
                }
            }
        }

        if (rankingList.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "排行榜推荐", onMore = onOpenRanking)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(rankingList, key = { it.id }) { toplist ->
                        RankingCard(
                            toplist = toplist,
                            onClick = onOpenRanking
                        )
                    }
                }
            }
        }

        if (djRadioList.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "节目推荐", onMore = null)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(djRadioList, key = { it.id }) { radio ->
                        DjProgramCard(
                            radio = radio,
                            onClick = { onOpenDjRadio(radio.id) }
                        )
                    }
                }
            }
        }

        if (mvList.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "MV 推荐", onMore = null)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(mvList, key = { it.id }) { mv ->
                        MvCard(
                            mv = mv,
                            onClick = { onOpenMv(mv.id) }
                        )
                    }
                }
            }
        }
    }
}

/** 歌曲列表区块卡片：红色序号 + 小圆角封面 + 歌名/歌手灰字，底部可带「查看全部」 */
@Composable
private fun SongSectionCard(
    songs: List<SongData>,
    onPlaySong: (SongData) -> Unit,
    onViewAll: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppThemeColor.Card)
    ) {
        songs.forEachIndexed { index, song ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaySong(song) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${index + 1}",
                    color = AppThemeColor.ThemeColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(24.dp)
                )
                CoverImage(
                    url = song.al.getSmallCover(),
                    cornerRadius = 6.dp,
                    modifier = Modifier.size(44.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                ) {
                    Text(
                        text = song.name,
                        color = AppThemeColor.TextH1,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.ar.joinToString("/") { it.name },
                        color = AppThemeColor.TextH2,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        if (onViewAll != null) {
            Text(
                text = "查看全部",
                color = AppThemeColor.TextH2,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onViewAll)
                    .padding(vertical = 10.dp)
            )
        }
    }
}

/** 歌单横滑两行交错网格：每列上下两张卡 */
@Composable
private fun TwoRowPlaylistFlow(
    playlists: List<PlaylistData>,
    onOpenPlaylistDetail: (Long) -> Unit
) {
    val rowCount = (playlists.size + 1) / 2
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(count = rowCount) { col ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                playlists.getOrNull(col)?.let { playlist ->
                    TopPlaylistCard(playlist) { onOpenPlaylistDetail(playlist.id) }
                }
                playlists.getOrNull(col + rowCount)?.let { playlist ->
                    TopPlaylistCard(playlist) { onOpenPlaylistDetail(playlist.id) }
                }
            }
        }
    }
}

/** 歌单卡：方圆角封面 + 播放量角标 + 标题两行截断 */
@Composable
private fun TopPlaylistCard(
    playlist: PlaylistData,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Box {
            CoverImage(
                url = playlist.getSmallCover(),
                cornerRadius = 8.dp,
                modifier = Modifier.size(120.dp)
            )
            PlayCountBadge(
                playCount = playlist.playCount,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )
        }
        Text(
            text = playlist.name,
            color = AppThemeColor.TextH1,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

/** 榜单卡：封面 + 榜单名 + 前三歌曲名小字 */
@Composable
private fun RankingCard(
    toplist: ToplistV2ItemData,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        CoverImage(
            url = toplist.coverUrl,
            cornerRadius = 8.dp,
            modifier = Modifier.size(140.dp)
        )
        Text(
            text = toplist.name,
            color = AppThemeColor.TextH1,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = toplist.tracks.take(3).joinToString("\n") { it.first },
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/** 节目卡（电台）：封面 + 播放量角标 + 电台名 */
@Composable
private fun DjProgramCard(
    radio: DjRadioData,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Box {
            CoverImage(
                url = radio.picUrl,
                cornerRadius = 8.dp,
                modifier = Modifier.size(120.dp)
            )
            PlayCountBadge(
                playCount = radio.playCount,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )
        }
        Text(
            text = radio.name,
            color = AppThemeColor.TextH1,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

/** MV 卡：16:9 封面 + 标题 + 歌手 */
@Composable
private fun MvCard(
    mv: PersonalizedMvCard,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick)
    ) {
        Box {
            CoverImage(
                url = mv.picUrl,
                cornerRadius = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
            PlayCountBadge(
                playCount = mv.playCount,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )
        }
        Text(
            text = mv.name,
            color = AppThemeColor.TextH1,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = mv.artistName,
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/** 播放量角标：播放图标 + 数量，半透明黑底圆角 */
@Composable
private fun PlayCountBadge(
    playCount: Long,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = formatPlayCount(playCount),
            color = Color.White,
            fontSize = 10.sp
        )
    }
}

private fun formatPlayCount(count: Long): String {
    val yi = 100_000_000L
    val wan = 10_000L
    fun oneDecimal(divisor: Long, unit: String): String {
        val v = count * 10 / divisor
        return if (v % 10 == 0L) "${v / 10}$unit" else "${v / 10}.${v % 10}$unit"
    }
    return when {
        count >= yi -> oneDecimal(yi, "亿")
        count >= wan -> oneDecimal(wan, "万")
        else -> count.toString()
    }
}

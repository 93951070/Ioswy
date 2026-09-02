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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.component.PlaylistCard
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.home.viewmodel.DiscoverViewModel
import me.wcy.music.shared.net.SharedNet

/**
 * GET personalized 返回 {hasTaste, code, category, result: [{id, name, coverImgUrl, playCount, trackCount}]}
 * 注意 fixLegacyFields 会把带 trackCount 的对象的 picUrl 重命名为 coverImgUrl。
 * playlist/mylike 实测返回 MLog feed 无歌单 id，personalize/privatecontent 本地 404，故选此接口。
 */
@Serializable
private data class HeartPlaylistWrap(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("result")
    val result: List<HeartPlaylistItem> = emptyList()
)

@Serializable
private data class HeartPlaylistItem(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("coverImgUrl")
    val coverUrl: String = "",
    @SerialName("playCount")
    val playCount: Long = 0,
    @SerialName("trackCount")
    val trackCount: Int = 0
)

@Composable
fun HeartTab(
    viewModel: DiscoverViewModel,
    onOpenRecommendSong: () -> Unit,
    onOpenPersonalFm: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onPlayPlaylist: (PlaylistData) -> Unit,
    onPlayDailySong: (List<SongData>, Int) -> Unit
) {
    val dailySongs by viewModel.dailySongs.collectAsState()
    val privatePlaylists by viewModel.recommendPlaylist.collectAsState()
    var heartbeatPlaylists by remember { mutableStateOf(emptyList<HeartPlaylistItem>()) }

    LaunchedEffect(Unit) {
        runCatching {
            SharedJson.decodeBean<HeartPlaylistWrap>(
                SharedNet.post("personalized", params = listOf("limit" to 10))
            )
        }.onSuccess {
            if (it.code == 200) {
                heartbeatPlaylists = it.result
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        if (dailySongs.isNotEmpty()) {
            item {
                DailyRecommendCard(
                    songCount = dailySongs.size,
                    coverUrl = dailySongs.first().al.getSmallCover(),
                    onPlayAll = { onPlayDailySong(dailySongs, 0) },
                    onOpenDetail = onOpenRecommendSong
                )
            }
        }

        item {
            PersonalFmCard(onClick = onOpenPersonalFm)
        }

        if (privatePlaylists.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "私人雷达", onMore = null)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(privatePlaylists, key = { it.id }) { playlist ->
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

        if (heartbeatPlaylists.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "心跳歌单", onMore = null)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(heartbeatPlaylists, key = { it.id }) { item ->
                        HeartPlaylistCard(
                            item = item,
                            onClick = { onOpenPlaylistDetail(item.id) }
                        )
                    }
                }
            }
        }
    }
}

/** 每日推荐大横卡：日期 + 歌曲数 + 播放全部 */
@Composable
private fun DailyRecommendCard(
    songCount: Int,
    coverUrl: String,
    onPlayAll: () -> Unit,
    onOpenDetail: () -> Unit
) {
    val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppThemeColor.ThemeColor.copy(alpha = 0.12f))
            .clickable(onClick = onOpenDetail)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppThemeColor.ThemeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${date.monthNumber}.${date.dayOfMonth}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = "每日推荐",
                color = AppThemeColor.TextH1,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "根据你的音乐口味生成 · ${songCount}首",
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(AppThemeColor.ThemeColor)
                .clickable(onClick = onPlayAll)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "播放全部",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "播放全部",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

/** 私人FM 入口卡 */
@Composable
private fun PersonalFmCard(onClick: () -> Unit) {
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
                imageVector = Icons.Filled.Radio,
                contentDescription = "私人FM",
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
                text = "私人FM",
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "享受你的专属电台",
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

/** 心跳歌单卡片 */
@Composable
private fun HeartPlaylistCard(
    item: HeartPlaylistItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        CoverImage(
            url = item.coverUrl,
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
    }
}

/** 区块标题，onMore 为 null 时隐藏「更多」入口 */
@Composable
internal fun HomeSectionHeader(
    title: String,
    onMore: (() -> Unit)?
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
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        if (onMore != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppThemeColor.ThemeColor.copy(alpha = 0.1f))
                    .clickable(onClick = onMore)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "更多",
                    color = AppThemeColor.ThemeColor,
                    fontSize = 12.sp
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "更多",
                    tint = AppThemeColor.ThemeColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

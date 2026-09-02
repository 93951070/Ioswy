package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.component.PlaylistCard
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.mine.home.viewmodel.MineViewModel

@Composable
fun MineScreen(
    viewModel: MineViewModel,
    profileFlow: StateFlow<ProfileData?>,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenLogin: () -> Unit,
    onOpenLocalMusic: () -> Unit,
    onOpenPlaylistDetail: (PlaylistData, Boolean, Boolean) -> Unit
) {
    val profile by profileFlow.collectAsState()
    val likePlaylist by viewModel.likePlaylist.collectAsState()
    val myPlaylists by viewModel.myPlaylists.collectAsState()
    val collectPlaylists by viewModel.collectPlaylists.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            MineTitleBar(
                onOpenDrawer = onOpenDrawer,
                onOpenSearch = onOpenSearch
            )
        }

        item {
            ProfileHeader(
                avatarUrl = profile?.avatarUrl ?: "",
                nickname = profile?.nickname ?: "",
                onOpenLogin = onOpenLogin
            )
        }

        item {
            LocalMusicEntry(onOpenLocalMusic = onOpenLocalMusic)
        }

        val like = likePlaylist
        if (like != null) {
            item {
                SectionTitle("我喜欢的音乐")
            }
            item {
                PlaylistRow(listOf(like)) { playlist ->
                    onOpenPlaylistDetail(playlist, false, true)
                }
            }
        }

        if (myPlaylists.isNotEmpty()) {
            item {
                SectionTitle("创建歌单(${myPlaylists.size})")
            }
            item {
                PlaylistRow(myPlaylists) { playlist ->
                    onOpenPlaylistDetail(playlist, true, false)
                }
            }
        }

        if (collectPlaylists.isNotEmpty()) {
            item {
                SectionTitle("收藏歌单(${collectPlaylists.size})")
            }
            item {
                PlaylistRow(collectPlaylists) { playlist ->
                    onOpenPlaylistDetail(playlist, false, false)
                }
            }
        }
    }
}

@Composable
private fun MineTitleBar(
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "菜单",
            tint = AppThemeColor.TextH1,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onOpenDrawer)
        )
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "搜索",
            tint = AppThemeColor.TextH1,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onOpenSearch)
        )
    }
}

@Composable
private fun ProfileHeader(
    avatarUrl: String,
    nickname: String,
    onOpenLogin: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .clickable(onClick = onOpenLogin),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = avatarUrl,
            cornerRadius = 30.dp,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E5E5))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = nickname.ifBlank { "点击登录" },
                color = AppThemeColor.TextH1,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (nickname.isBlank()) {
                Text(
                    text = "登录后同步歌单",
                    color = AppThemeColor.TextH2,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = "进入",
            tint = AppThemeColor.TextH2,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LocalMusicEntry(onOpenLocalMusic: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppThemeColor.ThemeColor.copy(alpha = 0.1f))
            .clickable(onClick = onOpenLocalMusic)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Headphones,
            contentDescription = "本地音乐",
            tint = AppThemeColor.ThemeColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "  本地音乐",
            color = AppThemeColor.TextH1,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = "进入",
            tint = AppThemeColor.TextH2,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = AppThemeColor.TextH1,
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun PlaylistRow(
    playlists: List<PlaylistData>,
    onItemClick: (PlaylistData) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(playlists, key = { it.id }) { playlist ->
            PlaylistCard(
                playlist = playlist,
                modifier = Modifier.width(120.dp),
                onClick = { onItemClick(playlist) }
            )
        }
    }
}

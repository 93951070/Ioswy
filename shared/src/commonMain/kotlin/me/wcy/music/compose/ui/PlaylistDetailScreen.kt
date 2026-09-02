package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.component.SongRow
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.playlist.detail.PlaylistManageSheet
import me.wcy.music.discover.playlist.detail.PlaylistShareDialog
import me.wcy.music.discover.playlist.detail.ToastHost
import me.wcy.music.discover.playlist.detail.viewmodel.PlaylistViewModel
import me.wcy.music.shared.net.PlayExtraNet
import me.wcy.music.shared.net.PlaylistManageNet
import me.wcy.music.shared.util.formatPlayCount

@Composable
fun PlaylistDetailScreen(
    viewModel: PlaylistViewModel,
    playlistId: Long,
    realtimeData: Boolean,
    isLike: Boolean,
    onBack: () -> Unit,
    onOpenPlaying: () -> Unit,
    onPlayAll: (List<SongData>) -> Unit,
    onPlaySong: (Int, List<SongData>) -> Unit
) {
    LaunchedEffect(playlistId) {
        viewModel.init(playlistId, realtimeData, isLike)
        viewModel.loadData()
    }

    val playlistData by viewModel.playlistData.collectAsState()
    val songList by viewModel.songList.collectAsState()
    val myUserId by viewModel.myUserId.collectAsState()

    var toast by remember { mutableStateOf<String?>(null) }
    var showManage by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    var deleteSong by remember { mutableStateOf<SongData?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                TitleBar(
                    title = playlistData?.name ?: "",
                    onBack = onBack
                )
            }
            playlistData?.let { data ->
                item {
                    PlaylistHeader(
                        playlist = data,
                        coverUrl = data.getLargeCover(),
                        isCreator = myUserId != 0L && data.creator.userId == myUserId,
                        onManage = { showManage = true },
                        onShare = { showShare = true }
                    )
                }
            }
            if (songList.isNotEmpty()) {
                item {
                    PlayAllAndHeartRow(
                        songCount = songList.size,
                        onPlayAll = { onPlayAll(songList) },
                        onHeartMode = {
                            scope.launch {
                                val res = runCatching {
                                    PlayExtraNet.getIntelligenceList(playlistId, pid = playlistId)
                                }.getOrNull()
                                val recSongs = res?.data?.mapNotNull { it.songInfo }.orEmpty()
                                if (res?.code == 200 && recSongs.isNotEmpty()) {
                                    val seen = songList.map { it.id }.toSet()
                                    onPlayAll(songList + recSongs.filter { it.id !in seen })
                                    toast = "已开启心动模式"
                                } else {
                                    toast = "当前歌单不支持心动模式"
                                }
                            }
                        }
                    )
                }
            }
            itemsIndexed(songList) { index, song ->
                SongRow(
                    song = song,
                    index = index + 1,
                    onClick = {
                        onPlaySong(index, songList)
                    },
                    onMoreClick = if (myUserId != 0L && playlistData?.creator?.userId == myUserId) {
                        { deleteSong = song }
                    } else {
                        null
                    }
                )
            }
        }
        ToastHost(
            message = toast,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    playlistData?.let { data ->
        if (showManage) {
            PlaylistManageSheet(
                playlist = data,
                onDismiss = { showManage = false },
                onUpdated = {
                    scope.launch { viewModel.loadData() }
                }
            )
        }
        if (showShare) {
            PlaylistShareDialog(
                playlist = data,
                onDismiss = { showShare = false },
                onShared = { err ->
                    toast = if (err == null) "已分享" else err
                }
            )
        }
        deleteSong?.let { song ->
            AlertDialog(
                onDismissRequest = { deleteSong = null },
                title = { Text("删除歌曲") },
                text = { Text("确定从歌单中删除「${song.name}」吗？") },
                confirmButton = {
                    TextButton(onClick = {
                        deleteSong = null
                        scope.launch {
                            val res = runCatching {
                                PlaylistManageNet.deleteTracks(data.id, "${song.id}")
                            }.getOrNull()
                            if (res != null && res.code == 200) {
                                viewModel.removeSong(song)
                                toast = "已删除"
                            } else {
                                toast = res?.msg ?: res?.message ?: "删除失败"
                            }
                        }
                    }) { Text("删除") }
                },
                dismissButton = {
                    TextButton(onClick = { deleteSong = null }) { Text("取消") }
                }
            )
        }
    }
}

/** 播放全部 + 心动模式双入口行 */
@Composable
private fun PlayAllAndHeartRow(
    songCount: Int,
    onPlayAll: () -> Unit,
    onHeartMode: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(AppThemeColor.ThemeColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onPlayAll)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "播放全部",
                tint = AppThemeColor.ThemeColor
            )
            Text(
                text = "  播放全部 ($songCount)",
                color = AppThemeColor.TextH1,
                fontSize = 15.sp
            )
        }
        Row(
            modifier = Modifier
                .clickable(onClick = onHeartMode)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "心动模式",
                tint = AppThemeColor.ThemeColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "心动模式",
                color = AppThemeColor.TextH1,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun PlaylistHeader(
    playlist: PlaylistData,
    coverUrl: String,
    isCreator: Boolean,
    onManage: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = coverUrl,
            cornerRadius = 8.dp,
            modifier = Modifier.size(120.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = playlist.name,
                color = AppThemeColor.TextH1,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "· ${formatPlayCount(playlist.playCount)} plays",
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoverImage(
                    url = playlist.creator.avatarUrl,
                    cornerRadius = 12.dp,
                    modifier = Modifier
                        .size(24.dp)
                        .background(AppThemeColor.ThemeColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                )
                Text(
                    text = playlist.creator.nickname,
                    color = AppThemeColor.TextH2,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "私信分享",
                tint = AppThemeColor.TextH2,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onShare)
            )
            if (isCreator) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = "管理歌单",
                    tint = AppThemeColor.TextH2,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(start = 14.dp)
                        .clickable(onClick = onManage)
                )
            }
        }
    }
}

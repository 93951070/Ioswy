package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.mine.extra.ListenRankScreen
import me.wcy.music.mine.extra.MineExtraNet
import me.wcy.music.mine.extra.bean.UserLevelInfo
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.component.PlaylistCard
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.shared.net.PlaylistManageNet
import me.wcy.music.mine.home.viewmodel.MineViewModel

@Composable
fun MineScreen(

    viewModel: MineViewModel,
    profileFlow: StateFlow<ProfileData?>,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenLogin: () -> Unit,
    onOpenLocalMusic: () -> Unit,
    onOpenRecentPlay: () -> Unit,
    onOpenSubList: () -> Unit,
    onOpenCloudDisk: () -> Unit,
    onOpenMsgCenter: () -> Unit,
    onOpenPlaylistDetail: (PlaylistData, Boolean, Boolean) -> Unit,
    onMessage: (String) -> Unit = {},
    onOpenImport: () -> Unit = {},
) {
    val profile by profileFlow.collectAsState()
    val likePlaylist by viewModel.likePlaylist.collectAsState()
    val myPlaylists by viewModel.myPlaylists.collectAsState()
    val collectPlaylists by viewModel.collectPlaylists.collectAsState()
    val playlistFailed by viewModel.playlistFailed.collectAsState()
    val scope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistDesc by remember { mutableStateOf("") }
    var newPlaylistPrivate by remember { mutableStateOf(false) }
    var manageMode by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<PlaylistData?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<PlaylistData?>(null) }
    var unsubTarget by remember { mutableStateOf<PlaylistData?>(null) }
    var showListenRank by remember { mutableStateOf(false) }
    var showLevelDialog by remember { mutableStateOf(false) }
    var userLevel by remember { mutableStateOf<UserLevelInfo?>(null) }

    LaunchedEffect(profile?.userId) {
        if (profile != null) {
            userLevel = kotlin.runCatching { MineExtraNet.getUserLevel() }.getOrNull()?.data
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("新建歌单") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        singleLine = true,
                        placeholder = { Text("歌单名", fontSize = 13.sp) }
                    )
                    OutlinedTextField(
                        value = newPlaylistDesc,
                        onValueChange = { newPlaylistDesc = it },
                        singleLine = true,
                        placeholder = { Text("描述(可选)", fontSize = 13.sp) }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "隐私歌单",
                            color = AppThemeColor.TextH2,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = newPlaylistPrivate,
                            onCheckedChange = { newPlaylistPrivate = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        scope.launch {
                            val res = PlaylistManageNet.createPlaylist(
                                newPlaylistName,
                                privacy = if (newPlaylistPrivate) 10 else 0,
                                description = newPlaylistDesc
                            )
                            if (res.code == 200) {
                                viewModel.updatePlaylist()
                                showCreateDialog = false
                                newPlaylistName = ""
                                newPlaylistDesc = ""
                                newPlaylistPrivate = false
                            } else {
                                onMessage("创建失败")
                            }
                        }
                    }
                }) { Text("创建", color = AppThemeColor.ThemeColor) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("取消") }
            }
        )
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("重命名歌单") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    placeholder = { Text("歌单名", fontSize = 13.sp) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) {
                        scope.launch {
                            val res = PlaylistManageNet.updatePlaylistName(target.id, renameText)
                            if (res.code == 200) {
                                viewModel.updatePlaylist()
                                renameTarget = null
                            } else {
                                onMessage(res.msg ?: res.message ?: "重命名失败")
                            }
                        }
                    }
                }) { Text("确认", color = AppThemeColor.ThemeColor) }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("取消") }
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除歌单") },
            text = { Text("确认删除歌单「${target.name}」？删除后无法恢复") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val res = PlaylistManageNet.deletePlaylist(target.id)
                        if (res.code == 200) {
                            viewModel.updatePlaylist()
                        } else {
                            onMessage(res.msg ?: res.message ?: "删除失败")
                        }
                        deleteTarget = null
                    }
                }) { Text("删除", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            }
        )
    }

    unsubTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { unsubTarget = null },
            title = { Text("取消收藏") },
            text = { Text("确认取消收藏歌单「${target.name}」？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val res = viewModel.removeCollect(target.id)
                        if (!res.isSuccess()) {
                            onMessage(res.msg ?: "取消收藏失败")
                        }
                        unsubTarget = null
                    }
                }) { Text("确认", color = AppThemeColor.ThemeColor) }
            },
            dismissButton = {
                TextButton(onClick = { unsubTarget = null }) { Text("取消") }
            }
        )
    }

    if (showLevelDialog) {
        AlertDialog(
            onDismissRequest = { showLevelDialog = false },
            title = { Text("我的等级") },
            text = {
                val level = userLevel
                if (level == null || level.level <= 0) {
                    Text("登录后可查看等级信息")
                } else {
                    Column {
                        Text(
                            text = "当前等级 Lv.${level.level}",
                            color = AppThemeColor.TextH1,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "成长进度 ${(level.progress * 100).toInt()}%",
                            color = AppThemeColor.TextH2,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        if (level.nextPlayCount > level.nowPlayCount) {
                            Text(
                                text = "再播放 ${level.nextPlayCount - level.nowPlayCount} 次可升级",
                                color = AppThemeColor.TextH2,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (level.nextLoginCount > level.nowLoginCount) {
                            Text(
                                text = "再登录 ${level.nextLoginCount - level.nowLoginCount} 天可升级",
                                color = AppThemeColor.TextH2,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (level.info.isNotBlank()) {
                            Text(
                                text = level.info,
                                color = AppThemeColor.TextH2,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLevelDialog = false }) { Text("知道了") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColor.Background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        item {
            MineTitleBar(
                onOpenDrawer = onOpenDrawer,
                onOpenSearch = onOpenSearch
            )
        }

        item {
            ProfileHeader(
                isLoggedIn = profile != null,
                avatarUrl = profile?.avatarUrl ?: "",
                nickname = profile?.nickname ?: "",
                signature = profile?.signature ?: "",
                level = userLevel?.level,
                onOpenLogin = onOpenLogin
            )
        }

        item {
            MenuCardList(
                onOpenRecentPlay = onOpenRecentPlay,
                onOpenSubList = onOpenSubList,
                onOpenCloudDisk = onOpenCloudDisk,
                onOpenMsgCenter = onOpenMsgCenter,
                onOpenLocalMusic = onOpenLocalMusic,
                onOpenListenRank = { showListenRank = true },
                onOpenLevel = { showLevelDialog = true }
            )
        }

        val like = likePlaylist
        if (profile != null) {
            item {
                SectionTitle("我喜欢的音乐")
            }
            item {
                if (like != null) {
                    LikePlaylistCard(
                        playlist = like,
                        onClick = { onOpenPlaylistDetail(like, false, true) }
                    )
                } else {
                    PlaylistEmptyHint(
                        failed = playlistFailed,
                        onRetry = { viewModel.updatePlaylist() }
                    )
                }
            }
        }

        if (profile != null) {
            item {
                SectionTitle(
                    "创建歌单(${myPlaylists.size})",
                    onAddClick = { showCreateDialog = true },
                    onImportClick = onOpenImport,
                    onManageClick = if (myPlaylists.isNotEmpty()) {
                        { manageMode = !manageMode }
                    } else null,
                    manageActive = manageMode
                )
            }
            item {
                if (myPlaylists.isNotEmpty()) {
                    PlaylistRow(
                        playlists = myPlaylists,
                        manageMode = manageMode,
                        onItemClick = { playlist ->
                            if (manageMode) {
                                renameText = playlist.name
                                renameTarget = playlist
                            } else {
                                onOpenPlaylistDetail(playlist, true, false)
                            }
                        },
                        onDeleteClick = { playlist -> deleteTarget = playlist }
                    )
                } else {
                    PlaylistEmptyHint(
                        failed = playlistFailed,
                        onRetry = { viewModel.updatePlaylist() }
                    )
                }
            }
        }

        if (profile != null) {
            item {
                SectionTitle("我的收藏(${collectPlaylists.size})")
            }
            item {
                if (collectPlaylists.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppThemeColor.Card)
                    ) {
                        collectPlaylists.forEach { playlist ->
                            PlaylistItemRow(
                                playlist = playlist,
                                onUnsubscribeClick = { unsubTarget = playlist }
                            ) {
                                onOpenPlaylistDetail(playlist, false, false)
                            }
                        }
                    }
                } else {
                    PlaylistEmptyHint(
                        failed = playlistFailed,
                        onRetry = { viewModel.updatePlaylist() }
                    )
                }
            }
        }
        }

        if (showListenRank) {
            ListenRankScreen(onBack = { showListenRank = false })
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
            .statusBarsPadding()
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
    isLoggedIn: Boolean,
    avatarUrl: String,
    nickname: String,
    signature: String,
    level: Int?,
    onOpenLogin: () -> Unit
) {
    if (isLoggedIn) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppThemeColor.Card)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverImage(
                url = avatarUrl,
                cornerRadius = 32.dp,
                modifier = Modifier.size(64.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = nickname,
                        color = AppThemeColor.TextH1,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (level != null && level > 0) {
                        Text(
                            text = "Lv.$level",
                            color = AppThemeColor.ThemeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .background(
                                    AppThemeColor.ThemeColor.copy(alpha = 0.12f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = signature.ifBlank { "编辑签名，展示我的音乐态度" },
                    color = AppThemeColor.TextH2,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppThemeColor.Card)
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AppThemeColor.ThemeColor)
                    .clickable(onClick = onOpenLogin)
                    .padding(horizontal = 32.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "立即登录",
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun MenuCardList(
    onOpenRecentPlay: () -> Unit,
    onOpenSubList: () -> Unit,
    onOpenCloudDisk: () -> Unit,
    onOpenMsgCenter: () -> Unit,
    onOpenLocalMusic: () -> Unit,
    onOpenListenRank: () -> Unit,
    onOpenLevel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppThemeColor.Card)
    ) {
        MenuCardRow(
            icon = Icons.Filled.Leaderboard,
            title = "听歌排行",
            subtitle = "",
            onClick = onOpenListenRank
        )
        MenuCardRow(
            icon = Icons.Filled.EmojiEvents,
            title = "我的等级",
            subtitle = "",
            onClick = onOpenLevel
        )
        MenuCardRow(
            icon = Icons.Filled.Star,
            title = "我的收藏",
            subtitle = "歌手 / 专辑 / MV",
            onClick = onOpenSubList
        )
        MenuCardRow(
            icon = Icons.Filled.CloudQueue,
            title = "音乐云盘",
            subtitle = "",
            onClick = onOpenCloudDisk
        )
        MenuCardRow(
            icon = Icons.Filled.Chat,
            title = "我的消息",
            subtitle = "",
            onClick = onOpenMsgCenter
        )
        MenuCardRow(
            icon = Icons.Filled.History,
            title = "最近播放",
            subtitle = "",
            onClick = onOpenRecentPlay
        )
        MenuCardRow(
            icon = Icons.Filled.LibraryMusic,
            title = "本地/下载音乐",
            subtitle = "",
            onClick = onOpenLocalMusic
        )
    }
}

@Composable
private fun MenuCardRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AppThemeColor.ThemeColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            color = AppThemeColor.TextH1,
            fontSize = 15.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "进入",
            tint = AppThemeColor.TextH2,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LikePlaylistCard(
    playlist: PlaylistData,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFEC4141).copy(alpha = 0.85f),
                        Color(0xFF9C4DB8).copy(alpha = 0.85f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "我喜欢的音乐",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = playlist.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.trackCount}首 · 点击播放全部",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "进入",
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PlaylistEmptyHint(
    failed: Boolean,
    onRetry: () -> Unit
) {
    Text(
        text = if (failed) "歌单加载失败，点击重试" else "暂无歌单",
        color = if (failed) AppThemeColor.ThemeColor else AppThemeColor.TextH2,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onRetry)
    )
}

@Composable
private fun SectionTitle(
    title: String,
    onAddClick: (() -> Unit)? = null,
    onImportClick: (() -> Unit)? = null,
    onManageClick: (() -> Unit)? = null,
    manageActive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = AppThemeColor.TextH1,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onAddClick != null) {
                Text(
                    text = "+ 新建",
                    color = AppThemeColor.ThemeColor,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onAddClick)
                )
            }
            if (onImportClick != null) {
                Text(
                    text = "导入",
                    color = AppThemeColor.ThemeColor,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onImportClick)
                )
            }
            if (onManageClick != null) {
                Text(
                    text = if (manageActive) "完成" else "管理",
                    color = AppThemeColor.ThemeColor,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onManageClick)
                )
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlists: List<PlaylistData>,
    onItemClick: (PlaylistData) -> Unit,
    manageMode: Boolean = false,
    onDeleteClick: ((PlaylistData) -> Unit)? = null
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(playlists, key = { it.id }) { playlist ->
            Box {
                PlaylistCard(
                    playlist = playlist,
                    modifier = Modifier.width(120.dp),
                    onClick = { onItemClick(playlist) }
                )
                if (manageMode && onDeleteClick != null) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "删除",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { onDeleteClick(playlist) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistItemRow(
    playlist: PlaylistData,
    onUnsubscribeClick: (() -> Unit)? = null,
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
            url = playlist.coverImgUrl,
            cornerRadius = 8.dp,
            modifier = Modifier.size(48.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = playlist.name,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.trackCount}首",
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (onUnsubscribeClick != null) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "取消收藏",
                tint = AppThemeColor.TextH2,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onUnsubscribeClick)
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "进入",
            tint = AppThemeColor.TextH2,
            modifier = Modifier.size(20.dp)
        )
    }
}

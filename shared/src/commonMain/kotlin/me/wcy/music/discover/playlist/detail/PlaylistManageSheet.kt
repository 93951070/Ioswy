package me.wcy.music.discover.playlist.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.component.SongRow
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.search.bean.SearchResultData
import me.wcy.music.shared.net.ListenDataNet
import me.wcy.music.shared.net.PlaylistManageNet
import me.wcy.music.shared.net.SharedNet

@Serializable
private data class CloudSearchData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("result")
    val result: SearchResultData = SearchResultData(),
)

@Serializable
private data class RecentContactData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("data")
    val data: RecentContactInner = RecentContactInner(),
)

@Serializable
private data class RecentContactInner(
    @SerialName("follow")
    val follow: List<ProfileData> = emptyList(),
)

/** 轻量 toast：文案非空时浮层显示 2 秒，由调用方置空结束 */
@Composable
fun ToastHost(message: String?, modifier: Modifier = Modifier) {
    if (message != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 64.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .background(Color(0xCC333333), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/** toast 自动消失：message 变化时 2 秒后回调清空 */
@Composable
fun AutoClearToast(message: String?, onClear: () -> Unit) {
    LaunchedEffect(message) {
        if (message != null) {
            delay(2000)
            onClear()
        }
    }
}

/**
 * 歌单管理 Sheet（创建者本人）：编辑歌单信息、添加歌曲。
 * 网络操作在内部直接调 PlaylistManageNet / SharedNet，成功后回调 onUpdated 由外层刷新详情。
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PlaylistManageSheet(
    playlist: PlaylistData,
    onDismiss: () -> Unit,
    onUpdated: () -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }
    AutoClearToast(message = toast, onClear = { toast = null })

    if (showEdit) {
        PlaylistEditDialog(
            playlist = playlist,
            onDismiss = { showEdit = false },
            onSaved = { err ->
                showEdit = false
                if (err == null) {
                    onUpdated()
                    toast = "已保存"
                } else {
                    toast = err
                }
            }
        )
    }
    if (showAdd) {
        PlaylistAddSongDialog(
            playlist = playlist,
            onDismiss = { showAdd = false },
            onAdded = { err ->
                if (err == null) {
                    onUpdated()
                    toast = "已添加"
                } else {
                    toast = err
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            ManageActionRow(
                icon = { Icon(Icons.Filled.Edit, "编辑歌单信息", tint = AppThemeColor.TextH1) },
                text = "编辑歌单信息",
                onClick = { showEdit = true }
            )
            ManageActionRow(
                icon = { Icon(Icons.Filled.LibraryAdd, "添加歌曲", tint = AppThemeColor.TextH1) },
                text = "添加歌曲",
                onClick = { showAdd = true }
            )
        }
    }
}

@Composable
private fun ManageActionRow(
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Text(
            text = text,
            color = AppThemeColor.TextH1,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun PlaylistEditDialog(
    playlist: PlaylistData,
    onDismiss: () -> Unit,
    onSaved: (String?) -> Unit
) {
    var name by remember { mutableStateOf(playlist.name) }
    var desc by remember { mutableStateOf(playlist.description) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑歌单信息") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("描述") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving && name.isNotBlank(),
                onClick = {
                    saving = true
                    scope.launch {
                        var err: String? = null
                        if (name != playlist.name) {
                            val res = runCatching {
                                PlaylistManageNet.updatePlaylistName(playlist.id, name)
                            }.getOrNull()
                            if (res == null || res.code != 200) {
                                err = res?.msg ?: res?.message ?: "保存失败"
                            }
                        }
                        if (err == null && desc != playlist.description) {
                            val res = runCatching {
                                PlaylistManageNet.updatePlaylistDesc(playlist.id, desc)
                            }.getOrNull()
                            if (res == null || res.code != 200) {
                                err = res?.msg ?: res?.message ?: "保存失败"
                            }
                        }
                        saving = false
                        onSaved(err)
                    }
                }
            ) { Text(if (saving) "保存中" else "保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/** 添加歌曲：cloudsearch 搜索，点击结果调 addTracks */
@Composable
private fun PlaylistAddSongDialog(
    playlist: PlaylistData,
    onDismiss: () -> Unit,
    onAdded: (String?) -> Unit
) {
    var keywords by remember { mutableStateOf("") }
    var songs by remember { mutableStateOf(listOf<me.wcy.music.common.bean.SongData>()) }
    var addedIds by remember { mutableStateOf(setOf<Long>()) }
    var toast by remember { mutableStateOf<String?>(null) }
    AutoClearToast(message = toast, onClear = { toast = null })
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加歌曲到「${playlist.name}」") },
        text = {
            Column {
                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    placeholder = { Text("搜索歌曲") },
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "搜索",
                            tint = AppThemeColor.TextH2,
                            modifier = Modifier.clickable {
                                val kw = keywords.trim()
                                if (kw.isNotEmpty()) {
                                    scope.launch {
                                        val res = runCatching {
                                            SharedJson.decodeBean<CloudSearchData>(
                                                SharedNet.post(
                                                    "cloudsearch",
                                                    params = listOf("keywords" to kw, "type" to "1")
                                                )
                                            )
                                        }.getOrNull()
                                        songs = res?.result?.songs ?: emptyList()
                                        if (res == null || res.code != 200) {
                                            toast = "搜索失败"
                                        }
                                    }
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    items(songs) { song ->
                        val added = song.id in addedIds
                        SongRow(
                            song = song,
                            onClick = {
                                if (!added) {
                                    scope.launch {
                                        val res = runCatching {
                                            PlaylistManageNet.addTracks(playlist.id, "${song.id}")
                                        }.getOrNull()
                                        if (res != null && res.code == 200) {
                                            addedIds = addedIds + song.id
                                            onAdded(null)
                                        } else {
                                            toast = res?.msg ?: res?.message ?: "添加失败"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.alpha(if (added) 0.4f else 1f)
                        )
                    }
                }
                if (toast != null) {
                    Text(
                        text = toast ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完成") }
        }
    )
}

/**
 * 私信分享歌单：拉最近联系人（msg/recentcontact，data.follow 结构），
 * 选中后 ListenDataNet.sendPlaylist。
 */
@Composable
fun PlaylistShareDialog(
    playlist: PlaylistData,
    onDismiss: () -> Unit,
    onShared: (String?) -> Unit = {}
) {
    var contacts by remember { mutableStateOf(listOf<ProfileData>()) }
    var loading by remember { mutableStateOf(true) }
    var toast by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val res = runCatching {
            SharedJson.decodeBean<RecentContactData>(
                SharedNet.post("msg/recentcontact")
            )
        }.getOrNull()
        loading = false
        contacts = res?.data?.follow ?: emptyList()
        if (res == null || res.code != 200) {
            toast = "获取联系人失败"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("私信分享「${playlist.name}」") },
        text = {
            Column {
                if (loading) {
                    Text("加载中...", color = AppThemeColor.TextH2, fontSize = 14.sp)
                } else if (contacts.isEmpty()) {
                    Text("暂无最近联系人", color = AppThemeColor.TextH2, fontSize = 14.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                    ) {
                        items(contacts) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            val res = runCatching {
                                                ListenDataNet.sendPlaylist(
                                                    userIds = listOf(contact.userId),
                                                    id = playlist.id
                                                )
                                            }.getOrNull()
                                            if (res != null && res.code == 200) {
                                                onShared(null)
                                                onDismiss()
                                            } else {
                                                toast = res?.message ?: "分享失败"
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoverImage(
                                    url = contact.avatarUrl,
                                    cornerRadius = 20.dp,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = contact.nickname,
                                    color = AppThemeColor.TextH1,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                }
                if (toast != null) {
                    Text(
                        text = toast ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

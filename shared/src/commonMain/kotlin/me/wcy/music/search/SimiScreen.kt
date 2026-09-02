package me.wcy.music.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.SongRow
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.ui.PlayAllRow
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.shared.net.SearchMoreNet
import me.wcy.music.shared.net.apiCall

/**
 * 相似歌曲页（/simi/song，需要登录 cookie，匿名返回空数据）。
 */
@Composable
fun SimiSongsScreen(
    songId: Long,
    onBack: () -> Unit = {},
    onPlaySongs: (songs: List<SongData>, index: Int) -> Unit = { _, _ -> }
) {
    var songs by remember { mutableStateOf<List<SongData>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(songId) {
        val res = apiCall { SearchMoreNet.getSimiSongs(songId) }
        if (res.isSuccessWithData()) {
            songs = res.getDataOrThrow().songs
        }
        loaded = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(title = "相似歌曲", onBack = onBack)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (loaded && songs.isNotEmpty()) {
                item {
                    PlayAllRow(
                        songCount = songs.size,
                        onPlayAll = { onPlaySongs(songs, 0) }
                    )
                }
            }
            itemsIndexed(songs) { index, song ->
                SongRow(
                    song = song,
                    onClick = { onPlaySongs(songs, index) }
                )
            }
            if (loaded && songs.isEmpty()) {
                item { SimiEmptyHint() }
            }
        }
    }
}

/**
 * 相似歌单页（/simi/playlist，包含该歌的歌单，需要登录 cookie）。
 */
@Composable
fun SimiPlaylistsScreen(
    songId: Long,
    onBack: () -> Unit = {},
    onOpenPlaylistDetail: (id: Long) -> Unit = {}
) {
    var playlists by remember { mutableStateOf<List<PlaylistData>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(songId) {
        val res = apiCall { SearchMoreNet.getSimiPlaylists(songId) }
        if (res.isSuccessWithData()) {
            playlists = res.getDataOrThrow().playlists
        }
        loaded = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(title = "包含这首歌的歌单", onBack = onBack)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(playlists) { _, item ->
                SearchPlaylistRow(item) { onOpenPlaylistDetail(item.id) }
            }
            if (loaded && playlists.isEmpty()) {
                item { SimiEmptyHint() }
            }
        }
    }
}

@Composable
private fun SimiEmptyHint() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无数据，登录后可查看更多推荐",
            color = AppThemeColor.TextH2,
            fontSize = 14.sp
        )
    }
}

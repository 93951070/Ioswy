package me.wcy.music.compose.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.SongRow
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.net.apiCall

@Composable
fun RecommendSongScreen(
    onBack: () -> Unit,
    onPlayAll: (songs: List<SongData>) -> Unit,
    onPlaySong: (songs: List<SongData>, position: Int) -> Unit
) {
    var songList by remember { mutableStateOf<List<SongData>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val res = apiCall { DiscoverNet.getRecommendSongs() }
        if (res.isSuccessWithData()) {
            songList = res.getDataOrThrow().dailySongs
        }
        loaded = true
    }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            TitleBar(title = "每日推荐", onBack = onBack)
        }
        if (loaded && songList.isNotEmpty()) {
            item {
                PlayAllRow(
                    songCount = songList.size,
                    onPlayAll = { onPlayAll(songList) }
                )
            }
        }
        itemsIndexed(songList) { index, song ->
            SongRow(
                song = song,
                index = index + 1,
                onClick = { onPlaySong(songList, index) }
            )
        }
    }
}

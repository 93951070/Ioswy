package me.wcy.music.daily

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import me.wcy.music.compose.ui.PlayAllRow
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.net.apiCall

/**
 * 每日推荐页（/recommend/songs 的 data.dailySongs，元素结构同 SongData，无需登录）。
 */
@Composable
fun DailyRecommendScreen(
    onBack: () -> Unit = {},
    onPlaySongs: (songs: List<SongData>, index: Int) -> Unit = { _, _ -> }
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

    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(title = "每日推荐", onBack = onBack)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (loaded && songList.isNotEmpty()) {
                item {
                    PlayAllRow(
                        songCount = songList.size,
                        onPlayAll = { onPlaySongs(songList, 0) }
                    )
                }
            }
            itemsIndexed(songList) { index, song ->
                SongRow(
                    song = song,
                    index = index + 1,
                    onClick = { onPlaySongs(songList, index) }
                )
            }
        }
    }
}

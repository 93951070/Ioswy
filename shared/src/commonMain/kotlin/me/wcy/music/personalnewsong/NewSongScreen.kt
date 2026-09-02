package me.wcy.music.personalnewsong

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.SongRow
import me.wcy.music.compose.ui.PlayAllRow
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.personalnewsong.viewmodel.NewSongViewModel

@Composable
fun NewSongScreen(
    viewModel: NewSongViewModel,
    onBack: () -> Unit,
    onPlaySongs: (List<SongData>, Int) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadSongs()
    }

    val songs by viewModel.songs.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TitleBar(title = "新歌速递", onBack = onBack)
        }
        if (songs.isNotEmpty()) {
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
                index = index + 1,
                onClick = { onPlaySongs(songs, index) }
            )
        }
    }
}

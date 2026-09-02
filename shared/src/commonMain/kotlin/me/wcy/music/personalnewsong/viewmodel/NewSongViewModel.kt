package me.wcy.music.personalnewsong.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.common.bean.SongData
import me.wcy.music.mv.MvNet

class NewSongViewModel : ViewModel() {

    private val _songs = MutableStateFlow<List<SongData>>(emptyList())
    val songs: StateFlow<List<SongData>> = _songs.asStateFlow()

    suspend fun loadSongs(): Boolean {
        val data = kotlin.runCatching {
            MvNet.getPersonalizedNewsong(limit = 30)
        }.getOrNull() ?: return false
        if (data.code != 200) return false
        _songs.value = data.result.map { it.song }
        return true
    }
}

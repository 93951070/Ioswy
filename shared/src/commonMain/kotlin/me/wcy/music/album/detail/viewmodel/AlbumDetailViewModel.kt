package me.wcy.music.album.detail.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.album.AlbumNet
import me.wcy.music.album.bean.AlbumInfo
import me.wcy.music.common.bean.SongData

class AlbumDetailViewModel : ViewModel() {

    private val _album = MutableStateFlow<AlbumInfo?>(null)
    val album: StateFlow<AlbumInfo?> = _album.asStateFlow()

    private val _songList = MutableStateFlow<List<SongData>>(emptyList())
    val songList: StateFlow<List<SongData>> = _songList.asStateFlow()

    private val _isSub = MutableStateFlow(false)
    val isSub: StateFlow<Boolean> = _isSub.asStateFlow()

    private var albumId = 0L

    fun init(id: Long) {
        albumId = id
    }

    suspend fun loadData(): Boolean {
        val data = kotlin.runCatching {
            AlbumNet.getAlbumDetail(albumId)
        }.getOrNull() ?: return false
        if (data.code != 200) return false
        _album.value = data.album
        _songList.value = data.songs
        return true
    }

    suspend fun collect(): Boolean {
        val album = _album.value ?: return false
        val res = kotlin.runCatching {
            AlbumNet.subAlbum(album.id, if (_isSub.value) 2 else 1)
        }.getOrNull() ?: return false
        if (res.code != 200) return false
        _isSub.value = !_isSub.value
        return true
    }
}

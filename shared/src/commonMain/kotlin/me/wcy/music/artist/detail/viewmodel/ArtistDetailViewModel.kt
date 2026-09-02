package me.wcy.music.artist.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.artist.ArtistNet
import me.wcy.music.artist.bean.ArtistAlbumItem
import me.wcy.music.artist.bean.ArtistDescData
import me.wcy.music.artist.bean.ArtistInfo
import me.wcy.music.artist.bean.MvItem
import me.wcy.music.common.bean.SongData

class ArtistDetailViewModel : ViewModel() {

    private var artistId: Long = 0

    private val _artist = MutableStateFlow<ArtistInfo?>(null)
    val artist: StateFlow<ArtistInfo?> = _artist.asStateFlow()

    private val _hotSongs = MutableStateFlow<List<SongData>>(emptyList())
    val hotSongs: StateFlow<List<SongData>> = _hotSongs.asStateFlow()

    private val _albums = MutableStateFlow<List<ArtistAlbumItem>>(emptyList())
    val albums: StateFlow<List<ArtistAlbumItem>> = _albums.asStateFlow()

    private val _mvs = MutableStateFlow<List<MvItem>>(emptyList())
    val mvs: StateFlow<List<MvItem>> = _mvs.asStateFlow()

    private val _desc = MutableStateFlow<ArtistDescData?>(null)
    val desc: StateFlow<ArtistDescData?> = _desc.asStateFlow()

    private val _subscribed = MutableStateFlow(false)
    val subscribed: StateFlow<Boolean> = _subscribed.asStateFlow()

    fun init(id: Long) {
        artistId = id
    }

    suspend fun loadDetail(): Boolean {
        val detail = runCatching {
            ArtistNet.getArtistDetail(artistId)
        }.getOrNull() ?: return false
        if (detail.code != 200) return false
        _artist.value = detail.artist
        _subscribed.value = detail.artist.subbed
        var songs = detail.hotSongs
        if (songs.isEmpty()) {
            songs = runCatching { ArtistNet.getArtistTopSong(artistId) }.getOrNull()
                ?.takeIf { it.code == 200 }?.songs ?: emptyList()
        }
        _hotSongs.value = songs
        return true
    }

    suspend fun loadAlbums(): Boolean {
        val data = runCatching {
            ArtistNet.getArtistAlbum(artistId)
        }.getOrNull() ?: return false
        if (data.code != 200) return false
        _albums.value = data.hotAlbums
        return true
    }

    suspend fun loadMvs(): Boolean {
        val data = runCatching {
            ArtistNet.getArtistMv(artistId)
        }.getOrNull() ?: return false
        if (data.code != 200) return false
        _mvs.value = data.mvs
        return true
    }

    suspend fun loadDesc(): Boolean {
        val data = runCatching {
            ArtistNet.getArtistDesc(artistId)
        }.getOrNull() ?: return false
        if (data.code != 200) return false
        _desc.value = data
        return true
    }

    /**
     * 收藏/取消收藏，本地切换态
     */
    fun toggleSub() {
        viewModelScope.launch {
            val t = if (_subscribed.value) 2 else 1
            val result = runCatching { ArtistNet.subArtist(artistId, t) }.getOrNull()
                ?: return@launch
            if (result.code == 200) {
                _subscribed.value = t == 1
            }
        }
    }
}

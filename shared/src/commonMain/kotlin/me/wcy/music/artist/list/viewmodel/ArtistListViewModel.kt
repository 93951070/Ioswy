package me.wcy.music.artist.list.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.artist.ArtistCategories
import me.wcy.music.artist.ArtistCategory
import me.wcy.music.artist.ArtistNet
import me.wcy.music.artist.bean.ArtistInfo

class ArtistListViewModel : ViewModel() {

    val categories: List<ArtistCategory> = ArtistCategories.ALL

    private val _artists = MutableStateFlow<List<ArtistInfo>>(emptyList())
    val artists: StateFlow<List<ArtistInfo>> = _artists.asStateFlow()

    suspend fun loadArtists(category: ArtistCategory): Boolean {
        val data = runCatching {
            ArtistNet.getArtistList(category.type, category.area)
        }.getOrNull() ?: return false
        if (data.code != 200) return false
        _artists.value = data.artists
        return true
    }
}

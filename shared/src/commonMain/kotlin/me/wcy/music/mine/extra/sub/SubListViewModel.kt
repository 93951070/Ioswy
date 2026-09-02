package me.wcy.music.mine.extra.sub

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.mine.extra.MineExtraNet
import me.wcy.music.mine.extra.bean.AlbumSubItem
import me.wcy.music.mine.extra.bean.ArtistSubItem
import me.wcy.music.mine.extra.bean.MvSubItem

class SubListViewModel : ViewModel() {
    private val _artists = MutableStateFlow<List<ArtistSubItem>>(emptyList())
    val artists: StateFlow<List<ArtistSubItem>> = _artists.asStateFlow()

    private val _albums = MutableStateFlow<List<AlbumSubItem>>(emptyList())
    val albums: StateFlow<List<AlbumSubItem>> = _albums.asStateFlow()

    private val _mvs = MutableStateFlow<List<MvSubItem>>(emptyList())
    val mvs: StateFlow<List<MvSubItem>> = _mvs.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private val loadedTabs = mutableSetOf<Int>()

    suspend fun load(tab: Int) {
        if (tab in loadedTabs) return
        loadedTabs.add(tab)
        when (tab) {
            0 -> runCatching { MineExtraNet.getArtistSublist() }.getOrNull()
                ?.takeIf { it.code == 200 }?.let { _artists.value = it.data }
            1 -> runCatching { MineExtraNet.getAlbumSublist() }.getOrNull()
                ?.takeIf { it.code == 200 }?.let { _albums.value = it.data }
            else -> runCatching { MineExtraNet.getMvSublist() }.getOrNull()
                ?.takeIf { it.code == 200 }?.let { _mvs.value = it.data }
        }
        _loaded.value = true
    }
}

package me.wcy.music.album.new.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.album.AlbumNet
import me.wcy.music.album.bean.NewAlbumItem

class AlbumNewViewModel : ViewModel() {

    val areaList: List<Pair<String, String>> = listOf(
        "全部" to "ALL",
        "华语" to "ZH",
        "欧美" to "EA",
        "韩国" to "KR",
        "日本" to "JP"
    )

    private val _albums = MutableStateFlow<List<NewAlbumItem>>(emptyList())
    val albums: StateFlow<List<NewAlbumItem>> = _albums.asStateFlow()

    suspend fun loadAlbums(area: String): Boolean {
        val data = kotlin.runCatching {
            AlbumNet.getNewAlbumList(area = area)
        }.getOrNull() ?: return false
        if (data.code != 200) return false
        _albums.value = data.products
        return true
    }
}

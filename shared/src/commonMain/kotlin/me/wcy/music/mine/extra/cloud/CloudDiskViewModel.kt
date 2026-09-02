package me.wcy.music.mine.extra.cloud

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.mine.extra.MineExtraNet
import me.wcy.music.mine.extra.bean.CloudItem

class CloudDiskViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<CloudItem>>(emptyList())
    val items: StateFlow<List<CloudItem>> = _items.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    suspend fun load() {
        val data = runCatching {
            MineExtraNet.getUserCloud(limit = 1000)
        }.getOrNull()
        if (data != null && data.code == 200) {
            _items.value = data.data
        }
        _loaded.value = true
    }

    suspend fun delete(item: CloudItem): Boolean {
        val id = if (item.simpleSong.id > 0) item.simpleSong.id else item.songId
        if (id <= 0) return false
        val res = runCatching { MineExtraNet.delCloudSong(id) }.getOrNull() ?: return false
        if (res.code == 200) {
            _items.value = _items.value.filterNot { it == item }
            return true
        }
        return false
    }
}

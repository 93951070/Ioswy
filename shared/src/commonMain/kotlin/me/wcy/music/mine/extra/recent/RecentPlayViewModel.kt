package me.wcy.music.mine.extra.recent

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.mine.extra.MineExtraNet
import me.wcy.music.mine.extra.bean.RecordItem

class RecentPlayViewModel : ViewModel() {
    var uid: Long = 0

    private val _items = MutableStateFlow<List<RecordItem>>(emptyList())
    val items: StateFlow<List<RecordItem>> = _items.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private var weekData: List<RecordItem>? = null
    private var allData: List<RecordItem>? = null

    suspend fun load(type: String) {
        val cached = if (type == "0") weekData else allData
        if (cached != null) {
            _items.value = cached
            return
        }
        val data = runCatching {
            MineExtraNet.getRecentPlaySongs(uid, type)
        }.getOrNull()
        if (data == null || data.code != 200) {
            _loaded.value = true
            return
        }
        val list = if (type == "0") data.weekData else data.allData
        if (type == "0") weekData = list else allData = list
        _items.value = list
        _loaded.value = true
    }
}

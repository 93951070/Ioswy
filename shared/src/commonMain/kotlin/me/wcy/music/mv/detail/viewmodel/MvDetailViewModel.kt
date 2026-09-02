package me.wcy.music.mv.detail.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.mv.MvNet
import me.wcy.music.mv.bean.MvItem

class MvDetailViewModel : ViewModel() {

    private val _mv = MutableStateFlow<MvItem?>(null)
    val mv: StateFlow<MvItem?> = _mv.asStateFlow()

    private val _mvUrl = MutableStateFlow("")
    val mvUrl: StateFlow<String> = _mvUrl.asStateFlow()

    private val _isSub = MutableStateFlow(false)
    val isSub: StateFlow<Boolean> = _isSub.asStateFlow()

    private var mvid = 0L

    fun init(id: Long) {
        mvid = id
    }

    suspend fun loadData(): Boolean {
        val data = kotlin.runCatching {
            MvNet.getMvDetail(mvid)
        }.getOrNull() ?: return false
        if (data.code != 200) return false
        _mv.value = data.data
        _isSub.value = data.subed
        val urlData = kotlin.runCatching {
            MvNet.getMvUrl(mvid)
        }.getOrNull()
        if (urlData != null && urlData.code == 200) {
            _mvUrl.value = urlData.data.url
        }
        return true
    }

    suspend fun collect(): Boolean {
        val mv = _mv.value ?: return false
        val res = kotlin.runCatching {
            MvNet.subMv(mv.id, if (_isSub.value) 2 else 1)
        }.getOrNull() ?: return false
        if (res.code != 200) return false
        _isSub.value = !_isSub.value
        return true
    }
}

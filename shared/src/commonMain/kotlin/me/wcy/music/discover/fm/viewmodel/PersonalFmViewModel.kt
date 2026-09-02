package me.wcy.music.discover.fm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.SongData
import me.wcy.music.shared.net.DiscoverNet

class PersonalFmViewModel : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadFm(onLoaded: (List<SongData>) -> Unit) {
        if (_loading.value) return
        _loading.value = true
        viewModelScope.launch {
            val res = kotlin.runCatching {
                DiscoverNet.getPersonalFm()
            }
            val data = res.getOrNull()
            if (data?.code == 200 && data.data.isNotEmpty()) {
                onLoaded(data.data)
                _error.value = null
            } else if (data != null) {
                _error.value = "FM 加载失败 code=${data.code}${if (data.code == 301) "，请重新登录" else ""}"
            } else {
                _error.value = "FM 加载异常: ${res.exceptionOrNull()?.message}"
            }
            _loading.value = false
        }
    }
}

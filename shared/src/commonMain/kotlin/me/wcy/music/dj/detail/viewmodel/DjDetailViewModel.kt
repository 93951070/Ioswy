package me.wcy.music.dj.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.dj.DjNet
import me.wcy.music.dj.bean.DjProgramData
import me.wcy.music.dj.bean.DjRadioData

class DjDetailViewModel : ViewModel() {

    private val _radio = MutableStateFlow<DjRadioData?>(null)
    val radio: StateFlow<DjRadioData?> = _radio.asStateFlow()

    private val _programs = MutableStateFlow<List<DjProgramData>>(emptyList())
    val programs: StateFlow<List<DjProgramData>> = _programs.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var loadedRid = 0L
    private var loadMoreJob: Job? = null

    suspend fun load(rid: Long) {
        if (loadedRid == rid && _radio.value != null) return
        loadedRid = rid
        _radio.value = null
        _programs.value = emptyList()
        _hasMore.value = false
        kotlin.runCatching { DjNet.getDjDetail(rid) }.onSuccess { data ->
            if (data.code == 200) {
                _radio.value = data.data
            }
        }
        loadPrograms(rid, 0)
    }

    fun loadMore() {
        if (loadMoreJob?.isActive == true || !_hasMore.value) return
        val rid = loadedRid
        loadMoreJob = viewModelScope.launch {
            loadPrograms(rid, _programs.value.size)
        }
    }

    suspend fun subDj(rid: Long, t: Int): Boolean {
        return kotlin.runCatching { DjNet.subDj(rid, t) }.getOrNull()?.isSuccess() == true
    }

    private suspend fun loadPrograms(rid: Long, offset: Int) {
        kotlin.runCatching {
            DjNet.getDjProgram(rid, limit = PAGE_SIZE, offset = offset)
        }.onSuccess { data ->
            if (data.code != 200) return@onSuccess
            _programs.value = if (offset == 0) {
                data.programs
            } else {
                _programs.value + data.programs
            }
            _hasMore.value = if (data.count > 0) {
                _programs.value.size < data.count
            } else {
                data.programs.isNotEmpty()
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 30
    }
}

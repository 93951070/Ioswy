package me.wcy.music.dj.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.dj.DjNet
import me.wcy.music.dj.bean.DjCatelistData
import me.wcy.music.dj.bean.DjRadioData

class DjRecommendViewModel : ViewModel() {

    private val _recommended = MutableStateFlow<List<DjRadioData>>(emptyList())
    val recommended: StateFlow<List<DjRadioData>> = _recommended.asStateFlow()

    private val _categories = MutableStateFlow<List<DjCatelistData.Category>>(emptyList())
    val categories: StateFlow<List<DjCatelistData.Category>> = _categories.asStateFlow()

    private val _hotRadios = MutableStateFlow<List<DjRadioData>>(emptyList())
    val hotRadios: StateFlow<List<DjRadioData>> = _hotRadios.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var started = false
    private var loadMoreJob: Job? = null

    suspend fun load() {
        if (started) return
        started = true
        kotlin.runCatching { DjNet.getDjCatelist() }.getOrNull()?.let {
            _categories.value = it.categories
        }
        kotlin.runCatching { DjNet.getDjRecommend() }.getOrNull()?.let {
            _recommended.value = it.djRadios
        }
        loadHot(0)
    }

    fun loadMore() {
        if (loadMoreJob?.isActive == true || !_hasMore.value) return
        loadMoreJob = viewModelScope.launch {
            loadHot(_hotRadios.value.size)
        }
    }

    private suspend fun loadHot(offset: Int) {
        kotlin.runCatching {
            DjNet.getDjHot(limit = PAGE_SIZE, offset = offset)
        }.onSuccess { data ->
            _hotRadios.value = if (offset == 0) {
                data.djRadios
            } else {
                _hotRadios.value + data.djRadios
            }
            _hasMore.value = data.hasMore
        }
    }

    companion object {
        private const val PAGE_SIZE = 30
    }
}

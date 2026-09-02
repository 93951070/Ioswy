package me.wcy.music.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.search.bean.HotSearchWord
import me.wcy.music.shared.net.SearchNet

/**
 * 搜索历史存储抽象，由宿主平台实现（Android 侧基于 SharedPreferences）。
 */
interface SearchHistoryStore {
    fun loadHistory(): List<String>
    fun saveHistory(keywords: List<String>)
}

/**
 * Created by wangchenyan.top on 2023/9/20.
 */
class SearchViewModel(
    private val historyStore: SearchHistoryStore
) : ViewModel() {
    private val _keywords = MutableStateFlow("")
    val keywords: StateFlow<String> = _keywords.asStateFlow()

    private val _historyKeywords = MutableStateFlow(historyStore.loadHistory())
    val historyKeywords: StateFlow<List<String>> = _historyKeywords.asStateFlow()

    private val _showResult = MutableStateFlow(false)
    val showResult: StateFlow<Boolean> = _showResult.asStateFlow()

    private val _hotWords = MutableStateFlow<List<HotSearchWord>>(emptyList())
    val hotWords: StateFlow<List<HotSearchWord>> = _hotWords.asStateFlow()

    init {
        viewModelScope.launch {
            kotlin.runCatching {
                SearchNet.getHotSearch()
            }.onSuccess {
                if (it.code == 200) {
                    _hotWords.value = it.data
                }
            }
        }
    }

    fun search(keywords: String) {
        if (keywords.isEmpty()) {
            return
        }
        _keywords.value = keywords
        _showResult.value = true

        val list = _historyKeywords.value.toMutableList()
        list.remove(keywords)
        list.add(0, keywords)
        val realList = list.take(SEARCH_HISTORY_COUNT)
        _historyKeywords.value = realList
        viewModelScope.launch {
            historyStore.saveHistory(realList)
        }
    }

    fun showHistory() {
        _showResult.value = false
    }

    private companion object {
        const val SEARCH_HISTORY_COUNT = 20
    }
}

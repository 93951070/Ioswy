package me.wcy.music.discover.playlist.square.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.shared.net.PlaylistApi

class PlaylistSquareViewModel : ViewModel() {
    data class CategoryGroup(val name: String, val tags: List<String>)

    private val _tagList = MutableStateFlow<List<String>>(emptyList())
    val tagList: StateFlow<List<String>> = _tagList.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryGroup>>(emptyList())
    val categories: StateFlow<List<CategoryGroup>> = _categories.asStateFlow()

    suspend fun loadTagList(): Boolean {
        val data = kotlin.runCatching {
            PlaylistApi.getCatlist()
        }.getOrNull() ?: return false
        if (data.code != 200) return false
        _tagList.value =
            (listOf("全部") + data.sub.filter { it.hot }.sortedBy { it.name }.map { it.name })
        _categories.value = data.categories.entries
            .sortedBy { it.key.toIntOrNull() ?: Int.MAX_VALUE }
            .associate { it.toPair() }
            .mapNotNull { (index, name) ->
            val category = index.toIntOrNull() ?: return@mapNotNull null
            val tags = data.sub.filter { it.category == category }.map { it.name }
            if (tags.isEmpty()) null else CategoryGroup(name, tags)
        }
        return true
    }
}

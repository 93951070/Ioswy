package me.wcy.music.mv.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.mv.MvNet
import me.wcy.music.mv.bean.MvItem

class MvListViewModel : ViewModel() {

    val areaList: List<Pair<String, String>> = listOf(
        "全部" to "",
        "内地" to "内地",
        "港台" to "港台",
        "欧美" to "欧美",
        "日本" to "日本",
        "韩国" to "韩国"
    )

    val typeList: List<Pair<String, String>> = listOf(
        "全部" to "",
        "官方版" to "官方版",
        "原生" to "原生",
        "现场版" to "现场版",
        "网易出品" to "网易出品"
    )

    private val _mvs = MutableStateFlow<List<MvItem>>(emptyList())
    val mvs: StateFlow<List<MvItem>> = _mvs.asStateFlow()

    suspend fun loadMvs(area: String, type: String): Boolean {
        val data = kotlin.runCatching {
            MvNet.getMvAll(area = area, type = type, limit = 50)
        }.getOrNull() ?: return false
        if (data.code != 200) return false
        _mvs.value = data.data
        return true
    }
}

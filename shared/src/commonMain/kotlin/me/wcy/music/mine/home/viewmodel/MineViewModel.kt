package me.wcy.music.mine.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.shared.net.MineNet
import me.wcy.music.shared.net.NetResult
import me.wcy.music.shared.net.apiCall

/**
 * Created by wangchenyan.top on 2023/9/28.
 */
class MineViewModel(
    private val profileFlow: StateFlow<ProfileData?>,
    private val readPlaylistCache: suspend () -> List<PlaylistData>? = { null },
    private val writePlaylistCache: suspend (List<PlaylistData>) -> Unit = {}
) : ViewModel() {
    private val _likePlaylist = MutableStateFlow<PlaylistData?>(null)
    val likePlaylist = _likePlaylist.asStateFlow()
    private val _myPlaylists = MutableStateFlow<List<PlaylistData>>(emptyList())
    val myPlaylists = _myPlaylists.asStateFlow()
    private val _collectPlaylists = MutableStateFlow<List<PlaylistData>>(emptyList())
    val collectPlaylists = _collectPlaylists.asStateFlow()

    private var updateJob: Job? = null

    init {
        viewModelScope.launch {
            profileFlow.collectLatest { profile ->
                if (profile != null) {
                    updatePlaylist(profile.userId)
                } else {
                    _likePlaylist.value = null
                    _myPlaylists.value = emptyList()
                    _collectPlaylists.value = emptyList()
                }
            }
        }
    }

    fun updatePlaylistFromCache() {
        viewModelScope.launch {
            val profile = profileFlow.value ?: return@launch
            val cacheList = readPlaylistCache() ?: return@launch
            notifyPlaylist(profile.userId, cacheList)
        }
    }

    fun updatePlaylist() {
        val uid = profileFlow.value?.userId ?: return
        updatePlaylist(uid)
    }

    private fun updatePlaylist(uid: Long) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            val res = kotlin.runCatching {
                MineNet.getUserPlaylist(uid)
            }
            val data = res.getOrNull()
            if (data != null && data.code == 200) {
                notifyPlaylist(uid, data.playlists)
                writePlaylistCache(data.playlists)
            }
        }
    }

    private fun notifyPlaylist(uid: Long, list: List<PlaylistData>) {
        val mineList = list.filter { it.userId == uid }
        _likePlaylist.value = mineList.firstOrNull()
        _myPlaylists.value = mineList.takeLast((mineList.size - 1).coerceAtLeast(0))
        _collectPlaylists.value = list.filter { it.userId != uid }
    }

    suspend fun removeCollect(id: Long): NetResult<Unit> {
        val res = apiCall { MineNet.collectPlaylist(id, 2) }
        return if (res.isSuccess()) {
            _collectPlaylists.value = _collectPlaylists.value.filter { it.id != id }
            NetResult(code = 200)
        } else {
            NetResult(code = res.code, msg = res.msg)
        }
    }
}

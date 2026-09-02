package me.wcy.music.mine.extra.msg

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.wcy.music.mine.extra.MineExtraNet
import me.wcy.music.mine.extra.bean.MsgItem

class MsgCenterViewModel : ViewModel() {
    data class MsgUiState(
        val privateMsgs: List<MsgItem> = emptyList(),
        val commentMsgs: List<MsgItem> = emptyList(),
        val noticeMsgs: List<MsgItem> = emptyList(),
        val loadedTabs: Set<Int> = emptySet()
    )

    private val _state = MutableStateFlow(MsgUiState())
    val state: StateFlow<MsgUiState> = _state.asStateFlow()

    suspend fun load(tab: Int) {
        val current = _state.value
        if (tab in current.loadedTabs) return
        val data = when (tab) {
            0 -> runCatching { MineExtraNet.getPrivateMsg() }.getOrNull()
            1 -> runCatching { MineExtraNet.getCommentMsg() }.getOrNull()
            else -> runCatching { MineExtraNet.getNoticeMsg() }.getOrNull()
        }
        val msgs = data?.takeIf { it.code == 200 }?.msgs ?: emptyList()
        _state.value = when (tab) {
            0 -> current.copy(privateMsgs = msgs, loadedTabs = current.loadedTabs + tab)
            1 -> current.copy(commentMsgs = msgs, loadedTabs = current.loadedTabs + tab)
            else -> current.copy(noticeMsgs = msgs, loadedTabs = current.loadedTabs + tab)
        }
    }
}

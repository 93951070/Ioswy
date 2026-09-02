package me.wcy.music.discover.comment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.discover.comment.bean.CommentItem
import me.wcy.music.discover.comment.bean.CommentOpData
import me.wcy.music.shared.net.CommentExtraNet
import me.wcy.music.shared.net.SharedNet

/**
 * 楼中楼（回复列表）ViewModel。DiscoverNet.addComment 无 commentId 参数（t=2 回复必需），
 * 发送回复直接走 SharedNet.post("comment", ...)。
 */
class CommentFloorViewModel : ViewModel() {

    private val _ownerComment = MutableStateFlow<CommentItem?>(null)
    val ownerComment: StateFlow<CommentItem?> = _ownerComment.asStateFlow()

    private val _bestComments = MutableStateFlow<List<CommentItem>>(emptyList())
    val bestComments: StateFlow<List<CommentItem>> = _bestComments.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentItem>>(emptyList())
    val comments: StateFlow<List<CommentItem>> = _comments.asStateFlow()

    private val _totalCount = MutableStateFlow(0L)
    val totalCount: StateFlow<Long> = _totalCount.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private var resourceId = 0L
    private var resourceType = 0
    private var parentCommentId = 0L

    /** comment/floor 的翻页游标（最后一条回复的时间戳） */
    private var time: Long? = null
    private var hasMore = true
    private var requestGen = 0

    fun load(id: Long, type: Int, parentCommentId: Long) {
        if (this.parentCommentId == parentCommentId && _comments.value.isNotEmpty()) return
        resourceId = id
        resourceType = type
        this.parentCommentId = parentCommentId
        requestGen++
        time = null
        hasMore = true
        _ownerComment.value = null
        _bestComments.value = emptyList()
        _comments.value = emptyList()
        _totalCount.value = 0
        fetch()
    }

    fun loadMore() {
        if (parentCommentId <= 0 || _loading.value || !hasMore) return
        fetch()
    }

    private fun fetch() {
        if (_loading.value) return
        _loading.value = true
        val gen = requestGen
        val cur = time
        viewModelScope.launch {
            kotlin.runCatching {
                CommentExtraNet.getCommentFloor(parentCommentId, resourceId, resourceType, PAGE_SIZE, cur)
            }.onSuccess {
                if (gen == requestGen && it.code == 200) {
                    val data = it.data
                    if (data != null) {
                        if (cur == null) {
                            _ownerComment.value = data.ownerComment
                            _bestComments.value = data.bestComments
                        }
                        _comments.value =
                            (_comments.value + data.comments).distinctBy { c -> c.commentId }
                        _totalCount.value = data.totalCount
                        time = data.time.takeIf { t -> t > 0 }
                        hasMore = data.hasMore && time != null
                    }
                }
            }
            if (gen == requestGen) {
                _loading.value = false
            }
        }
    }

    /** 回复楼主评论：addComment t=2 + commentId */
    fun send(content: String, onResult: (Boolean, String?) -> Unit) {
        if (resourceId <= 0 || parentCommentId <= 0 || content.isBlank()) return
        viewModelScope.launch {
            val res = kotlin.runCatching {
                SharedNet.post(
                    "comment",
                    params = listOf(
                        "id" to resourceId,
                        "type" to resourceType,
                        "t" to 2,
                        "commentId" to parentCommentId,
                        "content" to content.trim()
                    )
                )
            }
            val data = res.getOrNull()?.let { body ->
                kotlin.runCatching { SharedJson.decodeBean<CommentOpData>(body) }.getOrNull()
            }
            if (data?.code == 200) {
                val reply = data.comment
                if (reply != null) {
                    _comments.value = (listOf(reply) + _comments.value).distinctBy { c -> c.commentId }
                }
                _totalCount.value += 1
                onResult(true, null)
            } else {
                onResult(false, data?.msg ?: res.exceptionOrNull()?.message)
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}

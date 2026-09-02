package me.wcy.music.discover.comment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.discover.comment.EmptyMyCommentStore
import me.wcy.music.discover.comment.MyCommentStore
import me.wcy.music.discover.comment.bean.CommentItem
import me.wcy.music.shared.net.CommentExtraNet
import me.wcy.music.shared.net.DiscoverNet

class CommentViewModel(
    private val myCommentStore: MyCommentStore = EmptyMyCommentStore
) : ViewModel() {

    private val _total = MutableStateFlow(0L)
    val total: StateFlow<Long> = _total.asStateFlow()

    private val _hotComments = MutableStateFlow<List<CommentItem>>(emptyList())
    val hotComments: StateFlow<List<CommentItem>> = _hotComments.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentItem>>(emptyList())
    val comments: StateFlow<List<CommentItem>> = _comments.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _sortType = MutableStateFlow(SORT_RECOMMEND)
    val sortType: StateFlow<Int> = _sortType.asStateFlow()

    private var resourceId = 0L
    private var pageNo = 1

    /** comment/new 的翻页游标（时间戳/热度标记字符串），响应返回后原样回传 */
    private var cursor: String? = null
    private var hasMore = true

    /** 请求代际号：切换资源/排序后丢弃旧请求的迟到响应 */
    private var requestGen = 0

    /** 评论资源类型：0 歌曲 / 1 MV / 2 歌单 / 4 电台 / 5 专辑（comment 接口 type 参数） */
    private var resourceType: Int = 0

    fun init(id: Long, source: String = "music") {
        resourceType = when (source) {
            "dj" -> 4
            "mv" -> 1
            "album" -> 5
            "playlist" -> 2
            else -> 0
        }
        if (resourceId == id && _comments.value.isNotEmpty()) return
        resourceId = id
        resetPaging()
    }

    /** 切换排序（1 推荐 / 2 热度 / 3 时间），重置列表后从第一页重拉 */
    fun setSortType(type: Int) {
        if (_sortType.value == type) return
        _sortType.value = type
        resetPaging()
    }

    private fun resetPaging() {
        requestGen++
        _loading.value = false
        pageNo = 1
        cursor = null
        hasMore = true
        _total.value = 0
        _hotComments.value = emptyList()
        // 本地已发评论直接作为最新评论头部，避免上游收录延迟导致"发完重进就没了"
        _comments.value = myCommentStore.load(resourceId)
        loadMore()
    }

    fun loadMore() {
        if (resourceId <= 0 || _loading.value || !hasMore) return
        _loading.value = true
        val gen = requestGen
        val page = pageNo
        val cur = cursor
        val sort = _sortType.value
        viewModelScope.launch {
            kotlin.runCatching {
                CommentExtraNet.getCommentNew(resourceId, resourceType, page, PAGE_SIZE, sort, cur)
            }.onSuccess {
                if (gen == requestGen && it.code == 200) {
                    val data = it.data
                    if (data != null) {
                        _total.value = data.totalCount
                        data.hotComments?.takeIf { hot -> hot.isNotEmpty() }?.let { hot ->
                            _hotComments.value = hot
                        }
                        // 服务端列表追加在本地已发评论之后，按 commentId 去重（上游收录后自动合并）
                        _comments.value =
                            (_comments.value + data.comments).distinctBy { c -> c.commentId }
                        cursor = data.cursor.takeIf { s -> s.isNotEmpty() }
                        hasMore = data.hasMore && cursor != null
                        pageNo = page + 1
                    }
                }
            }
            if (gen == requestGen) {
                _loading.value = false
            }
        }
    }

    fun toggleLike(comment: CommentItem, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        if (resourceId <= 0) return
        viewModelScope.launch {
            val target = if (comment.liked) 0 else 1
            val res = kotlin.runCatching {
                DiscoverNet.likeComment(resourceId, comment.commentId, target, resourceType)
            }
            val data = res.getOrNull()
            if (data?.code == 200) {
                val delta = if (target == 1) 1L else -1L
                fun List<CommentItem>.updateLike() = map {
                    if (it.commentId == comment.commentId) {
                        it.copy(liked = target == 1, likedCount = (it.likedCount + delta).coerceAtLeast(0))
                    } else {
                        it
                    }
                }
                _comments.value = _comments.value.updateLike()
                _hotComments.value = _hotComments.value.updateLike()
                onResult(true, null)
            } else {
                onResult(false, data?.msg ?: res.exceptionOrNull()?.message)
            }
        }
    }

    fun send(content: String, onResult: (Boolean, String?) -> Unit) {
        if (resourceId <= 0 || content.isBlank()) return
        viewModelScope.launch {
            val res = kotlin.runCatching {
                DiscoverNet.addComment(resourceId, type = resourceType, content = content.trim())
            }
            val data = res.getOrNull()
            if (data?.code == 200) {
                val newComment = data.comment
                if (newComment != null) {
                    myCommentStore.prepend(resourceId, newComment)
                    _comments.value = listOf(newComment) + _comments.value
                }
                _total.value += 1
                onResult(true, null)
            } else {
                onResult(false, data?.msg ?: res.exceptionOrNull()?.message)
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 30

        /** 排序方式（comment/new sortType）：1 推荐 / 2 热度 / 3 时间 */
        const val SORT_RECOMMEND = 1
        const val SORT_HOT = 2
        const val SORT_TIME = 3
    }
}

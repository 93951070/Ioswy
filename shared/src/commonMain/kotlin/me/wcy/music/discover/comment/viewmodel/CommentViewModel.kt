package me.wcy.music.discover.comment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.discover.comment.EmptyMyCommentStore
import me.wcy.music.discover.comment.MyCommentStore
import me.wcy.music.discover.comment.bean.CommentData
import me.wcy.music.discover.comment.bean.CommentItem
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

    private var songId = 0L
    private var page = 0

    /** 电台评论走 comment/dj，MV 走 comment/mv，歌曲走 comment/music */
    private var fetchComment: suspend (Long, Int, Int) -> CommentData =
        { id, limit, offset -> DiscoverNet.getCommentMusic(id, limit, offset) }

    fun init(id: Long, source: String = "music") {
        fetchComment = when (source) {
            "dj" -> { i: Long, l: Int, o: Int -> DiscoverNet.getCommentDj(i, l, o) }
            "mv" -> { i: Long, l: Int, o: Int -> DiscoverNet.getCommentMv(i, l, o) }
            else -> { i: Long, l: Int, o: Int -> DiscoverNet.getCommentMusic(i, l, o) }
        }
        if (songId == id && _comments.value.isNotEmpty()) return
        songId = id
        page = 0
        _total.value = 0
        _hotComments.value = emptyList()
        // 本地已发评论直接作为最新评论头部，避免上游收录延迟导致"发完重进就没了"
        _comments.value = myCommentStore.load(id)
    }

    fun loadMore() {
        if (songId <= 0 || _loading.value) return
        _loading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                fetchComment(songId, PAGE_SIZE, page * PAGE_SIZE)
            }.onSuccess {
                if (it.code == 200) {
                    _total.value = it.total
                    if (it.hotComments.isNotEmpty()) {
                        _hotComments.value = it.hotComments
                    }
                    // 服务端列表追加在本地已发评论之后，按 commentId 去重（上游收录后自动合并）
                    _comments.value =
                        (_comments.value + it.comments).distinctBy { c -> c.commentId }
                    page++
                }
            }
            _loading.value = false
        }
    }

    fun toggleLike(comment: CommentItem, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        if (songId <= 0) return
        viewModelScope.launch {
            val target = if (comment.liked) 0 else 1
            val res = kotlin.runCatching {
                DiscoverNet.likeComment(songId, comment.commentId, target)
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
        if (songId <= 0 || content.isBlank()) return
        viewModelScope.launch {
            val res = kotlin.runCatching {
                DiscoverNet.addComment(songId, content = content.trim())
            }
            val data = res.getOrNull()
            if (data?.code == 200) {
                val newComment = data.comment
                if (newComment != null) {
                    myCommentStore.prepend(songId, newComment)
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
    }
}

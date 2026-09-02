package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.comment.viewmodel.CommentViewModel
import me.wcy.music.mv.detail.MvPlayerSurface
import me.wcy.music.shared.bean.mvvideo.MvDetailInfoData
import me.wcy.music.shared.bean.mvvideo.VideoCategory
import me.wcy.music.shared.bean.mvvideo.VideoData
import me.wcy.music.shared.net.MvVideoExtraNet
import me.wcy.music.shared.net.NetResult
import me.wcy.music.shared.net.apiCall
import me.wcy.music.shared.util.formatPlayCount

private const val MAX_CATEGORY_TABS = 6
private const val VIDEO_RES = 1080

/**
 * 视频详情内嵌页状态（详情在 VideoScreen 内部切换展示）
 */
internal data class VideoDetailUi(
    val vid: String = "",
    val video: VideoData? = null,
    val playUrl: String = "",
    val info: MvDetailInfoData? = null,
    val related: List<VideoData> = emptyList()
)

class VideoViewModel : ViewModel() {

    private val _categories = MutableStateFlow<List<VideoCategory>>(emptyList())
    val categories: StateFlow<List<VideoCategory>> = _categories.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _timeline = MutableStateFlow<List<VideoData>>(emptyList())
    val timeline: StateFlow<List<VideoData>> = _timeline.asStateFlow()

    private val _timelineHasMore = MutableStateFlow(false)
    val timelineHasMore: StateFlow<Boolean> = _timelineHasMore.asStateFlow()

    private val _groupVideos = MutableStateFlow<List<VideoData>>(emptyList())
    val groupVideos: StateFlow<List<VideoData>> = _groupVideos.asStateFlow()

    private val _groupHasMore = MutableStateFlow(false)
    val groupHasMore: StateFlow<Boolean> = _groupHasMore.asStateFlow()

    private val _showDetail = MutableStateFlow(false)
    val showDetail: StateFlow<Boolean> = _showDetail.asStateFlow()

    private val _detail = MutableStateFlow(VideoDetailUi())
    internal val detail: StateFlow<VideoDetailUi> = _detail.asStateFlow()

    private var started = false
    private var currentGroupId = -1L
    private var timelineLoading = false
    private var groupLoading = false
    private var detailJob: Job? = null

    fun load() {
        if (started) return
        started = true
        viewModelScope.launch {
            // 分类 tab 来源 videoCategoryList，取前 6 个；失败时退回 videoGroupList（同构）
            kotlin.runCatching { MvVideoExtraNet.getVideoCategoryList() }.onSuccess {
                _categories.value = it.data.take(MAX_CATEGORY_TABS)
            }
            if (_categories.value.isEmpty()) {
                kotlin.runCatching { MvVideoExtraNet.getVideoGroupList() }.onSuccess {
                    _categories.value = it.data.take(MAX_CATEGORY_TABS)
                }
            }
            loadTimelinePage(0)
        }
    }

    fun selectTab(index: Int) {
        if (_selectedTab.value == index) return
        _selectedTab.value = index
        if (index > 0) {
            val groupId = _categories.value.getOrNull(index - 1)?.id ?: return
            if (groupId != currentGroupId) {
                currentGroupId = groupId
                _groupVideos.value = emptyList()
                _groupHasMore.value = false
                viewModelScope.launch { loadGroupPage(0) }
            }
        }
    }

    fun loadMoreTimeline() {
        if (timelineLoading || !_timelineHasMore.value) return
        viewModelScope.launch { loadTimelinePage(_timeline.value.size.toLong()) }
    }

    fun loadMoreGroup() {
        if (groupLoading || !_groupHasMore.value) return
        viewModelScope.launch { loadGroupPage(_groupVideos.value.size.toLong()) }
    }

    private suspend fun loadTimelinePage(offset: Long) {
        timelineLoading = true
        try {
            kotlin.runCatching { MvVideoExtraNet.getVideoTimelineRecommend(offset = offset) }
                .onSuccess { data ->
                    val items = data.datas.map { it.data }.filter { it.vid.isNotBlank() }
                    _timeline.value = if (offset == 0L) items else _timeline.value + items
                    _timelineHasMore.value = data.hasmore
                }
        } finally {
            timelineLoading = false
        }
    }

    private suspend fun loadGroupPage(offset: Long) {
        groupLoading = true
        try {
            // video/group 上游对当前账号所有 gid 均返回 datas:null（带 cookie 同样），
            // 空结果时退回推荐流按分组名过滤，保证部分分类 tab 有数据
            val direct = kotlin.runCatching { MvVideoExtraNet.getVideoGroup(currentGroupId, offset) }.getOrNull()
            val directItems = direct?.datas.orEmpty()
            if (directItems.isNotEmpty()) {
                _groupVideos.value = if (offset == 0L) directItems else _groupVideos.value + directItems
                _groupHasMore.value = direct?.hasmore ?: false
                return
            }
            val groupName = _categories.value.firstOrNull { it.id == currentGroupId }?.name.orEmpty()
            val timeline = kotlin.runCatching { MvVideoExtraNet.getVideoTimelineRecommend(offset = offset) }.getOrNull()
            val items = timeline?.datas
                ?.map { it.data }
                ?.filter { it.vid.isNotBlank() && it.videoGroup.any { g -> g.name == groupName } }
                .orEmpty()
            _groupVideos.value = if (offset == 0L) items else _groupVideos.value + items
            _groupHasMore.value = timeline?.hasmore ?: false
        } finally {
            groupLoading = false
        }
    }

    fun openVideo(vid: String) {
        if (vid.isBlank()) return
        _showDetail.value = true
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _detail.value = VideoDetailUi(vid = vid)
            kotlin.runCatching { MvVideoExtraNet.getVideoDetail(vid) }.getOrNull()?.let {
                _detail.value = _detail.value.copy(video = it.data)
            }
            // 播放地址需登录；needPay 或空 url 时展示封面 + 不可播提示
            val urlData = kotlin.runCatching { MvVideoExtraNet.getVideoUrl(vid, VIDEO_RES) }.getOrNull()
            val url = urlData?.urls
                ?.firstOrNull { !it.needPay && it.url.isNotBlank() }
                ?.url.orEmpty()
            _detail.value = _detail.value.copy(playUrl = url)
            apiCall { MvVideoExtraNet.getVideoDetailInfo(vid) }.getDataOrThrowOrNull()?.let {
                _detail.value = _detail.value.copy(info = it)
            }
            // Net 签名为 Long；hex 格式 vid 转 Long 失败时传 0，服务端返回空列表
            apiCall { MvVideoExtraNet.getRelatedVideos(vid.toLongOrNull() ?: 0L) }.getDataOrThrowOrNull()?.let {
                _detail.value = _detail.value.copy(related = it.data)
            }
            // related/allvideo 实测恒返回空数组，退回推荐流排除当前视频后取前 12 条
            if (_detail.value.related.isEmpty()) {
                val fallback = mutableListOf<VideoData>()
                for (offset in listOf(0L, 8L)) {
                    val tl = kotlin.runCatching {
                        MvVideoExtraNet.getVideoTimelineRecommend(offset = offset)
                    }.getOrNull() ?: break
                    fallback += tl.datas.map { it.data }
                        .filter { it.vid.isNotBlank() && it.vid != vid }
                    if (fallback.size >= 12 || !tl.hasmore) break
                }
                if (fallback.isNotEmpty()) {
                    _detail.value = _detail.value.copy(related = fallback.distinctBy { it.vid }.take(12))
                }
            }
        }
    }

    fun closeDetail() {
        _showDetail.value = false
        detailJob?.cancel()
        detailJob = null
        _detail.value = VideoDetailUi()
    }

    private fun <T> NetResult<T>.getDataOrThrowOrNull(): T? {
        return if (isSuccessWithData()) getDataOrThrow() else null
    }
}

@Composable
fun VideoScreen(
    onBack: () -> Unit,
    onMessage: (String) -> Unit = {}
) {
    val viewModel = remember { VideoViewModel() }
    val showDetail by viewModel.showDetail.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    if (showDetail) {
        VideoDetailContent(viewModel, onMessage)
    } else {
        VideoListContent(viewModel, onBack)
    }
}

@Composable
private fun VideoListContent(viewModel: VideoViewModel, onBack: () -> Unit) {
    val categories by viewModel.categories.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val timeline by viewModel.timeline.collectAsState()
    val timelineHasMore by viewModel.timelineHasMore.collectAsState()
    val groupVideos by viewModel.groupVideos.collectAsState()
    val groupHasMore by viewModel.groupHasMore.collectAsState()

    val tabNames = remember(categories) { listOf("推荐") + categories.map { it.name } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColor.Background)
    ) {
        TitleBar(title = "视频", onBack = onBack)
        if (tabNames.size > 1) {
            TextTabRow(
                tabs = tabNames,
                selected = selectedTab,
                onSelect = { viewModel.selectTab(it) }
            )
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (selectedTab == 0) {
                itemsIndexed(timeline) { _, item ->
                    VideoCard(item) { viewModel.openVideo(item.vid) }
                }
                if (timelineHasMore) {
                    item {
                        LaunchedEffect(timeline.size) { viewModel.loadMoreTimeline() }
                        LoadingHint()
                    }
                } else if (timeline.isEmpty()) {
                    item { EmptyHint("暂无推荐视频") }
                }
            } else {
                itemsIndexed(groupVideos) { _, item ->
                    VideoCard(item) { viewModel.openVideo(item.vid) }
                }
                if (groupHasMore) {
                    item {
                        LaunchedEffect(groupVideos.size) { viewModel.loadMoreGroup() }
                        LoadingHint()
                    }
                } else if (groupVideos.isEmpty()) {
                    item { EmptyHint("暂无数据") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoDetailContent(viewModel: VideoViewModel, onMessage: (String) -> Unit) {
    val detail by viewModel.detail.collectAsState()
    val video = detail.video
    val commentViewModel = remember { CommentViewModel() }
    var showCommentSheet by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    if (isFullscreen) {
        // 全屏：页面内布局切换，只渲染播放器 + 退出按钮；iOS 横屏由 MvPlayerSurface 内部 LaunchedEffect 请求
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (detail.playUrl.isNotBlank()) {
                MvPlayerSurface(
                    url = detail.playUrl,
                    isFullscreen = true,
                    onToggleFullscreen = { isFullscreen = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "退出全屏",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(28.dp)
                    .clickable { isFullscreen = false }
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppThemeColor.Background)
        ) {
            item {
                TitleBar(
                    title = video?.title?.takeIf { it.isNotBlank() } ?: "视频详情",
                    onBack = { viewModel.closeDetail() }
                )
            }
            item {
                if (detail.playUrl.isNotBlank()) {
                    MvPlayerSurface(
                        url = detail.playUrl,
                        isFullscreen = false,
                        onToggleFullscreen = { isFullscreen = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .background(Color.Black)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CoverImage(url = video?.coverUrl ?: "", modifier = Modifier.fillMaxSize())
                        Text(text = "视频不可播放", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
            if (video != null) {
                item { VideoDetailInfo(video, detail.info, onOpenComment = { showCommentSheet = true }) }
            }
            if (detail.related.isNotEmpty()) {
                item {
                    Text(
                        text = "相关推荐",
                        color = AppThemeColor.TextH1,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(detail.related) { _, item ->
                            RelatedVideoCard(item) { viewModel.openVideo(item.vid) }
                        }
                    }
                }
            }
        }
    }

    if (showCommentSheet) {
        LaunchedEffect(detail.vid) {
            // 视频评论走 comment/new type=5（R_VI_62_ 资源），支持 hex vid
            commentViewModel.init(detail.vid, source = "video")
            commentViewModel.loadMore()
        }
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            CommentPanel(commentViewModel, onMessage)
        }
    }
}

@Composable
private fun VideoDetailInfo(video: VideoData, info: MvDetailInfoData?, onOpenComment: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(
            text = video.title.ifBlank { video.description },
            color = AppThemeColor.TextH1,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverImage(
                url = video.creator.avatarUrl,
                cornerRadius = 10.dp,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = video.creator.nickname,
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .weight(1f)
            )
            Text(
                text = "播放 ${formatPlayCount(video.playTime)}",
                color = AppThemeColor.TextH2,
                fontSize = 12.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "点赞 ${info?.likedCount ?: video.praisedCount} · " +
                    "收藏 ${info?.subCount ?: video.subscribeCount} · " +
                    "评论 ${info?.commentCount ?: video.commentCount}",
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .background(AppThemeColor.ThemeColor, RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenComment)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(text = "评论", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

/** 相关推荐横向卡片：16:9 封面 + 标题 2 行截断 */
@Composable
private fun RelatedVideoCard(video: VideoData, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick)
    ) {
        CoverImage(
            url = video.coverUrl,
            cornerRadius = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )
        Text(
            text = video.title.ifBlank { video.description },
            color = AppThemeColor.TextH1,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun VideoCard(video: VideoData, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        CoverImage(
            url = video.coverUrl,
            cornerRadius = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )
        Text(
            text = video.title.ifBlank { video.description },
            color = AppThemeColor.TextH1,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverImage(
                url = video.creator.avatarUrl,
                cornerRadius = 9.dp,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = video.creator.nickname,
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .weight(1f)
            )
            Text(
                text = "${formatDuration(video.durationms)} · 播放 ${formatPlayCount(video.playTime)}",
                color = AppThemeColor.TextH2,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TextTabRow(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(tabs.size) { index ->
            val isSelected = index == selected
            Text(
                text = tabs[index],
                color = if (isSelected) AppThemeColor.ThemeColor else AppThemeColor.TextH2,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clickable { onSelect(index) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun LoadingHint() {
    Text(
        text = "加载中…",
        color = AppThemeColor.TextH2,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
            .padding(vertical = 12.dp)
    )
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        color = AppThemeColor.TextH2,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
            .padding(vertical = 32.dp)
    )
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "$min:${if (sec < 10) "0" else ""}$sec"
}

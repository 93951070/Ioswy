package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
            kotlin.runCatching { MvVideoExtraNet.getVideoGroup(currentGroupId, offset) }
                .onSuccess { data ->
                    _groupVideos.value = if (offset == 0L) {
                        data.datas
                    } else {
                        _groupVideos.value + data.datas
                    }
                    _groupHasMore.value = data.hasmore
                }
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
    onBack: () -> Unit
) {
    val viewModel = remember { VideoViewModel() }
    val showDetail by viewModel.showDetail.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    if (showDetail) {
        VideoDetailContent(viewModel)
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
                    item { EmptyHint("暂无数据，可能需要登录") }
                }
            }
        }
    }
}

@Composable
private fun VideoDetailContent(viewModel: VideoViewModel) {
    val detail by viewModel.detail.collectAsState()
    val video = detail.video

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
            item { VideoDetailInfo(video, detail.info) }
        }
        if (detail.related.isNotEmpty()) {
            item {
                Text(
                    text = "相关视频",
                    color = AppThemeColor.TextH1,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(detail.related) { _, item ->
                VideoCard(item) { viewModel.openVideo(item.vid) }
            }
        }
    }
}

@Composable
private fun VideoDetailInfo(video: VideoData, info: MvDetailInfoData?) {
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
        Text(
            text = "点赞 ${info?.likedCount ?: video.praisedCount} · " +
                "收藏 ${info?.subCount ?: video.subscribeCount} · " +
                "评论 ${info?.commentCount ?: video.commentCount}",
            color = AppThemeColor.TextH2,
            fontSize = 12.sp,
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

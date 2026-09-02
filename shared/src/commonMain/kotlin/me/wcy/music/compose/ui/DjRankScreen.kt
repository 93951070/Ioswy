package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.dj.bean.DjRadioData
import me.wcy.music.shared.bean.dj.BroadcastChannel
import me.wcy.music.shared.bean.dj.DjAnchor
import me.wcy.music.shared.bean.dj.DjPaygiftRadio
import me.wcy.music.shared.bean.dj.DjProgramToplistHoursItem
import me.wcy.music.shared.bean.dj.DjToplistRadio
import me.wcy.music.shared.net.DjRadioExtraNet
import me.wcy.music.shared.util.formatPlayCount

// ponytail: 热门电台 cateId 固定 2001（创作|翻唱，实测有数据）；做分类选择时改用 getBroadcastCategoryRegion
private const val HOT_RADIO_CATE_ID = 2001L
private const val PAGE_SIZE = 30

/**
 * 电台榜/播客页 tab
 */
internal enum class DjRankTab(val label: String) {
    RADIO("电台榜"),
    PROGRAM("节目榜"),
    NEWCOMER("新人榜"),
    PAYGIFT("付费精品"),
    HOT("热门电台"),
    CHANNEL("播客")
}

class DjRankViewModel : ViewModel() {

    private val _toplist = MutableStateFlow<List<DjToplistRadio>>(emptyList())
    val toplist: StateFlow<List<DjToplistRadio>> = _toplist.asStateFlow()

    private val _programTop = MutableStateFlow<List<DjProgramToplistHoursItem>>(emptyList())
    val programTop: StateFlow<List<DjProgramToplistHoursItem>> = _programTop.asStateFlow()

    private val _newcomer = MutableStateFlow<List<DjAnchor>>(emptyList())
    val newcomer: StateFlow<List<DjAnchor>> = _newcomer.asStateFlow()

    private val _paygift = MutableStateFlow<List<DjPaygiftRadio>>(emptyList())
    val paygift: StateFlow<List<DjPaygiftRadio>> = _paygift.asStateFlow()

    private val _paygiftHasMore = MutableStateFlow(false)
    val paygiftHasMore: StateFlow<Boolean> = _paygiftHasMore.asStateFlow()

    private val _hotRadios = MutableStateFlow<List<DjRadioData>>(emptyList())
    val hotRadios: StateFlow<List<DjRadioData>> = _hotRadios.asStateFlow()

    private val _hotHasMore = MutableStateFlow(false)
    val hotHasMore: StateFlow<Boolean> = _hotHasMore.asStateFlow()

    private val _channels = MutableStateFlow<List<BroadcastChannel>>(emptyList())
    val channels: StateFlow<List<BroadcastChannel>> = _channels.asStateFlow()

    private val _channelsHasMore = MutableStateFlow(false)
    val channelsHasMore: StateFlow<Boolean> = _channelsHasMore.asStateFlow()

    private val loadedTabs = mutableSetOf<DjRankTab>()
    private var loadingTab: DjRankTab? = null

    internal fun selectTab(tab: DjRankTab) {
        if (tab in loadedTabs || loadingTab == tab) return
        loadingTab = tab
        viewModelScope.launch {
            val result = kotlin.runCatching {
                when (tab) {
                    DjRankTab.RADIO -> {
                        _toplist.value = DjRadioExtraNet.getDjToplist().toplist
                    }
                    DjRankTab.PROGRAM -> {
                        _programTop.value = DjRadioExtraNet.getDjProgramToplistHours(PAGE_SIZE).data.list
                    }
                    DjRankTab.NEWCOMER -> {
                        _newcomer.value = DjRadioExtraNet.getDjToplistNewcomer(PAGE_SIZE).data.list
                    }
                    DjRankTab.PAYGIFT -> loadPaygiftPage(0)
                    DjRankTab.HOT -> loadHotPage(0)
                    DjRankTab.CHANNEL -> loadChannelPage()
                }
            }
            if (result.isSuccess) loadedTabs.add(tab)
            loadingTab = null
        }
    }

    fun loadMorePaygift() {
        if (_paygiftHasMore.value || loadingTab != null) return
        loadingTab = DjRankTab.PAYGIFT
        viewModelScope.launch {
            kotlin.runCatching { loadPaygiftPage(_paygift.value.size) }
            loadingTab = null
        }
    }

    fun loadMoreHot() {
        if (_hotHasMore.value || loadingTab != null) return
        loadingTab = DjRankTab.HOT
        viewModelScope.launch {
            kotlin.runCatching { loadHotPage(_hotRadios.value.size) }
            loadingTab = null
        }
    }

    fun loadMoreChannels() {
        if (_channelsHasMore.value || loadingTab != null) return
        loadingTab = DjRankTab.CHANNEL
        viewModelScope.launch {
            kotlin.runCatching { loadChannelPage() }
            loadingTab = null
        }
    }

    private suspend fun loadPaygiftPage(offset: Int) {
        val data = DjRadioExtraNet.getDjPaygift(limit = PAGE_SIZE, offset = offset).data
        _paygift.value = if (offset == 0) data.list else _paygift.value + data.list
        _paygiftHasMore.value = data.hasMore
    }

    private suspend fun loadHotPage(offset: Int) {
        val data = DjRadioExtraNet.getDjRadioHot(cateId = HOT_RADIO_CATE_ID, limit = PAGE_SIZE, offset = offset)
        _hotRadios.value = if (offset == 0) data.djRadios else _hotRadios.value + data.djRadios
        _hotHasMore.value = data.hasMore
    }

    private suspend fun loadChannelPage() {
        // 翻页用上一页最后一条的 id/score 作为 lastId/score
        val last = _channels.value.lastOrNull()
        val data = DjRadioExtraNet.getBroadcastChannelList(
            limit = PAGE_SIZE,
            lastId = last?.id ?: 0,
            score = last?.score ?: -1
        ).data
        _channels.value = if (last == null) data.list else _channels.value + data.list
        _channelsHasMore.value = data.hasMore
    }
}

@Composable
fun DjRankScreen(
    onBack: () -> Unit,
    onOpenRadio: (Long) -> Unit = {},
    onOpenChannel: (Long) -> Unit = {}
) {
    val viewModel = remember { DjRankViewModel() }
    var selectedTab by remember { mutableStateOf(DjRankTab.RADIO) }

    LaunchedEffect(selectedTab) { viewModel.selectTab(selectedTab) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColor.Background)
    ) {
        TitleBar(title = "电台榜", onBack = onBack)
        TextTabRow(
            tabs = DjRankTab.entries.map { it.label },
            selected = DjRankTab.entries.indexOf(selectedTab),
            onSelect = { selectedTab = DjRankTab.entries[it] }
        )
        when (selectedTab) {
            DjRankTab.RADIO -> RadioToplistTab(viewModel, onOpenRadio)
            DjRankTab.PROGRAM -> ProgramToplistTab(viewModel, onOpenRadio)
            DjRankTab.NEWCOMER -> NewcomerTab(viewModel, onOpenRadio)
            DjRankTab.PAYGIFT -> PaygiftTab(viewModel, onOpenRadio)
            DjRankTab.HOT -> HotRadioTab(viewModel, onOpenRadio)
            DjRankTab.CHANNEL -> ChannelTab(viewModel, onOpenChannel)
        }
    }
}

@Composable
private fun RadioToplistTab(viewModel: DjRankViewModel, onOpenRadio: (Long) -> Unit) {
    val list by viewModel.toplist.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(list) { index, item ->
            RankRow(
                rank = index + 1,
                coverUrl = item.picUrl,
                title = item.name,
                subtitle = item.rcmdText.ifBlank { item.dj.nickname },
                trailing = "热度 ${formatPlayCount(item.score)}",
                onClick = { onOpenRadio(item.id) }
            )
        }
        if (list.isEmpty()) {
            item { EmptyHint("暂无数据") }
        }
    }
}

@Composable
private fun ProgramToplistTab(viewModel: DjRankViewModel, onOpenRadio: (Long) -> Unit) {
    val list by viewModel.programTop.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(list) { _, item ->
            RankRow(
                rank = item.rank,
                coverUrl = item.program.coverUrl,
                title = item.program.name,
                subtitle = "期数 ${item.program.serialNum}",
                trailing = "热度 ${formatPlayCount(item.score)}",
                // 注意：节目榜条目 id 为节目 id 而非电台 id
                onClick = { onOpenRadio(item.program.id) }
            )
        }
        if (list.isEmpty()) {
            item { EmptyHint("暂无数据") }
        }
    }
}

@Composable
private fun NewcomerTab(viewModel: DjRankViewModel, onOpenRadio: (Long) -> Unit) {
    val list by viewModel.newcomer.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(list) { _, item ->
            // 注意：新人榜条目为主播，id 为用户 id
            AnchorRow(rank = item.rank, item = item, onClick = { onOpenRadio(item.id) })
        }
        if (list.isEmpty()) {
            item { EmptyHint("暂无数据") }
        }
    }
}

@Composable
private fun PaygiftTab(viewModel: DjRankViewModel, onOpenRadio: (Long) -> Unit) {
    val list by viewModel.paygift.collectAsState()
    val hasMore by viewModel.paygiftHasMore.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(list) { _, item ->
            val price = item.discountPrice ?: item.originalPrice
            val priceText = if (price % 100 == 0) "¥${price / 100}" else "¥${price / 100.0}"
            RankRow(
                rank = 0,
                coverUrl = item.picUrl,
                title = item.name,
                subtitle = item.rcmdText.ifBlank { item.lastProgramName },
                trailing = "$priceText · ${formatPlayCount(item.subCount)} 订阅",
                onClick = { onOpenRadio(item.id) }
            )
        }
        if (hasMore) {
            item {
                LaunchedEffect(list.size) { viewModel.loadMorePaygift() }
                LoadingHint()
            }
        } else if (list.isEmpty()) {
            item { EmptyHint("暂无数据") }
        }
    }
}

@Composable
private fun HotRadioTab(viewModel: DjRankViewModel, onOpenRadio: (Long) -> Unit) {
    val list by viewModel.hotRadios.collectAsState()
    val hasMore by viewModel.hotHasMore.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(list) { _, item ->
            RankRow(
                rank = 0,
                coverUrl = item.picUrl,
                title = item.name,
                subtitle = item.dj.nickname.ifBlank { item.category },
                trailing = "播放 ${formatPlayCount(item.playCount)}",
                onClick = { onOpenRadio(item.id) }
            )
        }
        if (hasMore) {
            item {
                LaunchedEffect(list.size) { viewModel.loadMoreHot() }
                LoadingHint()
            }
        } else if (list.isEmpty()) {
            item { EmptyHint("暂无数据") }
        }
    }
}

@Composable
private fun ChannelTab(viewModel: DjRankViewModel, onOpenChannel: (Long) -> Unit) {
    val list by viewModel.channels.collectAsState()
    val hasMore by viewModel.channelsHasMore.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(list) { _, item ->
            RankRow(
                rank = 0,
                coverUrl = item.coverUrl,
                title = item.name,
                subtitle = item.regionName,
                trailing = "热度 ${formatPlayCount(item.score.toLong())}",
                onClick = { onOpenChannel(item.id) }
            )
        }
        if (hasMore) {
            item {
                LaunchedEffect(list.size) { viewModel.loadMoreChannels() }
                LoadingHint()
            }
        } else if (list.isEmpty()) {
            item { EmptyHint("暂无数据") }
        }
    }
}

@Composable
private fun AnchorRow(rank: Int, item: DjAnchor, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RankBadge(rank)
        CoverImage(
            url = item.avatarUrl,
            cornerRadius = 24.dp,
            modifier = Modifier.size(48.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        ) {
            Text(
                text = item.nickName,
                color = AppThemeColor.TextH1,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "粉丝 ${formatPlayCount(item.userFollowedCount)}",
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = "热度 ${formatPlayCount(item.score)}",
            color = AppThemeColor.TextH2,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun RankRow(
    rank: Int,
    coverUrl: String,
    title: String,
    subtitle: String,
    trailing: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RankBadge(rank)
        CoverImage(
            url = coverUrl,
            cornerRadius = 6.dp,
            modifier = Modifier.size(48.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        ) {
            Text(
                text = title,
                color = AppThemeColor.TextH1,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = trailing,
            color = AppThemeColor.TextH2,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun RankBadge(rank: Int) {
    if (rank <= 0) {
        Spacer(modifier = Modifier.width(24.dp))
    } else {
        Text(
            text = rank.toString(),
            color = if (rank <= 3) AppThemeColor.ThemeColor else AppThemeColor.TextH2,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp),
            maxLines = 1
        )
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
                    .clip(RoundedCornerShape(6.dp))
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

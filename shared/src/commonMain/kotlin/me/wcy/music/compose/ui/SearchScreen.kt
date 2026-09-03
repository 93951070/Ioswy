package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.component.SongRow
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.search.SearchType
import me.wcy.music.search.SearchTypeTabRow
import me.wcy.music.search.SearchAlbumRow
import me.wcy.music.search.SearchArtistRow
import me.wcy.music.search.SearchDjRow
import me.wcy.music.search.SearchMvRow
import me.wcy.music.search.SearchPlaylistRow
import me.wcy.music.search.SearchUserRow
import me.wcy.music.search.SearchViewModel
import me.wcy.music.search.bean.HotSearchWord
import me.wcy.music.search.bean.SearchMultiResult
import me.wcy.music.shared.net.SearchMoreNet
import me.wcy.music.shared.net.apiCall

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onPlayAll: (songs: List<SongData>) -> Unit,
    onPlaySong: (song: SongData) -> Unit,
    onClickItem: (type: SearchType, id: Long) -> Unit = { _, _ -> }
) {
    var keyword by remember { mutableStateOf("") }
    val keywords by viewModel.keywords.collectAsState()
    val showResult by viewModel.showResult.collectAsState()
    val historyKeywords by viewModel.historyKeywords.collectAsState()
    val hotWords by viewModel.hotWords.collectAsState()
    var selectedType by remember { mutableStateOf(SearchType.SONG) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf(SearchMultiResult()) }

    LaunchedEffect(keywords, selectedType, showResult) {
        if (showResult && keywords.isNotEmpty()) {
            loading = true
            result = SearchMultiResult()
            val res = runCatching {
                SearchMoreNet.searchMulti(selectedType.apiType, keywords, 30, 0)
            }.getOrNull()
            if (res != null && res.code == 200) {
                result = res.result
            }
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchTitleBar(
            keyword = keyword,
            onKeywordChange = { keyword = it },
            onSearch = {
                if (keyword.isNotBlank()) {
                    viewModel.search(keyword.trim())
                }
            },
            onBack = onBack
        )
        if (showResult) {
            SearchTypeTabRow(
                selected = selectedType,
                onSelect = { selectedType = it }
            )
            val isEmpty = when (selectedType) {
                SearchType.SONG -> result.songs.isEmpty()
                SearchType.ARTIST -> result.artists.isEmpty()
                SearchType.ALBUM -> result.albums.isEmpty()
                SearchType.PLAYLIST -> result.playlists.isEmpty()
                SearchType.MV -> result.mvs.isEmpty()
                SearchType.RADIO -> result.djRadios.isEmpty()
                SearchType.USER -> result.userprofiles.isEmpty()
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                when (selectedType) {
                    SearchType.SONG -> {
                        if (result.songs.isNotEmpty()) {
                            item {
                                PlayAllRow(
                                    songCount = result.songs.size,
                                    onPlayAll = { onPlayAll(result.songs) }
                                )
                            }
                            itemsIndexed(result.songs) { _, song ->
                                SongRow(
                                    song = song,
                                    onClick = { onPlaySong(song) }
                                )
                            }
                        }
                    }
                    SearchType.ARTIST -> itemsIndexed(result.artists) { _, item ->
                        SearchArtistRow(item) { onClickItem(SearchType.ARTIST, item.id) }
                    }
                    SearchType.ALBUM -> itemsIndexed(result.albums) { _, item ->
                        SearchAlbumRow(item) { onClickItem(SearchType.ALBUM, item.id) }
                    }
                    SearchType.PLAYLIST -> itemsIndexed(result.playlists) { _, item ->
                        SearchPlaylistRow(item) { onClickItem(SearchType.PLAYLIST, item.id) }
                    }
                    SearchType.MV -> itemsIndexed(result.mvs) { _, item ->
                        SearchMvRow(item) { onClickItem(SearchType.MV, item.id) }
                    }
                    SearchType.RADIO -> itemsIndexed(result.djRadios) { _, item ->
                        SearchDjRow(item) { onClickItem(SearchType.RADIO, item.id) }
                    }
                    SearchType.USER -> itemsIndexed(result.userprofiles) { _, item ->
                        SearchUserRow(item) { onClickItem(SearchType.USER, item.userId) }
                    }
                }
                if (loading) {
                    item { LoadingHint() }
                } else if (isEmpty) {
                    item { EmptyHint("未找到相关结果") }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    HistorySection(
                        history = historyKeywords,
                        onHistoryClick = {
                            keyword = it
                            viewModel.search(it)
                        },
                        onClear = { viewModel.showHistory() }
                    )
                }
                if (hotWords.isNotEmpty()) {
                    item { SectionTitle("热搜榜") }
                    itemsIndexed(hotWords) { index, word ->
                        HotSearchRow(
                            rank = index + 1,
                            word = word,
                            onClick = {
                                keyword = word.searchWord
                                viewModel.search(word.searchWord)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTitleBar(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "返回",
            tint = AppThemeColor.TextH1,
            modifier = Modifier
                .size(32.dp)
                .clickable(onClick = onBack)
        )
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            singleLine = true,
            placeholder = { Text("搜索", fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = "搜索", tint = AppThemeColor.TextH2)
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        TextButton(onClick = onSearch) {
            Text("搜索", color = AppThemeColor.ThemeColor)
        }
    }
}

@Composable
private fun HotSearchRow(
    rank: Int,
    word: HotSearchWord,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rank.toString(),
            color = if (rank <= 3) Color(0xFFEC4141) else AppThemeColor.TextH2,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.searchWord,
                    color = AppThemeColor.TextH1,
                    fontSize = 14.sp
                )
                if (!word.iconUrl.isNullOrEmpty()) {
                    CoverImage(
                        url = word.iconUrl,
                        cornerRadius = 2.dp,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(16.dp)
                    )
                }
            }
            if (word.content.isNotEmpty()) {
                Text(
                    text = word.content,
                    color = AppThemeColor.TextH2,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Text(
            text = formatHotScore(word.score),
            color = AppThemeColor.TextH2,
            fontSize = 11.sp
        )
    }
}

private fun formatHotScore(score: Long): String {
    return if (score >= 10_000) "${score / 10_000}万热度" else "${score}热度"
}

@Composable
private fun HistorySection(
    history: List<String>,
    onHistoryClick: (String) -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("历史搜索", color = AppThemeColor.TextH1, fontSize = 15.sp)
            Text("清空", color = AppThemeColor.TextH2, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onClear))
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(history) { _, item ->
                Box(
                    modifier = Modifier
                        .background(AppThemeColor.ThemeColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .clickable { onHistoryClick(item) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(item, color = AppThemeColor.TextH1, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = AppThemeColor.TextH1,
        fontSize = 17.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun EmptyHint(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Text(text = text, color = AppThemeColor.TextH2, fontSize = 14.sp)
    }
}

@Composable
private fun LoadingHint() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = AppThemeColor.ThemeColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

package me.wcy.music.dj.list

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.dj.bean.DjRadioData
import me.wcy.music.dj.list.viewmodel.DjRecommendViewModel
import me.wcy.music.shared.util.formatPlayCount

private const val ALL_CATEGORY = "全部"

@Composable
fun DjRecommendScreen(
    viewModel: DjRecommendViewModel,
    onBack: () -> Unit,
    onOpenDj: (Long) -> Unit
) {
    val recommended by viewModel.recommended.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val hotRadios by viewModel.hotRadios.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    var selectedCategory by remember { mutableStateOf(ALL_CATEGORY) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    val displayRadios = if (selectedCategory == ALL_CATEGORY) {
        hotRadios
    } else {
        hotRadios.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColor.Background)
    ) {
        item {
            TitleBar(title = "电台", onBack = onBack)
        }
        if (recommended.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recommended.size) { index ->
                        RecommendCard(
                            radio = recommended[index],
                            onClick = { onOpenDj(recommended[index].id) }
                        )
                    }
                }
            }
        }
        if (categories.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(categories.size + 1) { index ->
                        val name = if (index == 0) ALL_CATEGORY else categories[index - 1].name
                        CategoryChip(
                            label = name,
                            selected = name == selectedCategory,
                            onClick = { selectedCategory = name }
                        )
                    }
                }
            }
        }
        item {
            Text(
                text = if (selectedCategory == ALL_CATEGORY) "热门电台" else selectedCategory,
                color = AppThemeColor.TextH1,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        val rows = displayRadios.chunked(3)
        items(rows.size) { rowIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rows[rowIndex].forEach { radio ->
                    HotRadioCell(
                        radio = radio,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenDj(radio.id) }
                    )
                }
                repeat(3 - rows[rowIndex].size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
        if (hasMore) {
            item {
                LaunchedEffect(displayRadios.size) {
                    viewModel.loadMore()
                }
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
        }
    }
}

@Composable
private fun RecommendCard(
    radio: DjRadioData,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(132.dp)
            .background(AppThemeColor.Card, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        CoverImage(
            url = radio.picUrl,
            cornerRadius = 4.dp,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        )
        Text(
            text = radio.name,
            color = AppThemeColor.TextH1,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = "订阅 ${formatPlayCount(radio.subCount)}",
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun HotRadioCell(
    radio: DjRadioData,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        CoverImage(
            url = radio.picUrl,
            cornerRadius = 4.dp,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        )
        Text(
            text = radio.name,
            color = AppThemeColor.TextH1,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
        Text(
            text = "订阅 ${formatPlayCount(radio.subCount)}",
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp)
        )
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(
                if (selected) AppThemeColor.ThemeColor else AppThemeColor.ThemeColor.copy(alpha = 0.1f),
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else AppThemeColor.TextH1,
            fontSize = 13.sp
        )
    }
}

package me.wcy.music.compose.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
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
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.discover.home.viewmodel.DiscoverViewModel
import me.wcy.music.dj.bean.DjRadioData
import me.wcy.music.shared.bean.dj.DjPaygiftRadio
import me.wcy.music.shared.net.DjRadioExtraNet

@Composable
fun PodcastTab(
    viewModel: DiscoverViewModel,
    onOpenDj: () -> Unit,
    onOpenDjRadio: (Long) -> Unit,
    onOpenDjRank: () -> Unit
) {
    val djRadioList by viewModel.djRadioList.collectAsState()
    var hotRadios by remember { mutableStateOf(emptyList<DjRadioData>()) }
    var paygiftRadios by remember { mutableStateOf(emptyList<DjPaygiftRadio>()) }

    LaunchedEffect(Unit) {
        runCatching {
            DjRadioExtraNet.getDjRadioHot(cateId = 2001, limit = 10)
        }.onSuccess {
            hotRadios = it.djRadios
        }
    }
    LaunchedEffect(Unit) {
        runCatching {
            DjRadioExtraNet.getDjPaygift(limit = 6)
        }.onSuccess {
            paygiftRadios = it.data.list
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        if (djRadioList.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "电台精选", onMore = onOpenDj)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(djRadioList, key = { it.id }) { radio ->
                        HomeDjRadioCard(
                            radio = radio,
                            onClick = { onOpenDjRadio(radio.id) }
                        )
                    }
                }
            }
        }

        item {
            ChannelEntryCard(
                title = "电台分类",
                subtitle = "按分类浏览全部电台",
                onClick = onOpenDj
            )
        }

        if (hotRadios.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "热门电台", onMore = null)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(hotRadios, key = { it.id }) { radio ->
                        HomeDjRadioCard(
                            radio = radio,
                            onClick = { onOpenDjRadio(radio.id) }
                        )
                    }
                }
            }
        }

        if (paygiftRadios.isNotEmpty()) {
            item {
                HomeSectionHeader(title = "付费精品", onMore = null)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(paygiftRadios, key = { it.id }) { radio ->
                        PaygiftRadioCard(
                            radio = radio,
                            onClick = { onOpenDjRadio(radio.id) }
                        )
                    }
                }
            }
        }

        item {
            ChannelEntryCard(
                title = "播客频道",
                subtitle = "热门播客频道排行",
                onClick = onOpenDjRank
            )
        }
    }
}

/** 电台卡：封面 + 电台名 + 主播 */
@Composable
private fun HomeDjRadioCard(
    radio: DjRadioData,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        CoverImage(
            url = radio.picUrl,
            cornerRadius = 10.dp,
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = radio.name,
            color = AppThemeColor.TextH1,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
        )
        Text(
            text = radio.dj.nickname,
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}

/** 付费精品电台卡：封面 + 电台名 + 推荐语 */
@Composable
private fun PaygiftRadioCard(
    radio: DjPaygiftRadio,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        CoverImage(
            url = radio.picUrl,
            cornerRadius = 10.dp,
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = radio.name,
            color = AppThemeColor.TextH1,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
        )
        Text(
            text = radio.rcmdText,
            color = AppThemeColor.TextH2,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}

/** 入口卡：图标 + 标题 + 副标题 */
@Composable
private fun ChannelEntryCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppThemeColor.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppThemeColor.ThemeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = title,
                tint = AppThemeColor.ThemeColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = AppThemeColor.TextH2,
            modifier = Modifier.size(20.dp)
        )
    }
}

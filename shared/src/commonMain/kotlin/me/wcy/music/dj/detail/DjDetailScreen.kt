package me.wcy.music.dj.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.dj.bean.DjProgramData
import me.wcy.music.dj.bean.DjRadioData
import me.wcy.music.dj.detail.viewmodel.DjDetailViewModel
import me.wcy.music.dj.formatDuration
import me.wcy.music.shared.util.formatPlayCount

@Composable
fun DjDetailScreen(
    viewModel: DjDetailViewModel,
    rid: Long,
    onBack: () -> Unit,
    onPlaySong: (SongData) -> Unit
) {
    val radio by viewModel.radio.collectAsState()
    val programs by viewModel.programs.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val scope = rememberCoroutineScope()
    var subed by remember { mutableStateOf(false) }

    LaunchedEffect(rid) {
        viewModel.load(rid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColor.Background)
    ) {
        TitleBar(
            title = radio?.name?.takeIf { it.isNotBlank() } ?: "电台",
            onBack = onBack
        )
        val currentRadio = radio
        if (currentRadio == null) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "加载中…",
                    color = AppThemeColor.TextH2,
                    fontSize = 14.sp
                )
            }
            return@Column
        }
        LaunchedEffect(currentRadio.id) {
            subed = currentRadio.subed
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            item {
                RadioHeader(
                    radio = currentRadio,
                    subed = subed,
                    onSubClick = {
                        val t = if (subed) 2 else 1
                        scope.launch {
                            if (viewModel.subDj(currentRadio.id, t)) {
                                subed = t == 1
                            }
                        }
                    }
                )
            }
            item {
                Text(
                    text = "节目列表（${currentRadio.programCount}）",
                    color = AppThemeColor.TextH1,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(programs.size) { index ->
                ProgramRow(
                    program = programs[index],
                    onPlaySong = onPlaySong
                )
            }
            if (hasMore) {
                item {
                    LaunchedEffect(programs.size) {
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
}

@Composable
private fun RadioHeader(
    radio: DjRadioData,
    subed: Boolean,
    onSubClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        CoverImage(
            url = radio.picUrl,
            cornerRadius = 4.dp,
            modifier = Modifier.size(96.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = radio.name,
                color = AppThemeColor.TextH1,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoverImage(
                    url = radio.dj.avatarUrl,
                    cornerRadius = 10.dp,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = radio.dj.nickname,
                    color = AppThemeColor.TextH2,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Text(
                text = "${formatPlayCount(radio.subCount)}人订阅 · 共${radio.programCount}期",
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .background(
                        if (subed) Color.Transparent else AppThemeColor.ThemeColor,
                        RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, AppThemeColor.ThemeColor, RoundedCornerShape(20.dp))
                    .clickable(onClick = onSubClick)
                    .padding(horizontal = 20.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (subed) "已订阅" else "+ 订阅",
                    color = if (subed) AppThemeColor.ThemeColor else Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
    if (radio.desc.isNotBlank()) {
        Text(
            text = radio.desc,
            color = AppThemeColor.TextH2,
            fontSize = 13.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun ProgramRow(
    program: DjProgramData,
    onPlaySong: (SongData) -> Unit
) {
    val song = program.mainSong
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = song.id > 0) { onPlaySong(song) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = program.coverUrl,
            cornerRadius = 4.dp,
            modifier = Modifier.size(48.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = program.name,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "第${program.serialNum}期 · ${formatDuration(song.dt)} · ${formatPlayCount(program.listenerCount)}次收听",
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "播放",
            tint = AppThemeColor.ThemeColor,
            modifier = Modifier
                .size(28.dp)
                .clickable(enabled = song.id > 0) { onPlaySong(song) }
        )
    }
}

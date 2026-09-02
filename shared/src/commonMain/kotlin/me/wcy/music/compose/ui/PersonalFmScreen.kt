package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.flow.StateFlow
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.AppThemeColor

private fun formatMs(milli: Long): String {
    val m = (milli / 60000).toInt()
    val s = (milli / 1000 % 60).toInt()
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}

@Composable
fun PersonalFmScreen(
    currentSong: StateFlow<SongData?>,
    isPlaying: StateFlow<Boolean>,
    playProgress: StateFlow<Long>,
    fmError: StateFlow<String?>,
    onPlayPause: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onNext: () -> Unit,
    isLiked: (Long) -> Boolean,
    onLike: (Long) -> Unit,
    onErrorRetry: () -> Unit,
    onBack: () -> Unit
) {
    val song by currentSong.collectAsState()
    val playing by isPlaying.collectAsState()
    val progress by playProgress.collectAsState()
    val errorMessage by fmError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColor.Background)
    ) {
        TitleBar(title = "私人FM", onBack = onBack)
        val current = song
        if (current == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMessage ?: "加载中…",
                        color = if (errorMessage == null) AppThemeColor.TextH2 else Color(0xFFEC4141),
                        fontSize = 14.sp
                    )
                    if (errorMessage != null) {
                        Text(
                            text = "点击重试",
                            color = AppThemeColor.ThemeColor,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .clickable(onClick = onErrorRetry)
                        )
                    }
                }
            }
            return@Column
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CoverImage(
                url = current.al.getLargeCover(),
                cornerRadius = 12.dp,
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .size(280.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = current.name,
                color = AppThemeColor.TextH1,
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = current.ar.joinToString("/") { it.name },
                color = AppThemeColor.TextH2,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            val duration = current.dt.toFloat().coerceAtLeast(1f)
            var dragValue by remember { mutableStateOf<Float?>(null) }
            val sliderValue = (dragValue ?: progress.toFloat().coerceIn(0f, duration))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMs(sliderValue.toLong()),
                    color = AppThemeColor.TextH2,
                    fontSize = 11.sp
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { dragValue = it },
                    onValueChangeFinished = {
                        dragValue?.let { onSeekTo(it.toInt()) }
                        dragValue = null
                    },
                    valueRange = 0f..duration,
                    colors = SliderDefaults.colors(
                        inactiveTrackColor = AppThemeColor.TextH2.copy(alpha = 0.3f),
                        activeTrackColor = AppThemeColor.ThemeColor,
                        thumbColor = AppThemeColor.ThemeColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(24.dp)
                )
                Text(
                    text = formatMs(duration.toLong()),
                    color = AppThemeColor.TextH2,
                    fontSize = 11.sp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val songId = current.id
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "垃圾桶",
                    tint = AppThemeColor.TextH1,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onNext)
                )
                Icon(
                    imageVector = if (songId > 0 && isLiked(songId)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "喜欢",
                    tint = if (songId > 0 && isLiked(songId)) Color(0xFFF44336) else AppThemeColor.TextH1,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { if (songId > 0) onLike(songId) }
                )
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "播放暂停",
                        tint = AppThemeColor.ThemeColor,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "下一首",
                    tint = AppThemeColor.TextH1,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(onClick = onNext)
                )
            }
        }
    }
}

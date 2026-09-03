package me.wcy.music.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import me.wcy.music.shared.lrc.LrcLine
import me.wcy.music.shared.lrc.findCurrentLrcIndex
import me.wcy.music.shared.lrc.parseLrc
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.compose.theme.Red500
import kotlin.math.roundToInt

/** 桌面歌词全局开关：进程级，任何页面顶层悬浮显示 */
val desktopLyricsOn = androidx.compose.runtime.mutableStateOf(false)

/**
 * 桌面歌词悬浮条：底部胶囊，显示当前歌词行（无歌词时显示「歌名 - 歌手」）。
 * 点歌词进入播放页，点右侧 × 关闭。
 */
@Composable
fun DesktopLyricsBar(
    currentLine: LrcLine?,
    fallbackText: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentLine?.text?.takeIf { it.isNotBlank() } ?: fallbackText,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Box(
            modifier = Modifier
                .padding(start = 10.dp)
                .background(Red500, RoundedCornerShape(10.dp))
                .clickable { desktopLyricsOn.value = false }
                .padding(horizontal = 7.dp, vertical = 1.dp)
        ) {
            Text("×", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** 由完整歌词与播放进度计算当前行 */
fun currentLrcLine(lines: List<LrcLine>, progressMs: Long): LrcLine? {
    if (lines.isEmpty()) return null
    val idx = findCurrentLrcIndex(lines, progressMs)
    return lines.getOrNull(idx)
}

/**
 * 桌面歌词悬浮层：随当前歌曲拉歌词、随播放进度切行。
 * 双端宿主在根布局顶部挂载即可（主 Tab 页显示）。
 */
@Composable
fun DesktopLyricsOverlay(
    engine: me.wcy.music.shared.player.PlayerEngine,
    onOpenPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song by engine.currentSong.collectAsState()
    val progress by engine.playProgress.collectAsState()
    var lrcLines by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(listOf<LrcLine>()) }
    androidx.compose.runtime.LaunchedEffect(song?.id) {
        lrcLines = emptyList()
        val id = song?.id ?: return@LaunchedEffect
        val lrc = runCatching { me.wcy.music.shared.net.DiscoverNet.getLrc(id) }.getOrNull()
        if (lrc?.code == 200 && lrc.lrc.isValid()) lrcLines = parseLrc(lrc.lrc.lyric)
    }
    // 拖动偏移（px），初始居中偏下
    var dragOffset by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.geometry.Offset(0f, 0f)) }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        DesktopLyricsBar(
            currentLine = currentLrcLine(lrcLines, progress),
            fallbackText = song?.let { s -> "${s.name} - ${s.ar.firstOrNull()?.name ?: ""}" } ?: "",
            onTap = onOpenPlaying,
            modifier = Modifier
                .padding(bottom = 90.dp)
                .pointerInput(Unit) {
                    detectDragGestures(onDragEnd = {}) { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                    }
                }
                .offset { androidx.compose.ui.unit.IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
        )
    }
}

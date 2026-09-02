package me.wcy.music.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import me.wcy.music.shared.lrc.LrcLine
import me.wcy.music.shared.lrc.findCurrentLrcIndex
import me.wcy.music.shared.lrc.parseLrc

@Composable
fun LyricsPanel(
    lrcContent: String,
    progressMs: Long,
    label: String,
    modifier: Modifier = Modifier,
    normalColor: Color = Color.White.copy(alpha = 0.45f),
    highlightColor: Color = Color.White,
    onSeek: (Int) -> Unit = {},
    onEmptyTap: () -> Unit = {}
) {
    val entries = remember(lrcContent) { parseLrc(lrcContent) }

    if (entries.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = normalColor,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable(onClick = onEmptyTap)
            )
        }
        return
    }

    val listState = rememberLazyListState()

    // 用户拖动时暂停自动跟随，松手 2.5s 后恢复
    var userDragging by remember { mutableStateOf(false) }
    LaunchedEffect(listState.interactionSource) {
        listState.interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is DragInteraction.Start -> userDragging = true
                is DragInteraction.Stop, is DragInteraction.Cancel -> {
                    delay(2500)
                    userDragging = false
                }
            }
        }
    }

    val currentIndex = findCurrentLrcIndex(entries, progressMs)

    // 当前行变化自动滚动到视口中央（上下 contentPadding = 半屏留白实现居中）
    LaunchedEffect(currentIndex, userDragging) {
        if (!userDragging) {
            listState.animateScrollToItem(index = currentIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 200.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(entries) { index, line ->
            val isCurrent = index == currentIndex
            Text(
                text = line.text,
                color = if (isCurrent) highlightColor else normalColor,
                fontSize = if (isCurrent) 18.sp else 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(vertical = 10.dp)
                    .clickable { onSeek(line.timeMs.toInt()) }
            )
        }
    }
}

package me.wcy.music.compose.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.compose.theme.AppThemeColor

private val DanmakuRed = Color(0xFFEC4141)
private val PillShape = RoundedCornerShape(50)

/**
 * 弹幕条：B 站样式「弹」开关胶囊 + 单行输入框 + 发送按钮，一行排布。
 * dark=true 全屏压视频（半透明白胶囊、白字）；false 竖屏（默认浅色样式，同评论区输入框）。
 * 点发送：内容非空回调 onSend 并清空输入框，失败由父级 toast。
 */
@Composable
fun DanmakuBar(
    danmakuOn: Boolean,
    onToggle: () -> Unit,
    onSend: (String) -> Unit,
    dark: Boolean = true,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val toggleColor = if (danmakuOn) {
        DanmakuRed
    } else if (dark) {
        Color.White.copy(alpha = 0.6f)
    } else {
        AppThemeColor.TextH2
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "弹",
            color = toggleColor,
            fontSize = 12.sp,
            modifier = Modifier
                .border(1.dp, toggleColor, PillShape)
                .clickable(onClick = onToggle)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            placeholder = { Text("发个弹幕见证当下", fontSize = 13.sp) },
            shape = PillShape,
            colors = if (dark) {
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = DanmakuRed,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                )
            } else {
                OutlinedTextFieldDefaults.colors()
            },
            textStyle = TextStyle(fontSize = 13.sp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        Text(
            text = "发送",
            color = if (dark) Color.White else AppThemeColor.ThemeColor,
            fontSize = 14.sp,
            modifier = Modifier
                .clickable {
                    if (text.isNotBlank()) {
                        onSend(text.trim())
                        text = ""
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/** 顶部弹幕区：2-3 行轨道，池子不足时行数随之减少 */
@Composable
fun DanmakuOverlay(danmaku: List<String>, modifier: Modifier = Modifier) {
    val rows = danmaku.chunked(7).take(3)
    Box(modifier = modifier.clipToBounds()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            rows.forEachIndexed { index, row ->
                DanmakuRow(
                    text = row.map { it.take(24) }.joinToString("　　　　"),
                    durationMillis = 16000 + index * 2000,
                    startOffsetMillis = index * 6000
                )
            }
        }
    }
}

/** 单条弹幕轨道：infiniteTransition 驱动文本从右侧匀速平移到左侧循环，各行起始 offset 错开 */
@Composable
fun DanmakuRow(text: String, durationMillis: Int, startOffsetMillis: Int) {
    val transition = rememberInfiniteTransition(label = "danmaku")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            initialStartOffset = StartOffset(startOffsetMillis)
        ),
        label = "danmaku-progress"
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val travel = maxWidth
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            maxLines = 1,
            style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), blurRadius = 2f)),
            modifier = Modifier
                .offset(x = travel)
                .graphicsLayer { translationX = -(travel.toPx() + size.width) * progress }
        )
    }
}

package me.wcy.music.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.compose.theme.AppThemeColor

@Composable
fun PlaylistCard(
    playlist: PlaylistData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onPlayClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box {
            CoverImage(
                url = playlist.getSmallCover(),
                cornerRadius = 10.dp,
                modifier = Modifier.size(120.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = formatPlayCount(playlist.playCount),
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "播放",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clickable(onClick = onPlayClick)
            )
        }
        Text(
            text = playlist.name,
            color = AppThemeColor.TextH1,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
        )
    }
}

private fun formatPlayCount(count: Long): String {
    val yi = 100_000_000L
    val wan = 10_000L
    fun oneDecimal(divisor: Long, unit: String): String {
        val v = count * 10 / divisor
        return if (v % 10 == 0L) "${v / 10}$unit" else "${v / 10}.${v % 10}$unit"
    }
    return when {
        count >= yi -> oneDecimal(yi, "亿")
        count >= wan -> oneDecimal(wan, "万")
        else -> count.toString()
    }
}

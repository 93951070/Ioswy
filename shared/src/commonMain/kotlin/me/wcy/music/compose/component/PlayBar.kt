package me.wcy.music.compose.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.shared.player.PlayerEngine

@Composable
fun PlayBar(
    playerEngine: PlayerEngine,
    onOpenPlaying: () -> Unit,
    onOpenPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by playerEngine.currentSong.collectAsState()
    val isPlaying by playerEngine.isPlaying.collectAsState()
    val playProgress by playerEngine.playProgress.collectAsState()
    val buffering by playerEngine.bufferingPercent.collectAsState()

    val song = currentSong ?: return
    // ponytail: PlayState.Preparing 近似为「未播放且在缓冲」，极早期缓冲(0%)会显示为暂停态，升级需引擎暴露 preparing 状态流
    val isPreparing = !isPlaying && buffering > 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(AppThemeColor.PlayBar)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RotatingCover(
            url = song.al.getSmallCover(),
            isPlaying = isPlaying,
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onOpenPlaying)
        )

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = song.name,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = true)
            )
            val artist = song.ar.joinToString("/") { it.name }
            if (artist.isNotBlank()) {
                Text(
                    text = " - " + artist,
                    color = AppThemeColor.TextH2,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }

        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = "播放/暂停",
            tint = AppThemeColor.TextH1,
            modifier = Modifier
                .size(32.dp)
                .clickable { playerEngine.playPause() }
        )

        Icon(
            imageVector = Icons.Filled.SkipNext,
            contentDescription = "下一首",
            tint = AppThemeColor.TextH1,
            modifier = Modifier
                .size(32.dp)
                .clickable { playerEngine.next() }
        )

        Icon(
            imageVector = Icons.Filled.List,
            contentDescription = "播放列表",
            tint = AppThemeColor.TextH2,
            modifier = Modifier
                .size(32.dp)
                .clickable(onClick = onOpenPlaylist)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        if (isPreparing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxSize())
        } else {
            val max = song.dt.toInt().coerceAtLeast(1)
            val progress = (playProgress.toInt().toFloat() / max).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = AppThemeColor.ThemeColor
            )
        }
    }
}

@Composable
private fun RotatingCover(
    url: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "cover-rotate")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                if (isPlaying) {
                    rotationZ = rotation
                }
            }
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        CoverImage(
            url = url,
            contentDescription = "播放封面",
            cornerRadius = 21.dp,
            modifier = Modifier
                .size(42.dp)
                .padding(2.dp)
        )
    }
}

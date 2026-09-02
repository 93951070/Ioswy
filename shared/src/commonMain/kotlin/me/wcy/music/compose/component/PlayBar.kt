package me.wcy.music.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.shared.player.PlayerEngine

private val PlayBarShape = RoundedCornerShape(26.dp)

@Composable
fun PlayBar(
    playerEngine: PlayerEngine,
    onOpenPlaying: () -> Unit,
    onOpenPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by playerEngine.currentSong.collectAsState()
    val isPlaying by playerEngine.isPlaying.collectAsState()

    val song = currentSong ?: return
    val barColor = if (isSystemInDarkTheme()) Color(0xFF2A2A2A) else Color(0xFF1E1E1E)

    Row(
        modifier = modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            .fillMaxWidth()
            .height(52.dp)
            .shadow(elevation = 8.dp, shape = PlayBarShape)
            .clip(PlayBarShape)
            .background(barColor)
            .clickable(onClick = onOpenPlaying)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CoverImage(
            url = song.al.getSmallCover(),
            contentDescription = "播放封面",
            cornerRadius = 8.dp,
            modifier = Modifier.size(40.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.name,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val artist = song.ar.joinToString("/") { it.name }
            if (artist.isNotBlank()) {
                Text(
                    text = artist,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = "播放/暂停",
            tint = Color.White,
            modifier = Modifier
                .size(28.dp)
                .clickable { playerEngine.playPause() }
        )

        Icon(
            imageVector = Icons.Filled.SkipNext,
            contentDescription = "下一首",
            tint = Color.White,
            modifier = Modifier
                .size(28.dp)
                .clickable { playerEngine.next() }
        )

        Icon(
            imageVector = Icons.Filled.QueueMusic,
            contentDescription = "播放列表",
            tint = Color.White,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onOpenPlaylist)
        )
    }
}

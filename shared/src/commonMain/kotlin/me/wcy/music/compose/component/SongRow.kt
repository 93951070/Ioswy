package me.wcy.music.compose.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.AppThemeColor

@Composable
fun SongRow(
    song: SongData,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    index: Int = -1
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (index >= 0) {
            Text(
                text = "$index",
                color = AppThemeColor.TextH2,
                fontSize = 14.sp,
                modifier = Modifier.width(32.dp)
            )
        }
        CoverImage(
            url = song.al.getSmallCover(),
            cornerRadius = 6.dp,
            modifier = Modifier.size(48.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = song.name,
                color = AppThemeColor.TextH1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.ar.joinToString("/") { it.name },
                color = AppThemeColor.TextH2,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = formatDuration(song.dt),
            color = AppThemeColor.TextH2,
            fontSize = 13.sp
        )
        if (onMoreClick != null) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "更多",
                tint = AppThemeColor.TextH2,
                modifier = Modifier
                    .size(20.dp)
                    .padding(start = 8.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onMoreClick)
            )
        }
    }
}

private fun formatDuration(milli: Long): String {
    val m = (milli / 60000).toString().padStart(2, '0')
    val s = (milli / 1000 % 60).toString().padStart(2, '0')
    return "$m:$s"
}

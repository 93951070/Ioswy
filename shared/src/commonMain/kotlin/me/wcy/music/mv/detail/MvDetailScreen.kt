package me.wcy.music.mv.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.ui.TitleBar
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.mv.bean.MvItem
import me.wcy.music.mv.detail.viewmodel.MvDetailViewModel
import me.wcy.music.shared.util.formatPlayCount

@Composable
fun MvDetailScreen(
    viewModel: MvDetailViewModel,
    mvid: Long,
    onBack: () -> Unit,
    onPlayMv: (String) -> Unit
) {
    LaunchedEffect(mvid) {
        viewModel.init(mvid)
        viewModel.loadData()
    }

    val mv by viewModel.mv.collectAsState()
    val mvUrl by viewModel.mvUrl.collectAsState()
    val isSub by viewModel.isSub.collectAsState()
    val scope = rememberCoroutineScope()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TitleBar(title = mv?.name ?: "MV", onBack = onBack)
        }
        item {
            MvPlayerCover(
                cover = mv?.cover ?: "",
                canPlay = mvUrl.isNotBlank(),
                onPlay = { onPlayMv(mvUrl) }
            )
        }
        mv?.let { data ->
            item {
                MvInfo(
                    mv = data,
                    isSub = isSub,
                    onCollect = { scope.launch { viewModel.collect() } }
                )
            }
        }
    }
}

@Composable
private fun MvPlayerCover(
    cover: String,
    canPlay: Boolean,
    onPlay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        CoverImage(
            url = cover,
            cornerRadius = 0.dp,
            modifier = Modifier.fillMaxSize()
        )
        if (canPlay) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "播放",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun MvInfo(
    mv: MvItem,
    isSub: Boolean,
    onCollect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = mv.name,
            color = AppThemeColor.TextH1,
            fontSize = 16.sp
        )
        Row(
            modifier = Modifier.padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mv.artistName,
                color = AppThemeColor.TextH2,
                fontSize = 13.sp
            )
            Text(
                text = buildList {
                    mv.publishTime.takeIf { it.isNotBlank() }?.let { add(it) }
                    if (mv.playCount > 0) add("${formatPlayCount(mv.playCount)}次播放")
                }.joinToString(" · "),
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 10.dp)
            )
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .background(AppThemeColor.ThemeColor, RoundedCornerShape(12.dp))
                    .clickable(onClick = onCollect)
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isSub) "已收藏" else "收藏",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
        val desc = mv.desc.ifBlank { mv.briefDesc }
        if (desc.isNotBlank()) {
            Text(
                text = desc,
                color = AppThemeColor.TextH2,
                fontSize = 12.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

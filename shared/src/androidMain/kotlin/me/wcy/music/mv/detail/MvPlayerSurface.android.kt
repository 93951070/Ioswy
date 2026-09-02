package me.wcy.music.mv.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
actual fun MvPlayerSurface(
    url: String,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = true
                // PlayerView 自带全屏按钮：每次点击翻转调用方的全屏状态（页面内布局切换，无需旋转）
                setFullscreenButtonClickListener { onToggleFullscreen() }
            }
        }
    )
    DisposableEffect(url) {
        onDispose { player.release() }
    }
}

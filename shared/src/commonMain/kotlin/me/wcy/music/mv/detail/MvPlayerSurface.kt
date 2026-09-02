package me.wcy.music.mv.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * MV 内嵌视频播放器：进入页面即自动播放。
 * Android 用 Media3 ExoPlayer，iOS 用 AVPlayerLayer。
 * 全屏为页面内布局切换：调用方持有 isFullscreen 状态，true 时把容器铺满；
 * 控制层全屏按钮点击触发 onToggleFullscreen 翻转状态。
 */
@Composable
expect fun MvPlayerSurface(
    url: String,
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {},
    modifier: Modifier
)

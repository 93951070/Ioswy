package me.wcy.music.mv.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * MV 内嵌视频播放器：进入页面即自动播放。
 * Android 用 Media3 ExoPlayer，iOS 用 AVPlayerLayer。
 */
@Composable
expect fun MvPlayerSurface(url: String, modifier: Modifier)

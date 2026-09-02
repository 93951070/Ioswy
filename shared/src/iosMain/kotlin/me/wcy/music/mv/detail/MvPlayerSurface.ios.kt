package me.wcy.music.mv.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.UIKit.UIView

/**
 * 承载 AVPlayerLayer 的容器：layoutSubviews 时同步 layer frame，
 * 解决 Compose 首帧 container.bounds 为 0 导致画面白屏（只有声音）的问题。
 */
@OptIn(ExperimentalForeignApi::class)
private class PlayerContainerView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {

    override fun layoutSubviews() {
        super.layoutSubviews()
        val sublayer = layer.sublayers?.firstOrNull() as? AVPlayerLayer
        if (sublayer != null) {
            sublayer.frame = layer.bounds
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MvPlayerSurface(url: String, modifier: Modifier) {
    val player = remember(url) {
        val p = AVPlayer()
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            p.replaceCurrentItemWithPlayerItem(AVPlayerItem(nsUrl))
        }
        p.play()
        p
    }
    UIKitView(
        modifier = modifier,
        factory = {
            PlayerContainerView().also { container ->
                val layer = AVPlayerLayer()
                layer.player = player
                layer.videoGravity = AVLayerVideoGravityResizeAspect
                layer.frame = container.bounds
                container.layer.addSublayer(layer)
            }
        },
        update = { container ->
            val sublayer = container.layer.sublayers?.firstOrNull() as? AVPlayerLayer
            if (sublayer != null && sublayer.player != player) {
                sublayer.player = player
            }
        }
    )
    DisposableEffect(url) {
        onDispose { player.pause() }
    }
}

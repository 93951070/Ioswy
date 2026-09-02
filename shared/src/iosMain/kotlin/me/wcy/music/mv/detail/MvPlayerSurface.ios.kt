package me.wcy.music.mv.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.Foundation.NSURL
import platform.UIKit.UIView

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
            val container = UIView()
            val layer = AVPlayerLayer()
            layer.player = player
            layer.videoGravity = AVLayerVideoGravityResizeAspect
            layer.frame = container.bounds
            container.layer.addSublayer(layer)
            container
        },
        update = { container ->
            val sublayer = container.layer.sublayers?.firstOrNull() as? AVPlayerLayer
            if (sublayer != null) {
                sublayer.frame = container.bounds
            }
        }
    )
    DisposableEffect(url) {
        onDispose { player.pause() }
    }
}

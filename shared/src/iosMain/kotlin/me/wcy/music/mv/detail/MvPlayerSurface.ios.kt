package me.wcy.music.mv.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerLayer
import platform.Foundation.NSURL
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MvPlayerSurface(url: String, modifier: Modifier) {
    val player = remember(url) {
        AVPlayer().apply {
            NSURL.URLWithString(url)?.let {
                replaceCurrentItemWithPlayerItem(AVPlayerItem(it))
            }
            play()
        }
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
            (container.layer.sublayers?.firstOrNull() as? AVPlayerLayer)?.frame = container.bounds
        },
        onRelease = { player.pause() }
    )
}

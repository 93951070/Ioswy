package me.wcy.music.shared

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.AVKit.*
import platform.Foundation.*
import platform.UIKit.*

/**
 * MV 视频播放：present 全屏 AVPlayerViewController。
 */
object IosMvPlayer {

    @OptIn(ExperimentalForeignApi::class)
    fun present(url: String) {
        val rootViewController =
            UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        val nsUrl = NSURL.URLWithString(url) ?: return
        val player = AVPlayer()
        player.replaceCurrentItemWithPlayerItem(AVPlayerItem(nsUrl))
        val controller = AVPlayerViewController()
        controller.player = player
        player.play()
        rootViewController.presentViewController(
            viewControllerToPresent = controller,
            animated = true,
            completion = null
        )
    }
}

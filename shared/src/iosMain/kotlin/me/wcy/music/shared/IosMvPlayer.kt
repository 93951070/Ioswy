package me.wcy.music.shared

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayer
import platform.AVKit.AVPlayerViewController
import platform.UIKit.UIApplication
import platform.darwin.NSObject

/**
 * MV 视频播放：present 全屏 AVPlayerViewController。
 */
object IosMvPlayer {

    @OptIn(ExperimentalForeignApi::class)
    fun present(url: String) {
        val rootViewController =
            UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        val player = AVPlayer(uRL = platform.Foundation.NSURL.URLWithString(url))
        val controller = AVPlayerViewController()
        controller.player = player
        player.play()
        rootViewController.presentViewController(
            viewController = controller,
            animated = true,
            completion = null
        )
    }
}

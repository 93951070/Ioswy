package me.wcy.music.shared

import platform.Foundation.*
import platform.UIKit.*

/**
 * iOS 系统分享面板（UIActivityViewController）。
 * ponytail: 只做文本分享；拿不到顶层 VC 时静默失败。
 */
object IosShareHelper {

    fun shareText(text: String) {
        runCatching {
            val topViewController = topViewController() ?: return
            val controller = UIActivityViewController(
                activityItems = listOf(text),
                applicationActivities = null
            )
            // iPad 必须给 popover 锚点，否则 present 崩溃；iPhone 上该属性为 null 自动跳过
            topViewController.view?.let { view ->
                controller.popoverPresentationController?.sourceView = view
            }
            topViewController.presentViewController(
                controller,
                animated = true,
                completion = null
            )
        }
    }

    private fun topViewController(): UIViewController? {
        // keyWindow 在 iOS 13+ deprecated 但 K/N 仍可用且稳定，scene API 的 K/N 集合映射坑多
        @Suppress("DEPRECATION")
        val window = UIApplication.sharedApplication.keyWindow ?: return null
        var viewController = window.rootViewController ?: return null
        while (viewController.presentedViewController != null) {
            viewController = viewController.presentedViewController!!
        }
        return viewController
    }
}

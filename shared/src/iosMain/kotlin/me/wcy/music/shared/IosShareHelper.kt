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
        val scenes = UIApplication.sharedApplication.connectedScenes
        val windowScenes = scenes.allObjects.filterIsInstance<UIWindowScene>()
        val windowScene = windowScenes.firstOrNull {
            it.activationState == UISceneActivationStateForegroundActive
        } ?: windowScenes.firstOrNull() ?: return null
        val window = windowScene.windows.firstOrNull { it.isKeyWindow }
            ?: windowScene.windows.firstOrNull()
            ?: return null
        var viewController = window.rootViewController ?: return null
        while (viewController.presentedViewController != null) {
            viewController = viewController.presentedViewController!!
        }
        return viewController
    }
}

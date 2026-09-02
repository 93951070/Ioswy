package me.wcy.music.shared.player

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession

@OptIn(ExperimentalForeignApi::class)
actual fun downloadToFile(url: String, filename: String, onDone: (Boolean, String) -> Unit) {
    val nsUrl = NSURL.URLWithString(url)
    if (nsUrl == null) {
        onDone(false, "下载地址无效")
        return
    }
    // Documents/Music 在本地音乐扫描（Documents 递归扫描）覆盖范围内，下载完成即可在「本地音乐」播到
    val dirPath = NSHomeDirectory() + "/Documents/Music"
    val fileManager = NSFileManager.defaultManager
    fileManager.createDirectoryAtPath(dirPath, withIntermediateDirectories = true, attributes = null, error = null)
    val targetUrl = NSURL.fileURLWithPath("$dirPath/$filename")
    val main = CoroutineScope(Dispatchers.Main)
    val task = NSURLSession.sharedSession.downloadTaskWithURL(nsUrl) { location, _, error ->
        if (location == null || error != null) {
            main.launch { onDone(false, "下载失败") }
            return@downloadTaskWithURL
        }
        // 目标已存在时 move 会失败，先清旧文件（不存在时返回 false 无影响）
        fileManager.removeItemAtURL(targetUrl, null)
        val moved = fileManager.moveItemAtURLToURL(location, targetUrl, null)
        main.launch { onDone(moved, if (moved) "已下载到本地音乐" else "下载失败") }
    }
    task.resume()
}

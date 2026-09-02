package me.wcy.music.shared.player

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
actual fun downloadToFile(url: String, filename: String, onDone: (Boolean, String) -> Unit) {
    val nsUrl = NSURL.URLWithString(url)
    if (nsUrl == null) {
        onDone(false, "下载地址无效")
        return
    }
    val main = CoroutineScope(Dispatchers.Main)
    CoroutineScope(Dispatchers.IO).launch {
        // ponytail: NSData.create 同步下载，阻塞 IO 线程；需要进度回调时换 NSURLSession delegate
        val data = runCatching { NSData.create(contentsOfURL = nsUrl) }.getOrNull()
        if (data == null) {
            main.launch { onDone(false, "下载失败") }
            return@launch
        }
        // Documents/Music 在本地音乐扫描（Documents 递归扫描）覆盖范围内，下载完成即可在「本地音乐」播到
        val dirPath = NSHomeDirectory() + "/Documents/Music"
        val fileManager = NSFileManager.defaultManager
        fileManager.createDirectoryAtPath(dirPath, withIntermediateDirectories = true, attributes = null, error = null)
        val path = "$dirPath/$filename"
        fileManager.removeItemAtPath(path, null)
        val ok = data.writeToFile(path, atomically = true)
        main.launch { onDone(ok, if (ok) "已下载到本地音乐" else "下载失败") }
    }
}

package me.wcy.music.shared.player

import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory

actual fun downloadDir(): String {
    // Documents/Music 在本地音乐扫描（Documents 递归扫描）覆盖范围内，下载完成即可在「本地音乐」播到
    val dirPath = NSHomeDirectory() + "/Documents/Music"
    NSFileManager.defaultManager.createDirectoryAtPath(
        dirPath, withIntermediateDirectories = true, attributes = null, error = null
    )
    return dirPath
}

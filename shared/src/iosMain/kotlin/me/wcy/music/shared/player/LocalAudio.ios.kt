package me.wcy.music.shared.player

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager

@OptIn(ExperimentalForeignApi::class)
actual fun deleteLocalAudio(path: String): Boolean {
    return NSFileManager.defaultManager.removeItemAtPath(path, null)
}

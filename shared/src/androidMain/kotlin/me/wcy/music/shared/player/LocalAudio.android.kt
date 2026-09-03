package me.wcy.music.shared.player

import java.io.File

actual fun deleteLocalAudio(path: String): Boolean {
    return runCatching { File(path).delete() }.getOrDefault(false)
}

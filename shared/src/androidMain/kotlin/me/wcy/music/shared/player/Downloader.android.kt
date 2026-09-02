package me.wcy.music.shared.player

import android.os.Environment
import top.wangchenyan.common.CommonApp

actual fun downloadDir(): String {
    return CommonApp.app.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
        ?: CommonApp.app.filesDir.absolutePath
}

package me.wcy.music.shared.player

import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.wangchenyan.common.CommonApp
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

actual fun downloadToFile(url: String, filename: String, onDone: (Boolean, String) -> Unit) {
    val main = CoroutineScope(Dispatchers.Main)
    Thread {
        var ok = false
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10000
            conn.readTimeout = 30000
            val dir = CommonApp.app.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                ?: throw IllegalStateException("存储目录不可用")
            val target = File(dir, filename)
            if (target.exists()) {
                target.delete()
            }
            conn.inputStream.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            ok = true
        } catch (ignored: Exception) {
        } finally {
            conn.disconnect()
        }
        main.launch { onDone(ok, if (ok) "已下载到本地音乐" else "下载失败") }
    }.start()
}

package me.wcy.music.shared.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.wcy.music.common.bean.SongData
import me.wcy.music.shared.net.DiscoverNet

/**
 * 跨平台下载器：把音频流写入端侧本地音乐目录，完成/失败通过 [onDone](成功, 提示语) 回调（主线程）。
 * iOS 落盘 Documents/Music/（与本地音乐扫描目录一致），Android 落盘 getExternalFilesDir(Music)。
 */
expect fun downloadToFile(url: String, filename: String, onDone: (Boolean, String) -> Unit)

/** 非协程环境入口（UI 点击回调）：内部切主线程协程执行 [downloadSong] */
fun downloadSongAsync(song: SongData, onMessage: (String) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch { downloadSong(song, onMessage) }
}

// ponytail: 单下载标记，检查+置位都在首个挂起点前的主线程上完成，Main 串行保证无并发竞争
private var downloading = false

/** 按 songId 拉 standard 流地址后下载，文件名 = 歌手 - 歌名.mp3（过滤非法字符） */
suspend fun downloadSong(song: SongData, onMessage: (String) -> Unit) {
    if (downloading) {
        onMessage("正在下载，请稍候")
        return
    }
    if (song.id <= 0) {
        onMessage("无法下载该歌曲")
        return
    }
    downloading = true
    try {
        onMessage("正在获取下载地址…")
        val url = runCatching { DiscoverNet.getSongDownloadUrl(song.id) }.getOrNull()
        if (url.isNullOrEmpty()) {
            onMessage("获取下载地址失败")
            return
        }
        onMessage("开始下载 ${song.name}")
        val artist = song.ar.joinToString(",") { it.name }
        downloadToFile(
            url = url,
            filename = "${safeFileName(artist)} - ${safeFileName(song.name)}.mp3",
            onDone = { _, msg -> onMessage(msg) }
        )
    } finally {
        downloading = false
    }
}

private fun safeFileName(name: String): String {
    return name.replace(Regex("[\\\\/:*?\"<>|]"), "").trim().ifEmpty { "未知" }
}

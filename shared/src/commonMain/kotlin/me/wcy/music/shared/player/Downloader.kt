package me.wcy.music.shared.player

import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import me.wcy.music.common.bean.SongData
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.shared.net.SharedNet
import kotlinx.io.write

/**
 * 跨平台下载器：把音频流写入端侧本地音乐目录，完成/失败通过 [onDone](成功, 提示语) 回调（主线程）。
 * iOS 落盘 Documents/Music/（与本地音乐扫描目录一致），Android 落盘 getExternalFilesDir(Music)。
 */
expect fun downloadDir(): String

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
        // 顺手写同名歌词文件（按纯歌名命名，本地播放时直接命中），失败不影响下载结果
        runCatching {
            val lrc = DiscoverNet.getLrc(song.id)
            if (lrc.code == 200 && lrc.lrc.isValid()) {
                FileSystem.SYSTEM.sink("${downloadDir()}/${safeFileName(song.name)}.lrc".toPath())
                    .buffer().use { it.writeUtf8(lrc.lrc.lyric) }
            }
        }
    } finally {
        downloading = false
    }
}

/** 读本地同名歌词文件（下载时写入），无则 null */
fun loadLocalLrc(songName: String): String? {
    val path = "${downloadDir()}/${safeFileName(songName)}.lrc".toPath()
    return runCatching {
        if (!FileSystem.SYSTEM.exists(path)) return@runCatching null
        FileSystem.SYSTEM.source(path).buffer().use { it.readUtf8() }
    }.getOrNull()?.takeIf { it.isNotBlank() }
}

/** 统一歌词拉取：本地文件优先，网络兜底；null = 无歌词 */
suspend fun fetchLyrics(song: SongData): String? {
    if (song.id <= 0) return loadLocalLrc(song.name)
    return loadLocalLrc(song.name) ?: runCatching {
        DiscoverNet.getLrc(song.id).takeIf { it.code == 200 && it.lrc.isValid() }?.lrc?.lyric
    }.getOrNull()
}

private const val CHUNK = 256 * 1024

/** Ktor 拉流 + kotlinx-io 落盘，双端统一实现 */
// ponytail: 全量入内存（单曲 5-10MB 可接受），要支持大文件/进度再改逐块流式
suspend fun downloadToFile(url: String, filename: String, onDone: (Boolean, String) -> Unit) {
    val ok = runCatching {
        val path = "${downloadDir()}/$filename".toPath()
        val response = SharedNet.client.get(url)
        check(response.status == HttpStatusCode.OK) { "HTTP ${response.status}" }
        val bytes = response.readRawBytes()
        FileSystem.SYSTEM.sink(path).buffer().use { it.write(bytes) }
    }.isSuccess
    onDone(ok, if (ok) "已下载到本地音乐" else "下载失败")
}

private fun safeFileName(name: String): String {
    return name.replace(Regex("[\\\\/:*?\"<>|]"), "").trim().ifEmpty { "未知" }
}

package me.wcy.music.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.shared.net.PlaylistManageNet
import me.wcy.music.shared.net.SharedNet

/**
 * 从外部平台歌单分享文本导入歌单。
 * 解析「歌名 - 歌手」「歌名/歌手」「纯歌名」等格式，
 * 逐首 cloudsearch 匹配后批量 addTracks。
 */
data class ImportSong(
    val title: String,
    val artist: String,
) {
    val display: String get() = if (artist.isBlank()) title else "$title - $artist"
}

object ImportPlaylistParser {

    private val numberPrefix = Regex("^\\s*\\d{1,4}\\s*[.、,，．:：)）]\\s*")
    private val bracketNumberPrefix = Regex("^\\s*[（(]\\d{1,4}[)）]\\s*")
    private val chineseNumberPrefix = Regex("^\\s*[一二三四五六七八九十]{1,3}\\s*[、.．:：)）]\\s*")
    private val spacedDash = Regex("\\s+[-–—]\\s+")
    private val quoteChars = charArrayOf(
        ' ', '\u3000', '《', '》', '「', '』', '『', '』',
        '"', '\u201c', '\u201d', '\'', '\u2018', '\u2019'
    )

    fun parse(text: String): List<ImportSong> {
        val result = LinkedHashMap<String, ImportSong>()
        text.split('\n').forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.contains("http", ignoreCase = true)) return@forEach
            val dashSegs = line.split(spacedDash)
            when {
                // "歌1 - 歌手1 - 歌2 - 歌手2" 连续歌名歌手对
                dashSegs.size >= 4 && dashSegs.size % 2 == 0 ->
                    dashSegs.chunked(2).forEach { (title, artist) ->
                        addSong(result, parseSegment(title, artist))
                    }
                // "歌1 - 歌手1、歌2 - 歌手2" 顿号分隔，且每段都含歌名歌手结构
                line.contains('、') && line.contains('-') &&
                    line.split('、').all { it.contains('-') || it.contains('｜') || it.contains('|') } ->
                    line.split('、').forEach { seg ->
                        addSong(result, parseSegment(seg, ""))
                    }
                else -> addSong(result, parseSegment(line, ""))
            }
        }
        return result.values.toList()
    }

    private fun addSong(result: LinkedHashMap<String, ImportSong>, song: ImportSong?) {
        if (song != null && song.title.isNotBlank()) {
            val key = "${song.title}|${song.artist}"
            if (!result.containsKey(key)) {
                result[key] = song
            }
        }
    }

    private fun parseSegment(seg0: String, artistFallback: String): ImportSong? {
        var seg = seg0.trim()
        listOf(numberPrefix, bracketNumberPrefix, chineseNumberPrefix).forEach { re ->
            seg = seg.replaceFirst(re, "")
        }
        seg = seg.trim(*quoteChars)
        if (seg.isEmpty()) return null
        // 歌名 - 歌手 / 歌名｜歌手
        val dashIdx = seg.indexOfFirst { it == '-' || it == '｜' || it == '|' }
        if (dashIdx > 0) {
            val title = seg.substring(0, dashIdx).trim(*quoteChars)
            val artist = seg.substring(dashIdx + 1).trim(*quoteChars)
            if (title.isNotBlank()) return ImportSong(title, artist)
        }
        // 歌名/歌手
        val slashIdx = seg.indexOf('/')
        if (slashIdx > 0) {
            val title = seg.substring(0, slashIdx).trim(*quoteChars)
            val artist = seg.substring(slashIdx + 1).trim(*quoteChars)
            if (title.isNotBlank()) return ImportSong(title, artist)
        }
        return ImportSong(seg, artistFallback)
    }
}

class ImportPlaylistViewModel : ViewModel() {

    data class ImportState(
        val parsed: List<ImportSong> = emptyList(),
        val importing: Boolean = false,
        val matched: Int = 0,
        val done: Boolean = false,
        val successCount: Int = 0,
        val failedSongs: List<ImportSong> = emptyList(),
        // addTracks 失败信息（如账号无权限 code 401）
        val addResultMsg: String = "",
        // 致命错误：创建歌单失败等
        val error: String = "",
    )

    private val _state = MutableStateFlow(ImportState())
    val state = _state.asStateFlow()

    fun parse(text: String) {
        _state.update { it.copy(parsed = ImportPlaylistParser.parse(text)) }
    }

    fun startImport(playlistName: String) {
        val songs = _state.value.parsed
        if (songs.isEmpty() || _state.value.importing) return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    importing = true, done = false, matched = 0,
                    successCount = 0, failedSongs = emptyList(),
                    addResultMsg = "", error = ""
                )
            }
            val created = runCatching {
                PlaylistManageNet.createPlaylist(playlistName.ifBlank { "导入的歌单" })
            }.getOrNull()
            if (created == null || created.code != 200 || created.id <= 0) {
                _state.update {
                    it.copy(
                        importing = false,
                        error = "创建歌单失败" + (created?.let { c -> "(${c.code})" } ?: "，网络异常")
                    )
                }
                return@launch
            }
            val pid = created.id
            val matchedIds = mutableListOf<Long>()
            val failed = mutableListOf<ImportSong>()
            songs.forEach { song ->
                val keywords = if (song.artist.isBlank()) song.title else "${song.title} ${song.artist}"
                val id = runCatching { searchFirstSongId(keywords) }.getOrDefault(0L)
                if (id > 0) matchedIds.add(id) else failed.add(song)
                _state.update { it.copy(matched = matchedIds.size) }
            }
            var addMsg = ""
            matchedIds.chunked(BATCH_SIZE).forEach { batch ->
                val res = runCatching {
                    PlaylistManageNet.addTracks(pid, batch.joinToString(","))
                }.getOrNull()
                if (res == null || res.code != 200) {
                    addMsg = res?.let { it.msg ?: it.message } ?: "网络异常"
                }
            }
            _state.update {
                it.copy(
                    importing = false,
                    done = true,
                    successCount = matchedIds.size,
                    failedSongs = failed,
                    addResultMsg = addMsg,
                )
            }
        }
    }

    private suspend fun searchFirstSongId(keywords: String): Long {
        val text = SharedNet.post(
            "cloudsearch",
            params = listOf(
                "type" to 1,
                "keywords" to keywords,
                "limit" to 1,
            )
        )
        val bean = SharedJson.decodeBean<CloudSearchResp>(text)
        return bean.result?.songs?.firstOrNull()?.id ?: 0L
    }

    companion object {
        private const val BATCH_SIZE = 50
    }
}

@Serializable
private data class CloudSearchResp(val result: CloudSearchResult? = null)

@Serializable
private data class CloudSearchResult(val songs: List<CloudSearchSong> = emptyList())

@Serializable
private data class CloudSearchSong(val id: Long = 0)

@Composable
fun ImportPlaylistScreen(
    viewModel: ImportPlaylistViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var text by remember { mutableStateOf("") }
    var playlistName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColor.Background)
    ) {
        TitleBar(title = "导入外部歌单", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        viewModel.parse(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "粘贴歌单分享文本，每行一首：\n歌名 - 歌手\n或 歌名/歌手\n或纯歌名",
                            fontSize = 13.sp
                        )
                    },
                    minLines = 5
                )
            }
            item {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("歌单名（默认：导入的歌单）", fontSize = 13.sp) }
                )
            }
            if (state.parsed.isNotEmpty()) {
                item {
                    Text(
                        text = "识别到 ${state.parsed.size} 首",
                        color = AppThemeColor.TextH1,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(state.parsed) { song ->
                    Text(
                        text = song.display,
                        color = AppThemeColor.TextH2,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            item {
                if (state.importing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "已匹配 ${state.matched}/${state.parsed.size}",
                            color = AppThemeColor.TextH2,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.startImport(playlistName) },
                        enabled = state.parsed.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "开始导入", color = Color.White)
                    }
                }
            }
            if (state.error.isNotEmpty()) {
                item {
                    Text(
                        text = "导入失败：${state.error}",
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
            }
            if (state.done) {
                item {
                    Text(
                        text = buildString {
                            append("导入完成：成功 ${state.successCount} 首，失败 ${state.failedSongs.size} 首")
                            if (state.addResultMsg.isNotEmpty()) {
                                append("\n添加到歌单失败：${state.addResultMsg}")
                            }
                        },
                        color = AppThemeColor.TextH1,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (state.failedSongs.isNotEmpty()) {
                    item {
                        Text(
                            text = "未匹配歌曲：",
                            color = AppThemeColor.TextH2,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(state.failedSongs) { song ->
                        Text(
                            text = song.display,
                            color = AppThemeColor.TextH2,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

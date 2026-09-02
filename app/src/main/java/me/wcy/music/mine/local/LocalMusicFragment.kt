package me.wcy.music.mine.local

import android.view.View
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.LocalMusicScreen
import me.wcy.music.compose.ui.LocalSongData
import me.wcy.music.consts.RoutePath
import me.wcy.music.service.PlayerController
import me.wcy.music.storage.db.entity.SongEntity
import me.wcy.music.utils.toMediaItem
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route
import javax.inject.Inject

/**
 * Created by wangchenyan.top on 2023/8/30.
 */
@Route(RoutePath.LOCAL_SONG)
@AndroidEntryPoint
class LocalMusicFragment : BaseMusicFragment() {
    private var composeView: ComposeView? = null

    @Inject
    lateinit var playerController: PlayerController

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    val context = LocalContext.current
                    var songs by remember { mutableStateOf<List<SongEntity>>(emptyList()) }
                    var loaded by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        songs = withContext(Dispatchers.IO) {
                            LocalMusicLoader().load(context)
                        }
                        loaded = true
                    }

                    LocalMusicScreen(
                        songs = songs.map { it.toLocalSongData() },
                        loaded = loaded,
                        onPlaySong = { index ->
                            val mediaItems = songs.map { it.toMediaItem() }
                            if (index in mediaItems.indices) {
                                playerController.replaceAll(mediaItems, mediaItems[index])
                                openPlaying()
                            }
                        },
                        onPlayAll = {
                            val mediaItems = songs.map { it.toMediaItem() }
                            if (mediaItems.isNotEmpty()) {
                                playerController.replaceAll(mediaItems, mediaItems.first())
                                openPlaying()
                            }
                        },
                        onBack = { finish() }
                    )
                }
            }
            composeView = view
        }
    }

    private fun openPlaying() {
        context?.let {
            CRouter.with(it).url(RoutePath.PLAYING).start()
        }
    }

    private fun SongEntity.toLocalSongData() = LocalSongData(
        id = songId,
        title = title,
        artist = artist,
        album = album,
        cover = getSmallCover(),
        duration = duration,
        fileName = fileName,
        fileSize = fileSize,
        path = path
    )
}

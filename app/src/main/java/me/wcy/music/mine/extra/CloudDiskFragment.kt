package me.wcy.music.mine.extra

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.consts.RoutePath
import me.wcy.music.mine.extra.cloud.CloudDiskScreen
import me.wcy.music.mine.extra.cloud.CloudDiskViewModel
import me.wcy.music.service.PlayerController
import me.wcy.music.utils.toMediaItem
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route
import top.wangchenyan.common.ext.toast
import javax.inject.Inject

@Route(RoutePath.CLOUD_DISK)
@AndroidEntryPoint
class CloudDiskFragment : BaseMusicFragment() {
    private val viewModel by viewModels<CloudDiskViewModel>()
    private var composeView: ComposeView? = null

    @Inject
    lateinit var playerController: PlayerController

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    CloudDiskScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onPlaySongs = { songs, index ->
                            playSongs(songs, index)
                        },
                        onDelete = { item ->
                            lifecycleScope.launch {
                                toast(
                                    if (viewModel.delete(item)) "删除成功" else "删除失败"
                                )
                            }
                        }
                    )
                }
            }
            composeView = view
        }
    }

    private fun playSongs(songs: List<SongData>, index: Int) {
        val mediaItems = songs.map { it.toMediaItem() }
        if (index in mediaItems.indices) {
            playerController.replaceAll(mediaItems, mediaItems[index])
            context?.let {
                CRouter.with(it).url(RoutePath.PLAYING).start()
            }
        }
    }
}

package me.wcy.music.discover.dj

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.common.bean.SongData
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.consts.RoutePath
import me.wcy.music.dj.detail.DjDetailScreen
import me.wcy.music.dj.detail.viewmodel.DjDetailViewModel
import me.wcy.music.service.PlayerController
import me.wcy.music.utils.toMediaItem
import me.wcy.router.CRouter
import me.wcy.router.annotation.Route
import top.wangchenyan.common.ext.toast
import javax.inject.Inject

/**
 * Created by wangchenyan.top on 2023/9/26.
 */
@Route(RoutePath.DJ_DETAIL)
@AndroidEntryPoint
class DjDetailFragment : BaseMusicFragment() {
    private val viewModel by viewModels<DjDetailViewModel>()
    private var composeView: ComposeView? = null

    @Inject
    lateinit var playerController: PlayerController

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    val id = getRouteArguments().getLongExtra("id", 0)
                    if (id <= 0) {
                        finish()
                        return@MusicTheme
                    }
                    DjDetailScreen(
                        viewModel = viewModel,
                        rid = id,
                        onBack = { finish() },
                        onPlaySongs = { songs, index -> playSongs(songs, index) },
                        onMessage = { toast(it) },
                        onOpenFloor = { pid ->
                            CRouter.with(requireActivity())
                                .url(RoutePath.COMMENT_FLOOR)
                                .extra("resourceId", id)
                                .extra("resourceType", 4)
                                .extra("parentCommentId", pid)
                                .start()
                        }
                    )
                }
            }
            composeView = view
        }
    }

    private fun playSongs(songs: List<SongData>, index: Int) {
        val items = songs.filter { it.id > 0 }.map { it.toMediaItem() }
        if (items.isEmpty()) return
        val start = index.coerceIn(items.indices)
        playerController.replaceAll(items, items[start])
        CRouter.with(requireContext()).url(RoutePath.PLAYING).start()
    }
}

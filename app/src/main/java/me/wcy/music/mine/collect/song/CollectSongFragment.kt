package me.wcy.music.mine.collect.song

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.wcy.music.R
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.compose.component.CoverImage
import me.wcy.music.compose.theme.MusicTheme
import top.wangchenyan.common.ext.toast

/**
 * Created by wangchenyan.top on 2024/3/20.
 */
@AndroidEntryPoint
class CollectSongFragment : BottomSheetDialogFragment() {
    private val viewModel: CollectSongViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val songId = arguments?.getLong("song_id") ?: 0
        if (songId <= 0) {
            toast("参数错误")
            dismissAllowingStateLoss()
        } else {
            viewModel.songId = songId
            lifecycleScope.launch {
                viewModel.getMyPlayList()
            }
        }
        return ComposeView(requireContext()).apply {
            setContent {
                MusicTheme {
                    CollectSongContent(
                        onCollect = { playlist ->
                            collectSong(playlist.id)
                        }
                    )
                }
            }
        }
    }

    private fun collectSong(pid: Long) {
        lifecycleScope.launch {
            val res = viewModel.collectSong(pid)
            if (res.isSuccess()) {
                toast("操作成功")
                dismissAllowingStateLoss()
            } else {
                toast(res.msg)
            }
        }
    }

    companion object {
        const val TAG = "CollectSongFragment"

        fun newInstance(songId: Long): CollectSongFragment {
            return CollectSongFragment().apply {
                arguments = bundleOf("song_id" to songId)
            }
        }
    }
}

@Composable
private fun CollectSongContent(onCollect: (PlaylistData) -> Unit) {
    val viewModel: CollectSongViewModel = viewModel()
    val playlists by viewModel.myPlaylists.collectAsState()

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "收藏到歌单",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
        ) {
            items(playlists) { playlist ->
                PlaylistRow(playlist = playlist, onClick = { onCollect(playlist) })
            }
        }
    }
}

@Composable
private fun PlaylistRow(playlist: PlaylistData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(
            url = playlist.getSmallCover(),
            contentDescription = playlist.name,
            modifier = Modifier.size(48.dp),
            cornerRadius = 4.dp
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = playlist.name,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "${playlist.trackCount}首",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

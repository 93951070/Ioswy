package me.wcy.music.compose.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.wcy.music.R
import me.wcy.music.account.service.UserService
import me.wcy.music.compose.component.PlayBar
import me.wcy.music.compose.ui.homePageState
import androidx.compose.ui.platform.LocalContext
import me.wcy.music.consts.RoutePath
import me.wcy.router.CRouter
import me.wcy.music.compose.theme.AppThemeColor
import me.wcy.music.storage.preference.ConfigPreferences
import me.wcy.music.discover.home.viewmodel.DiscoverViewModel
import me.wcy.music.main.NaviTab
import me.wcy.music.mine.home.viewmodel.MineViewModel
import me.wcy.music.service.PlayerController
import me.wcy.music.shared.player.PlayerEngine

@Composable
fun MainScreen(
    drawerState: DrawerState,
    discoverViewModel: DiscoverViewModel,
    mineViewModel: MineViewModel,
    playerController: PlayerController,
    playerEngine: PlayerEngine,
    userService: UserService,
    onOpenDrawer: () -> Unit,
    onMenuSelect: (Int) -> Unit,
    onOpenPlaylist: () -> Unit,
    onOpenPlaying: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit,
    onOpenRanking: () -> Unit,
    onOpenPlaylistSquare: () -> Unit,
    onOpenRecommendSong: () -> Unit,
    onOpenPersonalFm: () -> Unit,
    onOpenArtistList: () -> Unit,
    onOpenNewSong: () -> Unit,
    onOpenDj: () -> Unit,
    onOpenMvList: () -> Unit,
    onOpenRecentPlay: () -> Unit,
    onOpenSubList: () -> Unit,
    onOpenCloudDisk: () -> Unit,
    onOpenMsgCenter: () -> Unit,
    onOpenLocalMusic: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenLogin: () -> Unit
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf<NaviTab>(NaviTab.Discover) }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                NaviTab.Discover -> DiscoverScreen(
                    viewModel = discoverViewModel,
                    playerController = playerController,
                    playerEngine = playerEngine,
                    onOpenDrawer = onOpenDrawer,
                    onOpenSearch = onOpenSearch,
                    onOpenPlaylistDetail = onOpenPlaylistDetail,
                    onOpenRanking = onOpenRanking,
                    onOpenPlaylistSquare = onOpenPlaylistSquare,
                    onOpenRecommendSong = onOpenRecommendSong,
                    onOpenPersonalFm = onOpenPersonalFm,
                    onOpenArtistList = onOpenArtistList,
                    onOpenNewSong = onOpenNewSong,
                    onOpenDj = onOpenDj,
                    onOpenMvList = onOpenMvList,
                    onOpenPlaying = onOpenPlaying,
                    onOpenMv = { id ->
                        CRouter.with(context).url(RoutePath.MV_DETAIL).extra("id", id).start()
                    },
                    onOpenVideo = {
                        CRouter.with(context).url(RoutePath.VIDEO).start()
                    },
                    onOpenDjRank = {
                        CRouter.with(context).url(RoutePath.DJ_RANK).start()
                    }
                )
                NaviTab.Mine -> MineScreen(
                    viewModel = mineViewModel,
                    profileFlow = userService.profile,
                    onOpenDrawer = onOpenDrawer,
                    onOpenSearch = onOpenSearch,
                    onOpenLogin = onOpenLogin,
                    onOpenLocalMusic = onOpenLocalMusic,
                    onOpenRecentPlay = onOpenRecentPlay,
                    onOpenSubList = onOpenSubList,
                    onOpenCloudDisk = onOpenCloudDisk,
                    onOpenMsgCenter = onOpenMsgCenter,
                    onOpenPlaylistDetail = { playlist, realtime, like ->
                        onOpenPlaylistDetail(playlist.id)
                    }
                )
            }
        }

        // 心动 tab（Discover pager 停在「心动」页）时底栏区域整体沉浸黑
        val heartDark = currentTab == NaviTab.Discover && homePageState.value == 0
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (heartDark) Color.Black else Color.Transparent)
        ) {
            PlayBar(
                playerEngine = playerEngine,
                onOpenPlaying = onOpenPlaying,
                onOpenPlaylist = onOpenPlaylist
            )

            HorizontalDivider(
                color = if (heartDark) Color.White.copy(alpha = 0.08f) else AppThemeColor.Divider
            )

            BottomTabBar(
                current = currentTab,
                dark = heartDark,
                onSelect = { currentTab = it }
            )
        }
    }

        // 桌面歌词改用系统悬浮窗（MusicService 持跨 App Window），不再 App 内渲染
        // 见 LyricFloatWindow.kt + MusicService.syncLyricWindow
    }
}

@Composable
private fun BottomTabBar(
    current: NaviTab,
    dark: Boolean,
    onSelect: (NaviTab) -> Unit
) {
    // 半透明白模拟毛玻璃（网易云底栏效果），真背景模糊需平台 GraphicsLayer，代价过高
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(if (dark) Color.Black else Color.White.copy(alpha = 0.82f))
    ) {
        NaviTabItem(NaviTab.Discover, current, dark, onSelect)
        NaviTabItem(NaviTab.Mine, current, dark, onSelect)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NaviTabItem(
    tab: NaviTab,
    current: NaviTab,
    dark: Boolean,
    onSelect: (NaviTab) -> Unit
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable { onSelect(tab) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = tab.name,
            color = if (current == tab) {
                AppThemeColor.ThemeColor
            } else if (dark) {
                Color.White.copy(alpha = 0.5f)
            } else {
                AppThemeColor.TextH2
            },
            fontSize = 13.sp
        )
    }
}

@Composable
fun DrawerContent(
    userService: UserService,
    onMenuSelect: (Int) -> Unit
) {
    val profile by userService.profile.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(AppThemeColor.Card)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(AppThemeColor.ThemeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile?.nickname ?: "网易云音乐",
                color = Color.White,
                fontSize = 20.sp
            )
        }
        DrawerMenuRow(Icons.Filled.Public, "域名设置", R.id.action_domain_setting, onMenuSelect)
        DrawerMenuRow(Icons.Filled.Settings, "功能设置", R.id.action_setting, onMenuSelect)
        DrawerMenuRow(Icons.Filled.Timer, "定时停止播放", R.id.action_timer, onMenuSelect)
        DrawerMenuRow(Icons.Filled.Description, "桌面歌词", R.id.action_desktop_lyrics, onMenuSelect)
        if (profile != null) {
            DrawerMenuRow(Icons.Filled.Logout, "退出登录", R.id.action_logout, onMenuSelect)
        }
        DrawerMenuRow(Icons.Filled.ExitToApp, "关闭应用", R.id.action_exit, onMenuSelect)
        DrawerMenuRow(Icons.Filled.Description, "关于网易云音乐", R.id.action_about, onMenuSelect)
    }
}

@Composable
private fun DrawerMenuRow(
    icon: ImageVector,
    label: String,
    id: Int,
    onMenuSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMenuSelect(id) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AppThemeColor.TextH1,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = AppThemeColor.TextH1,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

internal fun todayString(): String =
    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        .format(java.util.Date())

internal fun toast(context: Context?, msg: String) {
    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
}

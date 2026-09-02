package me.wcy.music.mv.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.*
import platform.UIKit.*

/**
 * 承载 AVPlayerLayer 的容器：layoutSubviews 时同步 layer frame，
 * 解决 Compose 首帧 container.bounds 为 0 导致画面白屏（只有声音）的问题。
 */
@OptIn(ExperimentalForeignApi::class)
private class PlayerContainerView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {

    override fun layoutSubviews() {
        super.layoutSubviews()
        val sublayer = layer.sublayers?.firstOrNull() as? AVPlayerLayer
        if (sublayer != null) {
            sublayer.frame = layer.bounds
        }
    }
}

/**
 * 请求旋转屏幕（iOS16+）：landscapeRight 进全屏，portrait 退出。
 * Info.plist 已声明支持横竖屏。返回是否成功发起请求，失败时调用方降级为竖屏全屏 + 提示。
 * （方法名/参数形式对齐已验证的 KMP 实践：requestGeometryUpdateWithPreferences + errorHandler=null）
 */
private fun requestInterfaceOrientation(landscape: Boolean): Boolean {
    return runCatching {
        val mask = if (landscape) {
            UIInterfaceOrientationMaskLandscapeRight
        } else {
            UIInterfaceOrientationMaskPortrait
        }
        val scene = UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
        scene?.requestGeometryUpdateWithPreferences(
            geometryPreferences = UIWindowSceneGeometryPreferencesIOS(interfaceOrientations = mask),
            errorHandler = null
        )
        scene != null
    }.getOrDefault(false)
}

/** 秒 -> "mm:ss"（commonMain 禁 String.format，padStart 拼接） */
private fun formatMvTime(seconds: Float): String {
    val total = if (seconds > 0f) seconds.toInt() else 0
    val m = (total / 60).toString().padStart(2, '0')
    val s = (total % 60).toString().padStart(2, '0')
    return "$m:$s"
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MvPlayerSurface(url: String, modifier: Modifier) {
    val player = remember(url) {
        val p = AVPlayer()
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            p.replaceCurrentItemWithPlayerItem(AVPlayerItem(nsUrl))
        }
        p.play()
        p
    }

    var isPlaying by remember(url) { mutableStateOf(true) }
    var currentTime by remember(url) { mutableFloatStateOf(0f) }
    var duration by remember(url) { mutableFloatStateOf(0f) }
    var showOverlay by remember(url) { mutableStateOf(true) }
    var fullscreen by remember(url) { mutableStateOf(false) }
    var showRotateHint by remember(url) { mutableStateOf(false) }
    var dragging by remember(url) { mutableStateOf(false) }
    var dragPos by remember(url) { mutableFloatStateOf(0f) }

    // 0.5s 轮询同步进度/时长/播放态（代替 periodicTimeObserver，Compose 侧协程自动随组合取消）
    LaunchedEffect(url) {
        while (isActive) {
            delay(500)
            player.currentTime().useContents {
                currentTime = if (timescale != 0) (value.toDouble() / timescale).toFloat() else 0f
            }
            val item = player.currentItem
            if (item != null) {
                item.duration.useContents {
                    duration = if (timescale != 0) (value.toDouble() / timescale).toFloat() else 0f
                }
            }
            isPlaying = player.timeControlStatus == AVPlayerTimeControlStatusPlaying
        }
    }

    // overlay 显示后 3 秒无操作自动隐藏（拖动进度条期间暂停计时）
    LaunchedEffect(showOverlay, dragging) {
        if (showOverlay && !dragging) {
            delay(3000)
            showOverlay = false
        }
    }

    val togglePlay = {
        if (player.timeControlStatus == AVPlayerTimeControlStatusPlaying) {
            player.pause()
            isPlaying = false
        } else {
            player.play()
            isPlaying = true
        }
    }
    val onSliderChange = { pos: Float ->
        dragging = true
        dragPos = pos
    }
    val onSliderFinished = {
        if (duration > 0f) {
            val target = (dragPos * duration).toDouble()
            player.seekToTime(CMTimeMakeWithSeconds(target, 600))
            currentTime = target.toFloat()
        }
        dragging = false
    }
    val enterFullscreen = {
        val rotated = requestInterfaceOrientation(landscape = true)
        showRotateHint = !rotated
        fullscreen = true
    }
    val exitFullscreen = {
        requestInterfaceOrientation(landscape = false)
        fullscreen = false
        showRotateHint = false
    }

    MvVideoContent(
        player = player,
        showOverlay = showOverlay,
        isPlaying = isPlaying,
        currentTime = currentTime,
        duration = duration,
        dragging = dragging,
        dragPos = dragPos,
        isFullscreen = false,
        onToggleOverlay = { showOverlay = !showOverlay },
        onTogglePlay = togglePlay,
        onSliderChange = onSliderChange,
        onSliderFinished = onSliderFinished,
        onToggleFullscreen = enterFullscreen
    )

    if (fullscreen) {
        // 全屏展示：同一 AVPlayer 实例挂到新 layer，播放进度无缝延续
        Dialog(
            onDismissRequest = exitFullscreen,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                MvVideoContent(
                    player = player,
                    showOverlay = showOverlay,
                    isPlaying = isPlaying,
                    currentTime = currentTime,
                    duration = duration,
                    dragging = dragging,
                    dragPos = dragPos,
                    isFullscreen = true,
                    onToggleOverlay = { showOverlay = !showOverlay },
                    onTogglePlay = togglePlay,
                    onSliderChange = onSliderChange,
                    onSliderFinished = onSliderFinished,
                    onToggleFullscreen = exitFullscreen
                )
                if (showRotateHint) {
                    Text(
                        text = "请旋转设备",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 60.dp)
                    )
                    LaunchedEffect(showRotateHint) {
                        delay(3000)
                        showRotateHint = false
                    }
                }
            }
        }
    }

    DisposableEffect(url) {
        onDispose { player.pause() }
    }
}

/** 视频画面 + 控制层：内嵌与全屏 Dialog 共用同一套实现 */
@OptIn(ExperimentalForeignApi::class)
@Composable
private fun MvVideoContent(
    player: AVPlayer,
    showOverlay: Boolean,
    isPlaying: Boolean,
    currentTime: Float,
    duration: Float,
    dragging: Boolean,
    dragPos: Float,
    isFullscreen: Boolean,
    onToggleOverlay: () -> Unit,
    onTogglePlay: () -> Unit,
    onSliderChange: (Float) -> Unit,
    onSliderFinished: () -> Unit,
    onToggleFullscreen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(player) {
                detectTapGestures { onToggleOverlay() }
            }
    ) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PlayerContainerView().also { container ->
                    val layer = AVPlayerLayer()
                    layer.player = player
                    layer.videoGravity = AVLayerVideoGravityResizeAspect
                    layer.frame = container.bounds
                    container.layer.addSublayer(layer)
                }
            },
            update = { container ->
                val sublayer = container.layer.sublayers?.firstOrNull() as? AVPlayerLayer
                if (sublayer != null && sublayer.player != player) {
                    sublayer.player = player
                }
            }
        )
        if (showOverlay) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(onClick = onTogglePlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "播放/暂停",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Icon(
                    imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    contentDescription = "全屏",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(28.dp)
                        .clickable(onClick = onToggleFullscreen)
                )
                val progress = if (duration > 0f) (currentTime / duration).coerceIn(0f, 1f) else 0f
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMvTime(currentTime),
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Slider(
                        value = if (dragging) dragPos else progress,
                        onValueChange = onSliderChange,
                        onValueChangeFinished = onSliderFinished,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = formatMvTime(duration),
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

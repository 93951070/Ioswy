package me.wcy.music.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.wcy.music.R
import me.wcy.music.common.bean.SongData
import me.wcy.music.main.playing.PlayingActivity
import me.wcy.music.shared.lrc.LrcLine
import me.wcy.music.shared.lrc.findCurrentLrcIndex
import me.wcy.music.shared.lrc.parseLrc
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.utils.toSongData
import kotlin.math.roundToInt

/**
 * 桌面歌词悬浮窗：跨 App 显示当前歌词行，可拖动、点歌词进播放页、可关闭。
 * 由 MusicService 持有，播放期间常驻前台。
 */
class LyricFloatWindow(
    private val context: Context,
    private val controller: PlayerController
) {
    @Suppress("DEPRECATION")
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    @Suppress("DEPRECATION")
    private val lp = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = (context.resources.displayMetrics.widthPixels * 0.24f).roundToInt()
        y = (context.resources.displayMetrics.heightPixels * 0.33f).roundToInt()
    }

    private var shown = false
    private var root: View? = null
    private var textView: TextView? = null
    private var currentSong: SongData? = null
    private var lrcLines: List<LrcLine> = emptyList()
    private var obsJob: Job? = null
    private var lrcJob: Job? = null

    fun show() {
        if (shown) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(context)) {
            Toast.makeText(context, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show()
            return
        }
        shown = true
        inflate()
        observe()
    }

    fun hide() {
        if (!shown) return
        shown = false
        obsJob?.cancel()
        lrcJob?.cancel()
        root?.let { windowManager.removeView(it) }
        root = null
        textView = null
        currentSong = null
        lrcLines = emptyList()
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams", "DiscouragedApi")
    private fun inflate() {
        val view = LayoutInflater.from(context).inflate(R.layout.floating_lyrics, null)
        val tv = view.findViewById<TextView>(R.id.floating_lyrics_text)
        view.alpha = 0.92f
        (view.background as? GradientDrawable)?.setCornerRadius(20 * context.resources.displayMetrics.density)
        view.setOnClickListener {
            val intent = Intent(context, PlayingActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        view.findViewById<View>(R.id.floating_lyrics_close).setOnClickListener {
            hide()
            Toast.makeText(context, "桌面歌词已关闭", Toast.LENGTH_SHORT).show()
        }

        var downRawX = 0f
        var downRawY = 0f
        var startX = 0
        var startY = 0
        var dragging = false
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    downRawX = event.rawX
                    downRawY = event.rawY
                    startX = lp.x
                    startY = lp.y
                    dragging = false
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downRawX).roundToInt()
                    val dy = (event.rawY - downRawY).roundToInt()
                    if (dx * dx + dy * dy > 16) dragging = true
                    if (dragging) {
                        lp.x = startX + dx
                        lp.y = startY + dy
                        windowManager.updateViewLayout(view, lp)
                    }
                    true
                }
                else -> false
            }
        }
        windowManager.addView(view, lp)
        textView = tv
        root = view
    }

    private fun observe() {
        obsJob?.cancel()
        obsJob = scope.launch {
            controller.currentSong
                .onEach { item ->
                    val song = item?.toSongData()
                    currentSong = song
                    lrcLines = emptyList()
                    textView?.text = song?.let { "${it.name} - ${it.ar.firstOrNull()?.name ?: ""}" }
                    fetchLrc(song)
                }
                .launchIn(scope)
            controller.playProgress
                .onEach { progress ->
                    val text = currentLrcLine(lrcLines, progress)
                    textView?.text = text ?: currentSong?.let { "${it.name} - ${it.ar.firstOrNull()?.name ?: ""}" }
                }
                .launchIn(scope)
        }
    }

    private fun fetchLrc(song: SongData?) {
        lrcJob?.cancel()
        val id = song?.id ?: return
        lrcJob = scope.launch {
            val lrc = runCatching { DiscoverNet.getLrc(id) }.getOrNull()
            lrcLines = if (lrc?.code == 200 && lrc.lrc.isValid()) parseLrc(lrc.lrc.lyric) else emptyList()
        }
    }

    private fun currentLrcLine(lines: List<LrcLine>, progressMs: Long): String? {
        if (lines.isEmpty()) return null
        val text = lines.getOrNull(findCurrentLrcIndex(lines, progressMs))?.text
        return text?.takeIf { it.isNotBlank() }
    }

    fun destroy() {
        hide()
        scope.cancel()
    }
}

package me.wcy.music.dj

fun formatDuration(milli: Long): String {
    val totalSeconds = (milli / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

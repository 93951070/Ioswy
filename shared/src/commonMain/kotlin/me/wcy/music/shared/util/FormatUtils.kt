package me.wcy.music.shared.util

fun formatPlayCount(num: Long): String {
    return when {
        num < 100000 -> num.toString()
        num < 100000000 -> "${num / 10000}万"
        else -> "${num / 100000000}亿"
    }
}

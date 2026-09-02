package me.wcy.music.shared.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatPlayCount(num: Long): String {
    return when {
        num < 100000 -> num.toString()
        num < 100000000 -> "${num / 10000}万"
        else -> "${num / 100000000}亿"
    }
}

/**
 * 毫秒时间戳 -> 当年 MM-dd HH:mm，往年 yyyy-MM-dd。
 */
fun formatMsgTime(milli: Long): String {
    if (milli <= 0) return ""
    val dateTime = Instant.fromEpochMilliseconds(milli)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return if (dateTime.year == Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).year
    ) {
        "${dateTime.monthNumber.toString().padStart(2, '0')}-" +
            "${dateTime.dayOfMonth.toString().padStart(2, '0')} " +
            "${dateTime.hour.toString().padStart(2, '0')}:" +
            dateTime.minute.toString().padStart(2, '0')
    } else {
        "${dateTime.year}-" +
            "${dateTime.monthNumber.toString().padStart(2, '0')}-" +
            dateTime.dayOfMonth.toString().padStart(2, '0')
    }
}

package me.wcy.music.shared.lrc

private val LRC_LINE_REGEX = Regex("""\[(\d{1,2}):(\d{1,2})(?:[.:](\d{1,3}))?\]""")

data class LrcLine(val timeMs: Long, val text: String)

fun parseLrc(content: String): List<LrcLine> {
    val lines = mutableListOf<LrcLine>()
    content.lineSequence().forEach { raw ->
        val tags = LRC_LINE_REGEX.findAll(raw).toList()
        if (tags.isEmpty()) return@forEach
        val text = raw.substring(tags.last().range.last + 1).trim()
        if (text.isEmpty()) return@forEach
        tags.forEach { m ->
            val min = m.groupValues[1].toLong()
            val sec = m.groupValues[2].toLong()
            val frac = m.groupValues[3]
            val fracMs = when (frac.length) {
                0 -> 0L
                1 -> frac.toLong() * 100
                2 -> frac.toLong() * 10
                else -> frac.take(3).toLong()
            }
            lines += LrcLine(min * 60_000 + sec * 1000 + fracMs, text)
        }
    }
    return lines.sortedBy { it.timeMs }
}

fun findCurrentLrcIndex(lines: List<LrcLine>, progressMs: Long): Int {
    return lines.indexOfLast { it.timeMs <= progressMs }.coerceAtLeast(0)
}

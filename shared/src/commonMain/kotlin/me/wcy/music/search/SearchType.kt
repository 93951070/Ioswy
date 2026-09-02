package me.wcy.music.search

/**
 * 搜索类型，对应 /cloudsearch 的 type 参数。
 */
enum class SearchType(val apiType: Int, val label: String) {
    SONG(1, "单曲"),
    ARTIST(100, "歌手"),
    ALBUM(10, "专辑"),
    PLAYLIST(1000, "歌单"),
    MV(1004, "MV"),
    RADIO(1009, "电台"),
    USER(1002, "用户")
}

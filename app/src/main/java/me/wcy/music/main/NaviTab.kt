package me.wcy.music.main

sealed class NaviTab(val name: String) {
    object Discover : NaviTab("发现")
    object Mine : NaviTab("我的")
}

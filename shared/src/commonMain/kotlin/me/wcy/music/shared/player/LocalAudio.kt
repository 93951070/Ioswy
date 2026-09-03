package me.wcy.music.shared.player

/** 删除本地音频文件（本地音乐列表删除用），成功 true */
expect fun deleteLocalAudio(path: String): Boolean

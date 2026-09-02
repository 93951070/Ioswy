package me.wcy.music.net.datasource

import android.net.Uri
import kotlinx.coroutines.runBlocking
import me.wcy.music.shared.net.DiscoverNet
import me.wcy.music.storage.preference.ConfigPreferences
import me.wcy.music.shared.net.apiCall

/**
 * Created by wangchenyan.top on 2024/3/26.
 */
object OnlineMusicUriFetcher {

    fun fetchPlayUrl(uri: Uri): String {
        val songId = uri.getQueryParameter("id")?.toLongOrNull() ?: return uri.toString()
        return runBlocking {
            val res = apiCall {
                DiscoverNet
                    .getSongUrl(songId, ConfigPreferences.playSoundQuality)
            }

            if (res.isSuccessWithData() && res.getDataOrThrow().isNotEmpty()) {
                return@runBlocking res.getDataOrThrow().first().url
            } else {
                return@runBlocking ""
            }
        }
    }
}
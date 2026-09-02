package me.wcy.music.compose.extension

import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.common.bean.SongData
import me.wcy.music.storage.db.entity.SongEntity
import me.wcy.music.utils.getDuration
import me.wcy.music.utils.getLargeCover
import me.wcy.music.utils.getSmallCover
import me.wcy.music.utils.getSongType
import androidx.media3.common.MediaItem

fun SongData.smallCover(): String = al.getSmallCover()

fun SongData.largeCover(): String = al.getLargeCover()

fun SongData.artistName(): String = ar.joinToString("/") { it.name }

fun PlaylistData.smallCover(): String = getSmallCover()

fun PlaylistData.largeCover(): String = getLargeCover()

fun MediaItem.smallCover(): String = getSmallCover()

fun MediaItem.largeCover(): String = getLargeCover()

fun MediaItem.songTitle(): String = mediaMetadata.title?.toString() ?: ""

fun MediaItem.songArtist(): String = mediaMetadata.artist?.toString() ?: ""

fun MediaItem.durationMs(): Long = mediaMetadata.getDuration()

fun MediaItem.isLocal(): Boolean = getSongType() == SongEntity.LOCAL

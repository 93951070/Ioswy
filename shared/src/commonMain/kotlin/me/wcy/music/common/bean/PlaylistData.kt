package me.wcy.music.common.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.shared.util.CoverUtils.asLargeCover
import me.wcy.music.shared.util.CoverUtils.asSmallCover

@Serializable(with = PlaylistDataJson::class)
data class PlaylistData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @Deprecated("Please use resized url")
    @SerialName("coverImgUrl")
    val coverImgUrl: String = "",
    @SerialName("creator")
    val creator: ProfileData = ProfileData(),
    @SerialName("subscribed")
    val subscribed: Boolean = false,
    @SerialName("trackCount")
    val trackCount: Int = 0,
    @SerialName("userId")
    val userId: Long = 0,
    @SerialName("playCount")
    val playCount: Long = 0,
    @SerialName("bookCount")
    val bookCount: Long = 0,
    @SerialName("specialType")
    val specialType: Int = 0,
    @SerialName("description")
    val description: String = "",
    @SerialName("tags")
    val tags: List<String> = emptyList(),
    @SerialName("highQuality")
    val highQuality: Boolean = false,
    @SerialName("updateFrequency")
    val updateFrequency: String = "",
    @SerialName("ToplistType")
    val toplistType: String = "",
) {
    @SerialName("_songList")
    var songList: List<SongData> = emptyList()

    fun getSmallCover(): String {
        return coverImgUrl.asSmallCover()
    }

    fun getLargeCover(): String {
        return coverImgUrl.asLargeCover()
    }
}
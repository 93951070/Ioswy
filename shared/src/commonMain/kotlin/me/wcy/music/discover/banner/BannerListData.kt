package me.wcy.music.discover.banner
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by wangchenyan.top on 2024/1/3.
 */
@Serializable
data class BannerListData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("banners")
    val banners: List<BannerData> = emptyList(),
)
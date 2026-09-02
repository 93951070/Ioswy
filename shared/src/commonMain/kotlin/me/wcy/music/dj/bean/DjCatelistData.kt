package me.wcy.music.dj.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DjCatelistData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("categories")
    val categories: List<Category> = emptyList()
) {
    @Serializable
    data class Category(
        @SerialName("id")
        val id: Long = 0,
        @SerialName("name")
        val name: String = "",
        @SerialName("picUrl")
        val picUrl: String = ""
    )
}

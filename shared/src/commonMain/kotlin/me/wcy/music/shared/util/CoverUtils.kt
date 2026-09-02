package me.wcy.music.shared.util

object CoverUtils {
    fun String.asSmallCover(): String {
        return appendImageSize(200)
    }

    fun String.asLargeCover(): String {
        return appendImageSize(800)
    }

    private fun String.appendImageSize(size: Int): String {
        return if (contains("?")) {
            "$this&param=${size}y${size}"
        } else {
            "$this?param=${size}y${size}"
        }
    }
}

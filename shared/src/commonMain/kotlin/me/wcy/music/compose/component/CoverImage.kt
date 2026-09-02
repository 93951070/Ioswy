package me.wcy.music.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun CoverImage(
    url: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    placeholderColor: Color = Color(0xFFE5E5E5)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(placeholderColor),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNotBlank()) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun CoverImage(size: Dp, url: String, cornerRadius: Dp = 8.dp, contentDescription: String? = null) {
    CoverImage(
        url = url,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        cornerRadius = cornerRadius
    )
}

package me.wcy.music.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Red500,
    onPrimary = Color.White,
    primaryContainer = Red500P30,
    onPrimaryContainer = Red700,
    secondary = Grey,
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = TextH1,
    background = BackgroudColor,
    onBackground = TextH1,
    surfaceVariant = Grey100,
    onSurfaceVariant = Grey,
    tertiary = SearchBarBg,
    error = Red500,
    outline = Grey300,
    outlineVariant = Grey100
)

private val DarkColors = darkColorScheme(
    primary = Red700,
    onPrimary = Color.White,
    primaryContainer = Red500,
    onPrimaryContainer = Color.White,
    secondary = Grey,
    onSecondary = Color.Black,
    surface = Color.Black,
    onSurface = Color.White,
    background = NightBackground,
    onBackground = Color.White,
    surfaceVariant = Grey800,
    onSurfaceVariant = NightTextH2,
    tertiary = NightSearchBarBg,
    error = Red700,
    outline = Grey800,
    outlineVariant = Grey800
)

object AppThemeColor {
    val ThemeColor
        @Composable get() = MaterialTheme.colorScheme.primary

    val Background
        @Composable get() = MaterialTheme.colorScheme.background

    val TextH1
        @Composable get() = MaterialTheme.colorScheme.onSurface

    val TextH2
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val Divider
        @Composable get() = MaterialTheme.colorScheme.outlineVariant

    val Card
        @Composable get() = MaterialTheme.colorScheme.surface

    val PlayBar
        @Composable get() = MaterialTheme.colorScheme.surface

    val SearchBar
        @Composable get() = MaterialTheme.colorScheme.tertiary
}

@Composable
fun MusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

package app

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// define our custom palettes
val GrayColorPalette = lightColors(
    primary = Color(0xFF616161),
    primaryVariant = Color(0xFF373737),
    secondary = Color(0xFF757575),
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFE0E0E0),
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

val DarkColorPalette = darkColors(
    primary = Color(0xFF9E9E9E),
    primaryVariant = Color(0xFF616161),
    secondary = Color(0xFFBDBDBD),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

val LightBlueColorPalette = lightColors(
    primary = Color(0xFF1976D2),
    primaryVariant = Color(0xFF004BA0),
    secondary = Color(0xFF64B5F6),
    background = Color(0xFFF3F8FF),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

enum class AppTheme {
    GRAY, DARK, BLUE
}

// local provider for the current theme state
val LocalThemeState = compositionLocalOf<MutableState<AppTheme>> {
    error("theme state not provided")
}

@Composable
fun FinanceManagerTheme(
    theme: AppTheme,
    content: @Composable () -> Unit
) {
    val colors = when (theme) {
        AppTheme.GRAY -> GrayColorPalette
        AppTheme.DARK -> DarkColorPalette
        AppTheme.BLUE -> LightBlueColorPalette
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
}
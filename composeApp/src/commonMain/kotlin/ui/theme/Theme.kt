package ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.lightColors
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import model.user.User

// default gray theme based on your mockups
val DefaultGrayColors = lightColors(
    primary = Color(0xFF4A4A4A),
    primaryVariant = Color(0xFF333333),
    secondary = Color(0xFF757575),
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFE0E0E0),
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// optional blue theme for settings demonstration
val BlueColors = lightColors(
    primary = Color(0xFF1976D2),
    primaryVariant = Color(0xFF115293),
    secondary = Color(0xFF2196F3),
    background = Color(0xFFF0F4F8),
    surface = Color(0xFFE3F2FD),
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// composition local to pass current logged in user globally to tabs
val LocalCurrentUser = staticCompositionLocalOf<User> {
    error("user not provided")
}
package app

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import ui.login.LoginScreen
import ui.theme.DefaultGrayColors
import ui.theme.BlueColors

@Composable
fun App() {
    // we hold the current theme state here at the very top
    var isBlueTheme by remember { mutableStateOf(false) }
    val colors = if (isBlueTheme) BlueColors else DefaultGrayColors

    MaterialTheme(colors = colors) {
        // we pass the theme toggle callback down via screen params if needed,
        // but for voyager it's easier to use a simple static object or pass it via constructor.
        // here we initialize standard login screen
        Navigator(LoginScreen { newThemeStatus ->
            isBlueTheme = newThemeStatus
        })
    }
}
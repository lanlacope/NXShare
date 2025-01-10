package io.github.lanlacope.nxsharinghelper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.clazz.ThemeManager
import io.github.lanlacope.nxsharinghelper.clazz.propaty.ThemeJsonPropaty

private val LightColorScheme = lightColorScheme(
    primary = PaupleDark,
    secondary = BlueDark,
    tertiary = BlueLight,
    background = White,
    onBackground = Black,
    error = RedDark
)

private val DarkColorScheme = darkColorScheme(
    primary = PaupleLight,
    secondary = BlueLight,
    tertiary = BlueDark,
    background = Black,
    onBackground = White,
    error = RedLight
)

private val updateThemeKey: MutableState<Int> = mutableStateOf(0)

fun updateTheme() {
    updateThemeKey.value++
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val themeManager = ThemeManager(LocalContext.current)

    var theme by remember { mutableStateOf(ThemeJsonPropaty.THEME_SYSTEM) }

    LaunchedEffect(Unit) {
        theme = themeManager.getAppTheme()
    }

    LaunchedEffect(updateThemeKey.value) {
        theme = themeManager.getAppTheme()
    }

    val colorScheme = when (theme) {
        ThemeJsonPropaty.THEME_LIGHT -> LightColorScheme
        ThemeJsonPropaty.THEME_DARK -> DarkColorScheme
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    /*
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }
     */

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
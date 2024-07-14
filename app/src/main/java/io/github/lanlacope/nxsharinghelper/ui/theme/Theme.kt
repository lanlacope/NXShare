package io.github.lanlacope.nxsharinghelper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.clazz.SettingManager
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SETTING_JSON_PROPATY

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
    onError = RedLight
)

@Composable
fun NXSharingHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    var colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    when (SettingManager(LocalContext.current).getAppTheme()) {
        SETTING_JSON_PROPATY.THEME_LIGHT -> LightColorScheme
        SETTING_JSON_PROPATY.THEME_DARK -> DarkColorScheme
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
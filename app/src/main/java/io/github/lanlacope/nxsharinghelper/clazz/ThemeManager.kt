package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.lanlacope.nxsharinghelper.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

@Suppress("unused")
@Composable
fun rememberThemeManager(): ThemeManager {
    val context = LocalContext.current
    return remember {
        ThemeManager(context)
    }
}

/*
 * アプリテーマの管理を行う
 *
 */
@Stable
class ThemeManager(private val context: Context) {

    private val THEME_KEY = stringPreferencesKey("theme_type")
    private val Context.dataStore by preferencesDataStore(name = "theme")

    private val themeFile by lazy {
        val file = File(context.filesDir, "theme.json")
        file.createNewFile()
        file
    }

    suspend fun getAppTheme(): AppTheme = withContext(Dispatchers.IO) {
        val themeString = context.dataStore.data.map { data ->
            data[THEME_KEY] ?: AppTheme.SYSTEM.toString()
        }.first()

        return@withContext AppTheme.fromString(themeString)
    }

    suspend fun changeAppTheme(theme: AppTheme) = withContext(Dispatchers.IO) {
        context.dataStore.edit { data ->
            data[THEME_KEY] = theme.toString()
        }
    }
}

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK;

    fun getStringResource(context: Context): String {
        return when(this) {
            SYSTEM -> context.getString(R.string.setting_theme_value_system)
            LIGHT -> context.getString(R.string.setting_theme_value_light)
            DARK -> context.getString(R.string.setting_theme_value_dark)
        }
    }

    override fun toString(): String {
        return when(this) {
            SYSTEM -> "SystemTheme"
            LIGHT -> "LightTheme"
            DARK -> "DarkTheme"
        }
    }

    companion object {
        fun fromString(value: String): AppTheme {
            return entries.find { it.toString() == value } ?: SYSTEM
        }
    }
}
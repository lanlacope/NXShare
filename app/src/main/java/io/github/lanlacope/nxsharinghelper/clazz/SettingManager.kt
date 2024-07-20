package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.activity.component.recompositionKey
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SettingJsonPropaty
import org.json.JSONException
import org.json.JSONObject

@Suppress("unused")
@Composable
fun rememberSettingManager(): SettingManager {
    val context = LocalContext.current
    return remember {
        SettingManager(context)
    }
}

@Stable
class SettingManager(context: Context) : FileSelector(context) {

    fun getAppTheme(): String {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: JSONException) {
            JSONObject()
        }

        try {
            return jsonObject.getString(SettingJsonPropaty.APP_THEME)
        } catch (e: JSONException) {
            return SettingJsonPropaty.THEME_SYSTEM
        }
    }

    fun changeAppTheme(theme: String) {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: JSONException) {
            JSONObject()
        }

        jsonObject.put(SettingJsonPropaty.APP_THEME, theme)

        file.writeText(jsonObject.toString())
    }

    fun getAlternativeConnectionEnabled(): Boolean {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: JSONException) {
            JSONObject()
        }

        try {
            return jsonObject.getBoolean(SettingJsonPropaty.ALTERNATIVE_CONNECTION_ENABlED)
        } catch (e: JSONException) {
            return false
        }
    }

    fun changeAlternativeConnectionEnabled(isEnabled: Boolean) {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: JSONException) {
            JSONObject()
        }

        jsonObject.put(SettingJsonPropaty.ALTERNATIVE_CONNECTION_ENABlED, isEnabled)

        file.writeText(jsonObject.toString())
    }
}
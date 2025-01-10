package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.clazz.propaty.SettingJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.ThemeJsonPropaty
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

/*
 * アプリ設定の管理を行う
 *
 */
@Stable
class SettingManager(context: Context) : FileSelector(context) {

    fun getAlternativeConnectionEnabled(): Boolean {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: JSONException) {
            JSONObject()
        }

        return try {
            jsonObject.getBoolean(SettingJsonPropaty.FOR_LEGACY)
        } catch (e: JSONException) {
            false
        }
    }

    fun changeAlternativeConnectionEnabled(isEnabled: Boolean) {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: JSONException) {
            JSONObject()
        }

        jsonObject.put(SettingJsonPropaty.FOR_LEGACY, isEnabled)

        file.writeText(jsonObject.toString())
    }
}
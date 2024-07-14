package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SETTING_JSON_PROPATY
import org.json.JSONObject

class SettingManager(context: Context) : FileSelector(context) {

    fun getAppTheme(): String {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            JSONObject()
        }

        try {
            return jsonObject.getString(SETTING_JSON_PROPATY.APP_THEME)
        } catch (e: Exception) {
            return SETTING_JSON_PROPATY.THEME_SYSTEM
        }
    }

    fun changeAppTheme(theme: String) {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            JSONObject()
        }

        jsonObject.put(SETTING_JSON_PROPATY.APP_THEME, theme)

        file.writeText(jsonObject.toString())
    }

    fun getAlternativeConnectionEnabled(): Boolean {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            JSONObject()
        }

        try {
            return jsonObject.getBoolean(SETTING_JSON_PROPATY.ALTERNATIVE_CONNECTION_ENABlED)
        } catch (e: Exception) {
            return false
        }
    }

    fun changeAlternativeConnectionEnabled(isEnabled: Boolean) {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            JSONObject()
        }

        jsonObject.put(SETTING_JSON_PROPATY.ALTERNATIVE_CONNECTION_ENABlED, isEnabled)

        file.writeText(jsonObject.toString())
    }
}
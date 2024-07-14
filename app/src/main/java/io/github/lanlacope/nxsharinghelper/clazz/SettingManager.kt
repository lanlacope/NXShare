package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SETTING_JSON_PROPATY
import org.json.JSONObject

class SettingManager(context: Context) : FileSelector(context) {

    fun getSubstituteConnectionEnabled(): Boolean {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            JSONObject()
        }

        try {
            return jsonObject.getBoolean(SETTING_JSON_PROPATY.SUBSTITUTE_CONNECTION_ENABlED)
        } catch (e: Exception) {
            return false
        }
    }

    fun changeSubstituteConnectionEnabled(isEnabled: Boolean) {
        val file = getSettingFile()
        val jsonObject = try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            JSONObject()
        }

        jsonObject.put(SETTING_JSON_PROPATY.SUBSTITUTE_CONNECTION_ENABlED, isEnabled)

        file.writeText(jsonObject.toString())
    }
}
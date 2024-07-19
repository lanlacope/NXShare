package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.APP_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.GAME_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.clazz.propaty.forEachIndexOnly
import io.github.lanlacope.nxsharinghelper.clazz.propaty.getGameId
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@Suppress("unused")
@Composable
fun rememberFileReader(): FileReader {
    val context = LocalContext.current
    return remember {
        FileReader(context)
    }
}

@Immutable
class FileReader(context: Context) : FileSelector(context) {

    fun getShareEnabled(packageName: String): Boolean {
        try {
            val file = getAppDataFile()
            val jsonArray = JSONArray(file.readText())
            jsonArray.forEachIndexOnly { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                if (jsonObject.getString(APP_JSON_PROPATY.PACKAGE_NAME) == packageName) {

                    return jsonObject.getBoolean(APP_JSON_PROPATY.PAKCAGE_ENABLED)
                }
            }
        } catch (e: JSONException) {
            return false
        }
        return false
    }

    fun getShareType(packageName: String): String? {
        try {
            val file = getAppDataFile()
            val jsonArray = JSONArray(file.readText())
            jsonArray.forEachIndexOnly { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                if (jsonObject.getString(APP_JSON_PROPATY.PACKAGE_NAME) == packageName) {
                    return jsonObject.getString(APP_JSON_PROPATY.PACKAGE_TYPE)
                }
            }
        } catch (e: JSONException) {
            return null
        }
        return null
    }

    // ファイルの表示用名 + 非選択用名
    fun getTypeNames(): List<String> {
        val defaultType = listOf(APP_JSON_PROPATY.TYPE_NONE)
        try {
            val files = getMySetFiles()
            val types = files.map { file ->
                val jsonObject = JSONObject(file.readText())
                jsonObject.getString(GAME_JSON_PROPATY.COMMON_TITLE)
            }
            return defaultType + types
        } catch (e: JSONException) {
            return defaultType
        }
    }

    fun createCopyText(fileNames: List<String>, packageName: String): String? {
        try {
            val ids = getGameId(fileNames)
            val typeName = getShareType(packageName)
            val file = getMySetFileByTitle(typeName!!).getOrNull()

            val rawJson = JSONObject(file!!.readText())

            val resultText = StringBuilder().apply {
                try {
                    val text = rawJson.getString(GAME_JSON_PROPATY.COMMON_TEXT)
                    append(text)
                } catch (e: JSONException) {
                    // do nothing
                }
                try {
                    val arrayData = rawJson.getJSONArray(GAME_JSON_PROPATY.GAME_DATA)
                    arrayData.forEachIndexOnly { index ->
                        try {
                            val jsonObject = arrayData.getJSONObject(index)
                            if (jsonObject.getString(GAME_JSON_PROPATY.GAME_ID) in ids) {
                                val text = jsonObject.getString(GAME_JSON_PROPATY.GAME_TEXT)
                                append(text)
                            }
                        } catch (e: JSONException) {
                            // do nothing
                        }
                    }
                } catch (e: Exception) {
                    // do nothing
                }
            }
            return resultText.toString()
        } catch (e: Exception) {
            return null
        }
    }
}
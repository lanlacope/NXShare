package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.AppJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.MySetJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.getGameId
import io.github.lanlacope.nxsharinghelper.widgit.forEachIndexOnly
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
                if (jsonObject.getString(AppJsonPropaty.PACKAGE_NAME) == packageName) {

                    return jsonObject.getBoolean(AppJsonPropaty.PAKCAGE_ENABLED)
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
                if (jsonObject.getString(AppJsonPropaty.PACKAGE_NAME) == packageName) {
                    return jsonObject.getString(AppJsonPropaty.PACKAGE_TYPE)
                }
            }
        } catch (e: JSONException) {
            return null
        }
        return null
    }

    // ファイルの表示用名 + 非選択用名
    fun getTypeNames(): List<String> {
        val defaultType = listOf(AppJsonPropaty.TYPE_NONE)
        try {
            val files = getMySetFiles()
            val types = files.map { file ->
                val jsonObject = JSONObject(file.readText())
                jsonObject.getString(MySetJsonPropaty.MYSET_TITLE)
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
                    val text = rawJson.getString(MySetJsonPropaty.HEAD_TEXT)
                    append(text)
                } catch (e: JSONException) {
                    // do nothing
                }
                try {
                    val arrayData = rawJson.getJSONArray(MySetJsonPropaty.GAME_DATA)
                    arrayData.forEachIndexOnly { index ->
                        try {
                            val jsonObject = arrayData.getJSONObject(index)
                            if (jsonObject.getString(MySetJsonPropaty.GAME_ID) in ids) {
                                val text = jsonObject.getString(MySetJsonPropaty.GAME_TEXT)
                                append(text)
                            }
                        } catch (e: JSONException) {
                            // do nothing
                        }
                    }
                } catch (e: Exception) {
                    // do nothing
                }
                try {
                    val text = rawJson.getString(MySetJsonPropaty.TAIL_TEXT)
                    append(text)
                } catch (e: JSONException) {
                    // do nothing
                }
            }
            return resultText.toString()
        } catch (e: Exception) {
            return null
        }
    }
}
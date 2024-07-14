package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.APP_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.GAME_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.clazz.propaty.forEachIndexOnly
import io.github.lanlacope.nxsharinghelper.clazz.propaty.getGameId
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

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
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            return null
        }
        return null
    }

    @Suppress("unused")
    fun getTypeName(file: File): String {
        val jsonObject = JSONObject(file.readText())
        return jsonObject.getString(GAME_JSON_PROPATY.COMMON_TITLE)
    }

    // ファイルの表示用名
    @Suppress("unused")
    fun getTypeNames(): List<String> {
        try {
            val files = getMySetFiles()
            val types = files.map { file ->
                val jsonObject = JSONObject(file.readText())
                jsonObject.getString(GAME_JSON_PROPATY.COMMON_TITLE)
            }
            return types
        } catch (e: Exception) {
            return emptyList()
        }
    }

    // ファイルの表示用名 + 非選択用名
    fun getTypeNamesWithNone(): List<String> {
        val defaultType = listOf(APP_JSON_PROPATY.TYPE_NONE)
        try {
            val files = getMySetFiles()
            val types = files.map { file ->
                val jsonObject = JSONObject(file.readText())
                jsonObject.getString(GAME_JSON_PROPATY.COMMON_TITLE)
            }
            return defaultType + types
        } catch (e: Exception) {
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
                } catch (e: Exception) {
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
                        } catch (e: Exception) {
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
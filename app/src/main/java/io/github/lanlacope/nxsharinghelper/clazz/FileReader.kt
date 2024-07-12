package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class FileReader(context: Context) : FileSelector(context) {

    fun getShareEnabled(packageName: String): Boolean {
        try {
            Log.d("Q", packageName)
            val file = getAppSettingFile()
            val jsonArray = JSONArray(file.readText())
            jsonArray.forEachIndexOnly { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                if (jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == packageName) {

                    return jsonObject.getBoolean(SHARE_JSON_PROPATY.PAKCAGE_ENABLED)
                }
            }
        } catch (e: Exception) {
            Log.d("Q", e.toString())
            return false
        }
        Log.d("Q", "FUQ")
        return false
    }

    fun getShareType(packageName: String): String? {
        try {
            val file = getAppSettingFile()
            val jsonArray = JSONArray(file.readText())
            jsonArray.forEachIndexOnly { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                if (jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == packageName) {
                    return jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_TYPE)
                }
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    fun getTypeName(file: File): String {
        val jsonObject = JSONObject(file.readText())
        return jsonObject.getString(SHARE_JSON_PROPATY.COMMON_TITLE)
    }

    // ファイルの表示用名
    fun getTypeNames(): List<String> {
        try {
            val files = getMySetFiles()
            val types = files.map { file ->
                val jsonObject = JSONObject(file.readText())
                jsonObject.getString(SHARE_JSON_PROPATY.COMMON_TITLE)
            }
            return types
        } catch (e: Exception) {
            return listOf()
        }
    }

    // ファイルの表示用名 + 非選択用名
    fun getTypeNamesWithNone(): List<String> {
        val defaultType = listOf(SHARE_JSON_PROPATY.TYPE_NONE)
        try {
            val files = getMySetFiles()
            val types = files.map { file ->
                val jsonObject = JSONObject(file.readText())
                jsonObject.getString(SHARE_JSON_PROPATY.COMMON_TITLE)
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
                    val text = rawJson.getString(SHARE_JSON_PROPATY.COMMON_TEXT)
                    append(text)
                } catch (e: Exception) {
                    // do nothing
                }
                try {
                    val arrayData = rawJson.getJSONArray(SHARE_JSON_PROPATY.GAME_DATA)
                    arrayData.forEachIndexOnly { index ->
                        try {
                            val jsonObject = arrayData.getJSONObject(index)
                            if (jsonObject.getString(SHARE_JSON_PROPATY.GAME_ID) in ids) {
                                val text = jsonObject.getString(SHARE_JSON_PROPATY.GAME_TEXT)
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
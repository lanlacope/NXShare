package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class FileReader(context: Context) : FileSelector(context) {

    fun getGameHashs(fileNames: List<String>): List<String> {
        val regex = Regex(""".*-(.*?)\..*?$""")
        val hashs = fileNames.map { rawHash ->
            val matchResult = regex.find(rawHash)
            matchResult?.groupValues?.get(1) ?: ""
        }.distinct()
        return hashs
    }

    fun getShareEnabled(appInfo: AppInfo): Boolean {
        try {
            val file = getAppSettingFile()
            val jsonArray = JSONArray(file.readText())
            jsonArray.forEachIndexOnly { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                if (jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == appInfo.packageName) {
                    return jsonObject.getBoolean(SHARE_JSON_PROPATY.PAKCAGE_ENABLED)
                }
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    fun getShareType(appInfo: AppInfo): String? {
        try {
            val file = getAppSettingFile()
            val jsonArray = JSONArray(file.readText())
            jsonArray.forEachIndexOnly { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                if (jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == appInfo.packageName) {
                    return jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_TYPE)
                }
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    fun getShareType(packageName: String?): String? {
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
        return jsonObject.getString(SHARE_JSON_PROPATY.DATA_NAME)
    }

    // ファイルの表示用名
    fun getTypeNames(): List<String> {
        try {
            val files = getTypeFiles()
            val types = files.map { file ->
                val jsonObject = JSONObject(file.readText())
                jsonObject.getString(SHARE_JSON_PROPATY.DATA_NAME)
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
            val files = getTypeFiles()
            val types = files.map { file ->
                val jsonObject = JSONObject(file.readText())
                jsonObject.getString(SHARE_JSON_PROPATY.DATA_NAME)
            }
            return defaultType + types
        } catch (e: Exception) {
            return defaultType
        }
    }

    fun createCopyText(fileNames: List<String>, packageName: String): String? {
        try {
            val hashs = getGameHashs(fileNames)
            val typeName = getShareType(packageName) ?: ""
            val file = getTypeFileByType(typeName)

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
                            if (jsonObject.getString(SHARE_JSON_PROPATY.GAME_HASH) in hashs) {
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
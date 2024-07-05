package io.github.lanlacope.nxsharinghelper.classes

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class FileEditer(val context: Context) {

    private val settingFolder by lazy {
        File(context.filesDir, FOLDER_SHARE)
    }

    private val typeFolder by lazy {
        File(settingFolder, FOLDER_GAME)
    }

    fun getAppInfo(): List<AppInfo> {
        val sendJpgIntent = Intent(Intent.ACTION_SEND).apply {
            type = MINETYPE.JPG
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val sendJpgsIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = MINETYPE.JPG
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val sendMp4Intent = Intent(Intent.ACTION_SEND).apply {
            type = MINETYPE.MP4
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val sendMp4sIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = MINETYPE.MP4
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val packageManager = context.packageManager

        val receiveJpgPackages =
            packageManager.queryIntentActivities(sendJpgIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val receiveJpgsPackages =
            packageManager.queryIntentActivities(sendJpgsIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val receiveMp4Packages =
            packageManager.queryIntentActivities(sendMp4Intent, PackageManager.MATCH_DEFAULT_ONLY)
        val receiveMp4sPackages =
            packageManager.queryIntentActivities(sendMp4sIntent, PackageManager.MATCH_DEFAULT_ONLY)

        val packages =
            (receiveJpgPackages + receiveJpgsPackages + receiveMp4Packages + receiveMp4sPackages).toSet()
                .toList()

        val appInfo: MutableList<AppInfo> = mutableListOf()

        packages.forEach() { index ->
            appInfo.add(AppInfo(index, packageManager))
        }
        return appInfo
    }

    fun getCommonInfo(file: File): CommonInfo {
        val jsonObject = JSONObject(file.readText())
        return CommonInfo(jsonObject)
    }

    fun getGameInfo(file: File): List<GameInfo> {
        val jsonObject = JSONObject(file.readText())
        val jsonArray = jsonObject.getJSONArray(SHARE_JSON_PROPATY.GAME_DATA)
        val info = mutableListOf<GameInfo>()
        List(jsonArray.length()) { index ->
            val gameData = jsonArray.getJSONObject(index)
            info.add(GameInfo(gameData))
        }
        return info
    }

    // パッケージごとの設定ファイルを個別取得
    fun getAppSettingFile(): File {
        val file = File(settingFolder, FILE_APP)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    fun getShareEnabled(appInfo: AppInfo): Boolean? {
        try {
            val file = getAppSettingFile()
            val jsonArray = JSONArray(file.readText())
            List(jsonArray.length()) { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                if (jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == appInfo.packageName) {
                    return jsonObject.getBoolean(SHARE_JSON_PROPATY.PAKCAGE_ENABLED)
                }
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    fun getShareType(appInfo: AppInfo): String? {
        try {
            val file = getAppSettingFile()
            val jsonArray = JSONArray(file.readText())
            List(jsonArray.length()) { index ->
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
            List(jsonArray.length()) { index ->
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

    fun getTypeFolder(): File {
        return typeFolder
    }

    // マイセットファイルを個別取得
    fun getTypeFile(fileName: String): File {
        val file = File(typeFolder, fileName)

        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    // マイセットファイルを全て取得
    fun getTypeFiles(): List<File> {
        return typeFolder.listFiles()?.toList() ?: listOf()
    }

    // ファイルの表示用名
    fun getTypeNames(): List<String> {
        val types = mutableListOf<String>()
        try {
            val files = FileEditer(context).getTypeFiles()
            files.forEach() { file ->
                val jsonObject = JSONObject(file.readText())
                types.add(jsonObject.getString(SHARE_JSON_PROPATY.DATA_NAME))
            }
            return types
        } catch (e: Exception) {
            // do nothing
        }
        return types
    }

    // // ファイルの表示用名 + 非選択用名
    fun getTypeNamesWithNone(): List<String> {
        val types = mutableListOf<String>()
        types.add(SHARE_JSON_PROPATY.TYPE_NONE)
        try {
            val files = FileEditer(context).getTypeFiles()
            files.forEach() { file ->
                val jsonObject = JSONObject(file.readText())
                types.add(jsonObject.getString(SHARE_JSON_PROPATY.DATA_NAME))
            }
        } catch (e: Exception) {
            // do nothing
        }
        return types
    }

    fun createCopyText(hash: String, type: String): String? {
        try {
            val file = getTypeFile(type)
            val rawJson = JSONObject(file.readText())

            return StringBuilder().apply {
                try {
                    val text = rawJson.getString(SHARE_JSON_PROPATY.COMMON_TEXT)
                    append(text)
                } catch (e: Exception) {
                    // do nothing
                }
                try {
                    val arrayData = rawJson.getJSONArray(SHARE_JSON_PROPATY.GAME_DATA)
                    List(arrayData.length()) { index ->
                        val partJson = arrayData.getJSONObject(index)
                        if (partJson.getString(SHARE_JSON_PROPATY.GAME_HASH) == hash) {
                            val text = partJson.getString(SHARE_JSON_PROPATY.GAME_TEXT)
                            append(text)
                        }
                    }
                } catch (e: Exception) {
                    // do nothing
                }
            }.toString()
        } catch (e: Exception) {
            return null
        }
    }
}
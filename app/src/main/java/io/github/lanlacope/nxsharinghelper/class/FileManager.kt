package io.github.lanlacope.nxsharinghelper.`class`

import android.content.Context
import android.content.pm.PackageManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class FileManager(val context: Context) {

    fun getAppInfo(): List<AppInfo> {
        val contentsSharer = ContentsSharer(context)

        val sendJpgIntent = contentsSharer.createSendableIntent(
            DownloadData(
                fileType = DOWNLOAD_JSON_PROPATY.FILETYPE_PHOTO,
                fileNames = listOf("a.jpg")
            )
        )
        val sendJpgsIntent = contentsSharer.createSendableIntent(
            DownloadData(
                fileType = DOWNLOAD_JSON_PROPATY.FILETYPE_PHOTO,
                fileNames = listOf("a.jpg", "b.jpg")
            )
        )
        val sendMp4Intent = contentsSharer.createSendableIntent(
                DownloadData(
                    fileType = DOWNLOAD_JSON_PROPATY.FILETYPE_MOVIE,
                    fileNames = listOf("a.mp4")
                )
        )

        val packageManager = context.packageManager

        val receiveJpgPackages =
            packageManager.queryIntentActivities(sendJpgIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val receiveJpgsPackages =
            packageManager.queryIntentActivities(sendJpgsIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val receiveMp4Packages =
            packageManager.queryIntentActivities(sendMp4Intent, PackageManager.MATCH_DEFAULT_ONLY)

        val allPackages = receiveJpgPackages + receiveJpgsPackages + receiveMp4Packages

        val parsedPackageNames = allPackages.map { it.activityInfo.packageName }.toSet()

        val appInfo = parsedPackageNames.map { packageName ->
            val resolveInfo = allPackages.first { rawPackage ->
                rawPackage.activityInfo.packageName == packageName
            }
            AppInfo(resolveInfo, packageManager)
        }.toList()

        return appInfo
    }

    fun getTypeInfo(): List<TypeInfo> {
        val typeInfo = mutableListOf<TypeInfo>()
        getTypeFiles().forEach() { file ->
            typeInfo.add(TypeInfo(file, getTypeName(file)))
        }
        return typeInfo
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

    fun getGameHashs(rawHashs: List<String>): List<String> {
        val hashs = mutableListOf<String>()
        val regex = Regex("""-(.*?)\.(.*?)$""")
        rawHashs.forEach { rawHash ->
            val matchResult = regex.find(rawHash)
            val hash = matchResult?.groupValues?.get(1) ?: ""
            hashs.add(hash)
        }
        return hashs.toSet().toList()
    }

    fun getSettingFolder(): File {
        val file = File(context.filesDir, FOLDER_SHARE)
        if (!file.exists()) {
            file.mkdir()
        }
        return file
    }

    fun getTypeFolder(): File {
        val file =  File(getSettingFolder(), FOLDER_GAME)
        if (!file.exists()) {
            file.mkdir()
        }
        return file
    }

    // パッケージごとの設定ファイルを個別取得
    fun getAppSettingFile(): File {
        val file = File(getSettingFolder(), FILE_APP)
        file.createNewFile()
        return file
    }

    fun getShareEnabled(appInfo: AppInfo): Boolean {
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
            return false
        }
        return false
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

    // マイセットファイルを個別取得
    fun getTypeFile(fileName: String): File {
        val file = File(getTypeFolder(), fileName)
        file.createNewFile()
        return file
    }

    fun getNewTypeFile(fileName: String): Result<File> {
        val file = File(getTypeFolder(), fileName)
        val isSucces = file.createNewFile()

        if (isSucces) {
            return Result.success(file)
        } else {
            return Result.failure(Exception())
        }
    }

    // マイセットファイルを全て取得
    fun getTypeFiles(): List<File> {
        return getTypeFolder().listFiles()?.toList() ?: listOf()
    }

    fun getTypeName(file: File): String {
        val jsonObject = JSONObject(file.readText())
        return (jsonObject.getString(SHARE_JSON_PROPATY.DATA_NAME))
    }

    // ファイルの表示用名
    fun getTypeNames(): List<String> {
        val types = mutableListOf<String>()
        try {
            val files = FileManager(context).getTypeFiles()
            files.forEach { file ->
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
            val files = FileManager(context).getTypeFiles()
            files.forEach { file ->
                val jsonObject = JSONObject(file.readText())
                types.add(jsonObject.getString(SHARE_JSON_PROPATY.DATA_NAME))
            }
        } catch (e: Exception) {
            // do nothing
        }
        return types
    }

    fun createCopyText(rawHashs: List<String>, type: String): String? {
        try {
            val hashs = getGameHashs(rawHashs)
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
                    val texts = mutableListOf<String>()
                    val arrayData = rawJson.getJSONArray(SHARE_JSON_PROPATY.GAME_DATA)
                    List(arrayData.length()) { index ->
                        val partJson = arrayData.getJSONObject(index)
                        if (partJson.getString(SHARE_JSON_PROPATY.GAME_HASH) in hashs) {
                            texts.add(partJson.getString(SHARE_JSON_PROPATY.GAME_TEXT))
                        }
                    }
                    texts.toSet().forEach { text ->
                        append(text)
                    }
                } catch (e: Exception) {
                    // do nothing
                }
            }.toString()
        } catch (e: Exception) {
            return null
        }
    }

    fun addMySet(name: String): Result<File> {
        val fileManager = FileManager(context)

        val fileName = "${removeStringsForFile(name)}.json"
        val result = fileManager.getNewTypeFile(fileName)
        val file = result.getOrNull()

        if (result.isFailure) {
            return Result.failure(Exception())
        }

        val jsonObject = JSONObject().apply {
            put(SHARE_JSON_PROPATY.DATA_NAME, name)
            put(SHARE_JSON_PROPATY.COMMON_TEXT, "")
            put(SHARE_JSON_PROPATY.GAME_DATA, JSONArray())
        }
        file!!.writeText(jsonObject.toString())
        return Result.success(file)
    }

    fun editCommonInfo(
        fileName: String,
        text: String
    ) {
        val fileManager = FileManager(context)
        val file = fileManager.getTypeFile(fileName)
        val jsonObject = JSONObject(file.readText())
        jsonObject.put(SHARE_JSON_PROPATY.COMMON_TEXT, text)
        file.writeText(jsonObject.toString())
    }

    fun addGameInfo(
        fileName: String,
        title: String,
        hash: String,
        text: String
    ): Result<GameInfo> {
        val fileManager = FileManager(context)
        val file = fileManager.getTypeFile(fileName)
        val jsonObject = JSONObject(file.readText())

        val jsonArray = jsonObject.getJSONArray(SHARE_JSON_PROPATY.GAME_DATA)

        val gameData = JSONObject().apply {
            put(SHARE_JSON_PROPATY.GAME_TITLE, title)
            put(SHARE_JSON_PROPATY.GAME_HASH, hash)
            put(SHARE_JSON_PROPATY.GAME_TEXT, text)
        }

        List(jsonArray.length()) { index ->
            val parsedData = jsonArray.getJSONObject(index)
            if (parsedData.getString(SHARE_JSON_PROPATY.GAME_HASH) == hash) {
                return Result.failure(Exception())
            }
        }

        jsonArray.put(gameData)
        jsonObject.put(SHARE_JSON_PROPATY.GAME_DATA, jsonArray)
        file.writeText(jsonObject.toString())
        return Result.success(GameInfo(gameData))
    }

    fun editGameInfo(
        fileName: String,
        title: String,
        hash: String,
        text: String
    ) {
        val fileManager = FileManager(context)
        val file = fileManager.getTypeFile(fileName)
        val jsonObject = JSONObject(file.readText())

        val jsonArray = jsonObject.getJSONArray(SHARE_JSON_PROPATY.GAME_DATA)

        List(jsonArray.length()) { index ->
            val gameData = jsonArray.getJSONObject(index)
            if (gameData.getString(SHARE_JSON_PROPATY.GAME_HASH) == hash) {
                gameData.apply {
                    put(SHARE_JSON_PROPATY.GAME_TITLE, title)
                    put(SHARE_JSON_PROPATY.GAME_TEXT, text)
                }
                jsonArray.put(index, gameData)
            }
        }
        jsonObject.put(SHARE_JSON_PROPATY.GAME_DATA, jsonArray)
        file.writeText(jsonObject.toString())
    }

    fun changeShareEnabled(app: AppInfo, isEnable: Boolean) {
        val fileManager = FileManager(context)
        val file = fileManager.getAppSettingFile()
        val jsonArray = try {
            JSONArray(file.readText())
        } catch (e: Exception) {
            JSONArray()
        }

        var isFound = false

        List(jsonArray.length()) { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            if (jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == app.packageName) {
                jsonObject.put(SHARE_JSON_PROPATY.PAKCAGE_ENABLED, isEnable)
                jsonArray.put(index, jsonObject)
                isFound = true
            }
        }

        if (!isFound) {
            val jsonObject = JSONObject().apply {
                put(SHARE_JSON_PROPATY.PACKAGE_NAME, app.packageName)
                put(SHARE_JSON_PROPATY.PAKCAGE_ENABLED, isEnable)
            }
            jsonArray.put(jsonObject)
        }

        file.writeText(jsonArray.toString())
    }

    fun changeShareType (app: AppInfo, name: String) {
        val fileManager = FileManager(context)
        val file = fileManager.getAppSettingFile()
        val jsonArray = try {
            JSONArray(file.readText())
        } catch (e: Exception) {
            JSONArray()
        }

        var isFound = false

        List(jsonArray.length()) { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            if (jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == app.packageName) {
                jsonObject.put(SHARE_JSON_PROPATY.PACKAGE_TYPE, name)
                jsonArray.put(index, jsonObject)
                isFound = true
            }
        }

        if (!isFound) {
            val jsonObject = JSONObject().apply {
                put(SHARE_JSON_PROPATY.PACKAGE_NAME, app.packageName)
                put(SHARE_JSON_PROPATY.PACKAGE_TYPE, name)
            }
            jsonArray.put(jsonObject)
        }

        file.writeText(jsonArray.toString())
    }
}
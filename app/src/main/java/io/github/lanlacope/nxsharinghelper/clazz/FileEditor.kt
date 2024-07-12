package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class FileEditor(context: Context) : FileSelector(context) {

    fun addMySet(name: String): Result<File> {

        val isExists = (getTypeFileByTitle(name).isSuccess || name == SHARE_JSON_PROPATY.TYPE_NONE)
        if (isExists) {
            return Result.failure(Exception())
        }

        val fileName = "myset_${getSimpleDate()}.json"
        val createFileResult = createNewTypeFile(fileName)

        if (createFileResult.isFailure){
            return Result.failure(Exception())
        }

        val file = createFileResult.getOrNull()
        val jsonObject = JSONObject().apply {
            put(SHARE_JSON_PROPATY.COMMON_TITLE, name)
            put(SHARE_JSON_PROPATY.COMMON_TEXT, "")
            put(SHARE_JSON_PROPATY.GAME_DATA, JSONArray())
        }
        file!!.writeText(jsonObject.toString())
        return Result.success(file)
    }

    fun removeMySet(fileName: String) {
        val file = getTypeFile(fileName)
        file.delete()
    }

    fun editCommonInfo(
        fileName: String,
        title: String,
        text: String
    ) {
        val file = getTypeFile(fileName)
        val jsonObject = JSONObject(file.readText())
        val lastTitle = try {
            jsonObject.getString(SHARE_JSON_PROPATY.COMMON_TITLE)
        } catch (e: Exception) {
            title
        }
        jsonObject.put(SHARE_JSON_PROPATY.COMMON_TITLE, title)
        jsonObject.put(SHARE_JSON_PROPATY.COMMON_TEXT, text)
        file.writeText(jsonObject.toString())

        if (title != lastTitle) {
            updateShareType(text, lastTitle)
        }
    }

    fun addGameInfo(
        fileName: String,
        title: String,
        id: String,
        text: String
    ): Result<GameInfo> {

        val file = getTypeFile(fileName)
        val jsonObject = JSONObject(file.readText())

        val jsonArray = jsonObject.getJSONArray(SHARE_JSON_PROPATY.GAME_DATA)

        jsonArray.forEachIndexOnly { index ->
            val parsedData = jsonArray.getJSONObject(index)
            if (parsedData.getString(SHARE_JSON_PROPATY.GAME_ID) == id) {
                return Result.failure(Exception())
            }
        }

        val gameData = JSONObject().apply {
            put(SHARE_JSON_PROPATY.GAME_TITLE, title)
            put(SHARE_JSON_PROPATY.GAME_ID, id)
            put(SHARE_JSON_PROPATY.GAME_TEXT, text)
        }

        jsonArray.put(gameData)
        jsonObject.put(SHARE_JSON_PROPATY.GAME_DATA, jsonArray)
        file.writeText(jsonObject.toString())
        return Result.success(GameInfo(gameData))
    }

    fun editGameInfo(
        fileName: String,
        title: String,
        id: String,
        text: String
    ) {
        val file = getTypeFile(fileName)
        val jsonObject = JSONObject(file.readText())

        val jsonArray = jsonObject.getJSONArray(SHARE_JSON_PROPATY.GAME_DATA)

        jsonArray.forEachIndexOnly { index ->
            val gameData = jsonArray.getJSONObject(index)
            if (gameData.getString(SHARE_JSON_PROPATY.GAME_ID) == id) {
                gameData.apply {
                    put(SHARE_JSON_PROPATY.GAME_TITLE, title)
                    put(SHARE_JSON_PROPATY.GAME_TEXT, text)
                }
                jsonArray.put(index, gameData)
                jsonObject.put(SHARE_JSON_PROPATY.GAME_DATA, jsonArray)
                file.writeText(jsonObject.toString())
                return
            }
        }
    }

    fun removeGameInfo(
        fileName: String,
        id: String
    ) {
        val file = getTypeFile(fileName)
        val jsonObject = JSONObject(file.readText())

        val jsonArray = jsonObject.getJSONArray(SHARE_JSON_PROPATY.GAME_DATA)

        jsonArray.forEachIndexOnly { index ->
            val gameData = jsonArray.getJSONObject(index)
            if (gameData.getString(SHARE_JSON_PROPATY.GAME_ID) == id) {
                jsonArray.remove(index)
                jsonObject.put(SHARE_JSON_PROPATY.GAME_DATA, jsonArray)
                file.writeText(jsonObject.toString())
                return
            }
        }
    }

    fun changeShareEnabled(packageName: String, isEnable: Boolean) {
        val file = getAppSettingFile()
        val jsonArray = try {
            JSONArray(file.readText())
        } catch (e: Exception) {
            JSONArray()
        }

        var isFound = false

        jsonArray.forEachIndexOnly { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            if (jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == packageName) {
                jsonObject.put(SHARE_JSON_PROPATY.PAKCAGE_ENABLED, isEnable)
                jsonArray.put(index, jsonObject)
                isFound = true
            }
        }

        if (!isFound) {
            val jsonObject = JSONObject().apply {
                put(SHARE_JSON_PROPATY.PACKAGE_NAME, packageName)
                put(SHARE_JSON_PROPATY.PAKCAGE_ENABLED, isEnable)
            }
            jsonArray.put(jsonObject)
        }

        file.writeText(jsonArray.toString())
    }

    fun changeShareType (packageName: String, name: String) {
        val file = getAppSettingFile()
        val jsonArray = try {
            JSONArray(file.readText())
        } catch (e: Exception) {
            JSONArray()
        }

        var isFound = false

        jsonArray.forEachIndexOnly { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            if (jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == packageName) {
                jsonObject.put(SHARE_JSON_PROPATY.PACKAGE_TYPE, name)
                jsonArray.put(index, jsonObject)
                isFound = true
                return@forEachIndexOnly
            }
        }

        if (!isFound) {
            val jsonObject = JSONObject().apply {
                put(SHARE_JSON_PROPATY.PACKAGE_NAME, packageName)
                put(SHARE_JSON_PROPATY.PACKAGE_TYPE, name)
            }
            jsonArray.put(jsonObject)
        }

        file.writeText(jsonArray.toString())
    }

    fun updateShareType(newType: String, lastType: String) {

        val file = getAppSettingFile()
        val jsonArray = try {
            JSONArray(file.readText())
        } catch (e: Exception) {
            return
        }

        jsonArray.forEachIndexOnly { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            if (jsonObject.getString(SHARE_JSON_PROPATY.COMMON_TITLE) == lastType) {
                jsonObject.put(SHARE_JSON_PROPATY.COMMON_TITLE, newType)
                jsonArray.put(index, jsonObject)
            }
        }

        file.writeText(jsonArray.toString())
    }
}
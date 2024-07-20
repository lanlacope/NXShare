package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.GameInfo
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.AppJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.GameJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.DevicePropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.forEachIndexOnly
import io.github.lanlacope.nxsharinghelper.clazz.propaty.mapIndexOnly
import org.json.JSONException

@Suppress("unused")
@Composable
fun rememberFileEditor(): FileEditor {
    val context = LocalContext.current
    return remember {
        FileEditor(context)
    }
}

@Immutable
class FileEditor(private val context: Context) : FileSelector(context) {

    fun checkMySetJson(jsonObject: JSONObject): Boolean {
        try {
            jsonObject.getString(GameJsonPropaty.COMMON_TITLE)
            val jsonArray = jsonObject.getJSONArray(GameJsonPropaty.GAME_DATA)
            jsonArray.forEachIndexOnly { index ->
                val gemeData = jsonArray.getJSONObject(index)
                gemeData.getString(GameJsonPropaty.GAME_ID)
            }
            return true
        } catch (e: JSONException) {
            return false
        }
    }

    fun addMySet(name: String): Result<File> {

        val isExists = (getMySetFileByTitle(name).isSuccess || name == AppJsonPropaty.TYPE_NONE)
        if (isExists) {
            return Result.failure(Exception())
        }

        val fileName = "myset_${DevicePropaty.getSimpleDate()}.json"
        val createFileResult = createNewMySetFile(fileName)

        if (createFileResult.isFailure) {
            return Result.failure(Exception())
        }

        val file = createFileResult.getOrNull()
        val jsonObject = JSONObject().apply {
            put(GameJsonPropaty.COMMON_TITLE, name)
            put(GameJsonPropaty.COMMON_TEXT, "")
            put(GameJsonPropaty.GAME_DATA, JSONArray())
        }
        file!!.writeText(jsonObject.toString())
        return Result.success(file)
    }

    fun removeMySet(fileName: String) {
        val file = getMySetFile(fileName)
        file.delete()
    }

    fun importMyset(jsonObject: JSONObject): Result<File> {

        if (!checkMySetJson(jsonObject)) {
            return Result.failure(Exception())
        }

        val fileName = "myset_${DevicePropaty.getSimpleDate()}.json"

        val createFileResult = createNewMySetFile(fileName)

        if (createFileResult.isFailure) {
            return Result.failure(Exception())
        }

        val file = createFileResult.getOrNull()

        file!!.writeText(jsonObject.toString())
        return Result.success(file)
    }

    fun editCommonInfo(
        fileName: String,
        title: String,
        text: String
    ) {
        val file = getMySetFile(fileName)
        val jsonObject = JSONObject(file.readText())
        val lastTitle = try {
            jsonObject.getString(GameJsonPropaty.COMMON_TITLE)
        } catch (e: Exception) {
            title
        }
        jsonObject.put(GameJsonPropaty.COMMON_TITLE, title)
        jsonObject.put(GameJsonPropaty.COMMON_TEXT, text)
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

        val file = getMySetFile(fileName)
        val jsonObject = JSONObject(file.readText())

        val jsonArray = jsonObject.getJSONArray(GameJsonPropaty.GAME_DATA)

        jsonArray.forEachIndexOnly { index ->
            val parsedData = jsonArray.getJSONObject(index)
            if (parsedData.getString(GameJsonPropaty.GAME_ID) == id) {
                return Result.failure(Exception())
            }
        }

        val gameData = JSONObject().apply {
            put(GameJsonPropaty.GAME_ID, id)
            put(GameJsonPropaty.GAME_TITLE, title)
            put(GameJsonPropaty.GAME_TEXT, text)
        }

        jsonArray.put(gameData)
        jsonObject.put(GameJsonPropaty.GAME_DATA, jsonArray)
        file.writeText(jsonObject.toString())
        return Result.success(GameInfo(gameData))
    }

    fun editGameInfo(
        fileName: String,
        title: String,
        id: String,
        text: String
    ) {
        val file = getMySetFile(fileName)
        val jsonObject = JSONObject(file.readText())

        val jsonArray = jsonObject.getJSONArray(GameJsonPropaty.GAME_DATA)

        jsonArray.forEachIndexOnly { index ->
            val gameData = jsonArray.getJSONObject(index)
            if (gameData.getString(GameJsonPropaty.GAME_ID) == id) {
                gameData.apply {
                    put(GameJsonPropaty.GAME_TITLE, title)
                    put(GameJsonPropaty.GAME_TEXT, text)
                }
                jsonArray.put(index, gameData)
                jsonObject.put(GameJsonPropaty.GAME_DATA, jsonArray)
                file.writeText(jsonObject.toString())
                return
            }
        }
    }

    fun removeGameInfo(
        fileName: String,
        id: String
    ) {
        val file = getMySetFile(fileName)
        val jsonObject = JSONObject(file.readText())

        val jsonArray = jsonObject.getJSONArray(GameJsonPropaty.GAME_DATA)

        jsonArray.forEachIndexOnly { index ->
            val gameData = jsonArray.getJSONObject(index)
            if (gameData.getString(GameJsonPropaty.GAME_ID) == id) {
                jsonArray.remove(index)
                jsonObject.put(GameJsonPropaty.GAME_DATA, jsonArray)
                file.writeText(jsonObject.toString())
                return
            }
        }
    }

    fun importGameInfo(
        targetFileName: String,
        joinJsonObject: JSONObject,
        overwrite: Boolean = false
    ): Result<List<GameInfo>> {

        if (!checkMySetJson(joinJsonObject)) {
            return Result.failure(Exception())
        }

        val newGames = arrayListOf<GameInfo>()

        val file = getMySetFile(targetFileName)
        val targetJsonObject = JSONObject(file.readText())
        val targetJSONArray = targetJsonObject.getJSONArray(GameJsonPropaty.GAME_DATA)
        val targetIdList = targetJSONArray.mapIndexOnly { index ->
            val gameData = targetJSONArray.getJSONObject(index)
            gameData.getString(GameJsonPropaty.GAME_ID)
        }

        val joinJSONArray = joinJsonObject.getJSONArray(GameJsonPropaty.GAME_DATA)

        joinJSONArray.forEachIndexOnly join@{ joinIndex ->
            val joinGameData = joinJSONArray.getJSONObject(joinIndex)
            if (joinGameData.getString(GameJsonPropaty.GAME_ID) in targetIdList) {
                if (overwrite) {
                    targetJSONArray.forEachIndexOnly target@{ targetIndex ->
                        val targetGameData = targetJSONArray.getJSONObject(targetIndex)
                        if (joinGameData.getString(GameJsonPropaty.GAME_ID)
                            == targetGameData.getString(GameJsonPropaty.GAME_ID)
                        ) {
                            targetGameData.apply {
                                try {
                                    put(
                                        GameJsonPropaty.GAME_TITLE,
                                        joinGameData.getString(GameJsonPropaty.GAME_TITLE)
                                    )
                                } catch (e: JSONException) {
                                    put(GameJsonPropaty.GAME_TITLE, "")
                                }
                                try {
                                    put(
                                        GameJsonPropaty.GAME_TEXT,
                                        joinGameData.getString(GameJsonPropaty.GAME_TITLE)
                                    )
                                } catch (e: JSONException) {
                                    put(GameJsonPropaty.GAME_TEXT, "")
                                }
                            }
                            targetJSONArray.put(targetIndex, targetGameData)
                            return@target
                        }
                    }
                }
            } else {
                val gameData = JSONObject().apply {
                        put(GameJsonPropaty.GAME_ID, joinGameData.getString(GameJsonPropaty.GAME_ID))
                    try {
                        put(
                            GameJsonPropaty.GAME_TITLE,
                            joinGameData.getString(GameJsonPropaty.GAME_TITLE)
                        )
                    } catch (e: JSONException) {
                        put(GameJsonPropaty.GAME_TITLE,"")
                    }
                    try {
                        put(
                            GameJsonPropaty.GAME_TEXT,
                            joinGameData.getString(GameJsonPropaty.GAME_TEXT)
                        )
                    } catch (e: JSONException) {
                        put(GameJsonPropaty.GAME_TITLE,"")
                    }
                }
                targetJSONArray.put(gameData)
                newGames.add(GameInfo(gameData))
            }
        }
        targetJsonObject.put(GameJsonPropaty.GAME_DATA, targetJSONArray)
        file.writeText(targetJsonObject.toString())
        return Result.success(newGames)
    }

    fun changeShareEnabled(packageName: String, isEnable: Boolean) {
        val file = getAppDataFile()
        val jsonArray = try {
            JSONArray(file.readText())
        } catch (e: Exception) {
            JSONArray()
        }

        var isFound = false

        jsonArray.forEachIndexOnly { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            if (jsonObject.getString(AppJsonPropaty.PACKAGE_NAME) == packageName) {
                jsonObject.put(AppJsonPropaty.PAKCAGE_ENABLED, isEnable)
                jsonArray.put(index, jsonObject)
                isFound = true
            }
        }

        if (!isFound) {
            val jsonObject = JSONObject().apply {
                put(AppJsonPropaty.PACKAGE_NAME, packageName)
                put(AppJsonPropaty.PAKCAGE_ENABLED, isEnable)
            }
            jsonArray.put(jsonObject)
        }

        file.writeText(jsonArray.toString())
    }

    fun changeShareType (packageName: String, name: String) {
        val file = getAppDataFile()
        val jsonArray = try {
            JSONArray(file.readText())
        } catch (e: Exception) {
            JSONArray()
        }

        var isFound = false

        jsonArray.forEachIndexOnly { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            if (jsonObject.getString(AppJsonPropaty.PACKAGE_NAME) == packageName) {
                jsonObject.put(AppJsonPropaty.PACKAGE_TYPE, name)
                jsonArray.put(index, jsonObject)
                isFound = true
                return@forEachIndexOnly
            }
        }

        if (!isFound) {
            val jsonObject = JSONObject().apply {
                put(AppJsonPropaty.PACKAGE_NAME, packageName)
                put(AppJsonPropaty.PACKAGE_TYPE, name)
            }
            jsonArray.put(jsonObject)
        }

        file.writeText(jsonArray.toString())
    }

    fun updateShareType(newType: String, lastType: String) {

        val file = getAppDataFile()
        val jsonArray = try {
            JSONArray(file.readText())
        } catch (e: Exception) {
            return
        }

        jsonArray.forEachIndexOnly { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            if (jsonObject.getString(GameJsonPropaty.COMMON_TITLE) == lastType) {
                jsonObject.put(GameJsonPropaty.COMMON_TITLE, newType)
                jsonArray.put(index, jsonObject)
            }
        }

        file.writeText(jsonArray.toString())
    }
}
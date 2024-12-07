package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.collection.json.forEach
import io.github.lanlacope.collection.json.forEachIndexed
import io.github.lanlacope.collection.json.map
import io.github.lanlacope.nxsharinghelper.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.GameInfo
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.AppJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.MySetJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.DevicePropaty
import org.json.JSONException

@Suppress("unused")
@Composable
fun rememberFileEditor(): FileEditor {
    val context = LocalContext.current
    return remember {
        FileEditor(context)
    }
}

/*
 * ファイルの追加、編集、削除を行う
 */
@Immutable
class FileEditor(private val context: Context) : FileSelector(context) {

    fun addMySet(title: String): Result<File> {

        val isExists = (getMySetFileByTitle(title).isSuccess || title == AppJsonPropaty.TYPE_NONE)
        if (isExists) {
            return Result.failure(Exception())
        }

        val mTitle = title.ifBlank {
            createNewMySetTitle()
        }

        val fileName = "myset_${DevicePropaty.getSimpleDate()}.json"
        val file = createNewMySetFile(fileName)

        val jsonObject = JSONObject().apply {
            put(MySetJsonPropaty.MYSET_TITLE, mTitle)
            put(MySetJsonPropaty.HEAD_TEXT, "")
            put(MySetJsonPropaty.TAIL_TEXT, "")
            put(MySetJsonPropaty.GAME_DATA, JSONArray())
        }
        file.writeText(jsonObject.toString())
        return Result.success(file)
    }

    fun removeMySet(fileName: String) {
        val file = getMySetFile(fileName)
        file.delete()
    }

    fun importMyset(title: String, jsonObject: JSONObject): Result<File> {

        if (!checkMySetJson(jsonObject)) {
            return Result.failure(Exception())
        }

        val mTitle = title.ifBlank {
            try {
                jsonObject.getString(MySetJsonPropaty.MYSET_TITLE)
            } catch (e: JSONException) {
                createNewMySetTitle()
            }
        }

        jsonObject.put(MySetJsonPropaty.MYSET_TITLE, mTitle)

        val isExists = (getMySetFileByTitle(mTitle).isSuccess || mTitle == AppJsonPropaty.TYPE_NONE)
        if (isExists) {
            return Result.failure(Exception())
        }

        val fileName = "myset_${DevicePropaty.getSimpleDate()}.json"

        val file = createNewMySetFile(fileName)

        file.writeText(jsonObject.toString())
        return Result.success(file)
    }

    fun editMysetInfo(
        fileName: String,
        title: String,
        headText: String,
        tailText: String,
    ) {
        val file = getMySetFile(fileName)
        val jsonObject = JSONObject(file.readText())
        val lastTitle = try {
            jsonObject.getString(MySetJsonPropaty.MYSET_TITLE)
        } catch (e: Exception) {
            title
        }
        jsonObject.put(MySetJsonPropaty.MYSET_TITLE, title)
        jsonObject.put(MySetJsonPropaty.HEAD_TEXT, headText)
        jsonObject.put(MySetJsonPropaty.TAIL_TEXT, tailText)
        file.writeText(jsonObject.toString())

        if (title != lastTitle) {
            updateShareType(headText, lastTitle)
        }
    }

    fun addGameInfo(
        fileName: String,
        id: String,
        title: String,
        text: String,
    ): Result<GameInfo> {

        val file = getMySetFile(fileName)
        val jsonObject = JSONObject(file.readText())

        val jsonArray = jsonObject.getJSONArray(MySetJsonPropaty.GAME_DATA)

        jsonArray.forEach { gameObject: JSONObject ->
            if (gameObject.getString(MySetJsonPropaty.GAME_ID) == id) {
                return Result.failure(Exception())
            }
        }

        val mTitle = title.ifBlank {
            createNewGameTitle()
        }

        val gameData = JSONObject().apply {
            put(MySetJsonPropaty.GAME_ID, id)
            put(MySetJsonPropaty.GAME_TITLE, mTitle)
            put(MySetJsonPropaty.GAME_TEXT, text)
        }

        jsonArray.put(gameData)
        jsonObject.put(MySetJsonPropaty.GAME_DATA, jsonArray)
        file.writeText(jsonObject.toString())
        return Result.success(GameInfo(gameData))
    }

    fun editGameInfo(
        fileName: String,
        id: String,
        title: String,
        text: String,
    ) {
        val file = getMySetFile(fileName)
        val jsonObject = JSONObject(file.readText())

        val jsonArray = jsonObject.getJSONArray(MySetJsonPropaty.GAME_DATA)

        jsonArray.forEachIndexed { index, gameData: JSONObject ->
            if (gameData.getString(MySetJsonPropaty.GAME_ID) == id) {
                gameData.apply {
                    put(MySetJsonPropaty.GAME_TITLE, title)
                    put(MySetJsonPropaty.GAME_TEXT, text)
                }
                jsonArray.put(index, gameData)
                jsonObject.put(MySetJsonPropaty.GAME_DATA, jsonArray)
                file.writeText(jsonObject.toString())
                return
            }
        }
    }

    fun removeGameInfo(
        fileName: String,
        id: String,
    ) {
        val file = getMySetFile(fileName)
        val jsonObject = JSONObject(file.readText())

        val jsonArray = jsonObject.getJSONArray(MySetJsonPropaty.GAME_DATA)

        jsonArray.forEachIndexed { index, gameData: JSONObject ->
            if (gameData.getString(MySetJsonPropaty.GAME_ID) == id) {
                jsonArray.remove(index)
                jsonObject.put(MySetJsonPropaty.GAME_DATA, jsonArray)
                file.writeText(jsonObject.toString())
                return
            }
        }
    }

    fun importGameInfo(
        targetFileName: String,
        joinJsonObject: JSONObject,
        overwrite: Boolean = false,
    ): Result<List<GameInfo>> {

        if (!checkMySetJson(joinJsonObject)) {
            return Result.failure(Exception())
        }

        val newGames = arrayListOf<GameInfo>()

        val file = getMySetFile(targetFileName)
        val targetJsonObject = JSONObject(file.readText())
        val targetJSONArray = targetJsonObject.getJSONArray(MySetJsonPropaty.GAME_DATA)
        val targetIdList = targetJSONArray.map { gameData: JSONObject ->
            gameData.getString(MySetJsonPropaty.GAME_ID)
        }

        val joinJSONArray = joinJsonObject.getJSONArray(MySetJsonPropaty.GAME_DATA)

        joinJSONArray.forEach join@{ joinGameData: JSONObject ->
            if (joinGameData.getString(MySetJsonPropaty.GAME_ID) in targetIdList) {
                if (overwrite) {
                    targetJSONArray.forEachIndexed target@{ targetIndex, targetGameData: JSONObject ->
                        if (joinGameData.getString(MySetJsonPropaty.GAME_ID)
                            == targetGameData.getString(MySetJsonPropaty.GAME_ID)
                        ) {
                            targetGameData.apply {
                                try {
                                    put(
                                        MySetJsonPropaty.GAME_TITLE,
                                        joinGameData.getString(MySetJsonPropaty.GAME_TITLE)
                                    )
                                } catch (e: JSONException) {
                                    put(MySetJsonPropaty.GAME_TITLE, createNewGameTitle())
                                }
                                try {
                                    put(
                                        MySetJsonPropaty.GAME_TEXT,
                                        joinGameData.getString(MySetJsonPropaty.GAME_TITLE)
                                    )
                                } catch (e: JSONException) {
                                    put(MySetJsonPropaty.GAME_TEXT, "")
                                }
                            }
                            targetJSONArray.put(targetIndex, targetGameData)
                            return@target
                        }
                    }
                }
            } else {
                val gameData = JSONObject().apply {
                    put(MySetJsonPropaty.GAME_ID, joinGameData.getString(MySetJsonPropaty.GAME_ID))
                    try {
                        put(
                            MySetJsonPropaty.GAME_TITLE,
                            joinGameData.getString(MySetJsonPropaty.GAME_TITLE)
                        )
                    } catch (e: JSONException) {
                        put(MySetJsonPropaty.GAME_TITLE, createNewGameTitle())
                    }
                    try {
                        put(
                            MySetJsonPropaty.GAME_TEXT,
                            joinGameData.getString(MySetJsonPropaty.GAME_TEXT)
                        )
                    } catch (e: JSONException) {
                        put(MySetJsonPropaty.GAME_TITLE, "")
                    }
                }
                targetJSONArray.put(gameData)
                newGames.add(GameInfo(gameData))
            }
        }
        targetJsonObject.put(MySetJsonPropaty.GAME_DATA, targetJSONArray)
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

        jsonArray.forEachIndexed { index, jsonObject: JSONObject ->
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

    fun changeShareType(packageName: String, name: String) {
        val file = getAppDataFile()
        val jsonArray = try {
            JSONArray(file.readText())
        } catch (e: Exception) {
            JSONArray()
        }

        var isFound = false

        jsonArray.forEachIndexed { index, jsonObject: JSONObject ->
            if (jsonObject.getString(AppJsonPropaty.PACKAGE_NAME) == packageName) {
                jsonObject.put(AppJsonPropaty.PACKAGE_TYPE, name)
                jsonArray.put(index, jsonObject)
                isFound = true
                return@forEachIndexed
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

        jsonArray.forEachIndexed { index, jsonObject: JSONObject ->
            if (jsonObject.getString(MySetJsonPropaty.MYSET_TITLE) == lastType) {
                jsonObject.put(MySetJsonPropaty.MYSET_TITLE, newType)
                jsonArray.put(index, jsonObject)
            }
        }

        file.writeText(jsonArray.toString())
    }

    fun checkMySetJson(jsonObject: JSONObject): Boolean {
        try {
            val jsonArray = jsonObject.getJSONArray(MySetJsonPropaty.GAME_DATA)
            jsonArray.forEach { gemeData: JSONObject ->
                gemeData.getString(MySetJsonPropaty.GAME_ID)
            }
            return true
        } catch (e: JSONException) {
            return false
        }
    }

    fun createNewMySetTitle(count: Int = 1): String {
        val title = context.getString(R.string.default_myset_title, count)
        if (getMySetFileByTitle(title).isSuccess) {
            return createNewMySetTitle(count + 1)
        } else {
            return title
        }
    }

    fun createNewGameTitle(): String {
        return context.getString(R.string.default_game_title)
    }
}
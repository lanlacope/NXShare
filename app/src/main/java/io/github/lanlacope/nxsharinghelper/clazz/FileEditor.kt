package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.R
import org.json.JSONObject
import java.io.File
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.GameInfo
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.MySetJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.getSimpleDate
import io.github.lanlacope.rewheel.util.json.forEach
import io.github.lanlacope.rewheel.util.json.keyList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun addMySet(title: String): Result<File> = withContext(Dispatchers.IO) {

        val isExists = (getMySetFileByTitle(title).isSuccess || title == AppJsonPropaty.MYSET_NONE)
        if (isExists) {
            return@withContext Result.failure(Exception())
        }

        val mTitle = title.ifBlank {
            createNewMySetTitle()
        }

        val fileName = "myset_${getSimpleDate()}.json"
        val file = createNewMySetFile(fileName)

        val jsonObject = JSONObject().apply {
            put(MySetJsonPropaty.MYSET_TITLE, mTitle)
            put(MySetJsonPropaty.PREFIX_TEXT, "")
            put(MySetJsonPropaty.SUFFIX_TEXT, "")
            put(MySetJsonPropaty.GAME_DATA, JSONObject())
        }
        file.writeText(jsonObject.toString())
        return@withContext Result.success(file)
    }

    suspend fun removeMySet(fileName: String) = withContext(Dispatchers.IO) {
        val file = getMySetFile(fileName)
        val mysetObject = JSONObject(file.readText())
        val lastTitle = mysetObject.optString(MySetJsonPropaty.MYSET_TITLE, AppJsonPropaty.MYSET_NONE)

        if (lastTitle != AppJsonPropaty.MYSET_NONE) {
            updateShareType(AppJsonPropaty.MYSET_NONE, lastTitle)
        }

        file.delete()
    }

    suspend fun importMyset(title: String, jsonObject: JSONObject): Result<File> = withContext(Dispatchers.IO) {

        val mTitle = title.ifBlank {
            try {
                jsonObject.getString(MySetJsonPropaty.MYSET_TITLE)
            } catch (e: JSONException) {
                createNewMySetTitle()
            }
        }

        jsonObject.put(MySetJsonPropaty.MYSET_TITLE, mTitle)

        val isExists =
            (getMySetFileByTitle(mTitle).isSuccess || mTitle == AppJsonPropaty.MYSET_NONE)
        if (isExists) {
            return@withContext Result.failure(Exception())
        }

        val fileName = "myset_${getSimpleDate()}.json"

        val file = createNewMySetFile(fileName)

        file.writeText(jsonObject.toString())
        return@withContext Result.success(file)
    }

    suspend fun editMysetInfo(
        fileName: String,
        title: String,
        headText: String,
        tailText: String,
    ) = withContext(Dispatchers.IO) {
        val file = getMySetFile(fileName)
        val mysetObject = JSONObject(file.readText())
        val lastTitle = mysetObject.optString(MySetJsonPropaty.MYSET_TITLE, title)

        mysetObject.put(MySetJsonPropaty.MYSET_TITLE, title)
        mysetObject.put(MySetJsonPropaty.PREFIX_TEXT, headText)
        mysetObject.put(MySetJsonPropaty.SUFFIX_TEXT, tailText)
        file.writeText(mysetObject.toString())

        if (title != lastTitle) {
            updateShareType(title, lastTitle)
        }
    }

    suspend fun addGameInfo(
        fileName: String,
        id: String,
        title: String,
        text: String,
    ): Result<GameInfo> = withContext(Dispatchers.IO) {

        val file = getMySetFile(fileName)
        val mysetObject = JSONObject(file.readText())

        val gameObject = mysetObject.optJSONObject(MySetJsonPropaty.GAME_DATA) ?: JSONObject()

        if (gameObject.optJSONObject(id) != null) {
            return@withContext Result.failure(Exception())
        }

        val mTitle = title.ifBlank {
            createNewGameTitle()
        }

        val gameData = JSONObject().apply {
            put(MySetJsonPropaty.GAME_TITLE, mTitle)
            put(MySetJsonPropaty.GAME_TEXT, text)
        }

        gameObject.put(id, gameData)
        mysetObject.put(MySetJsonPropaty.GAME_DATA, gameObject)
        file.writeText(mysetObject.toString())
        return@withContext Result.success(GameInfo(id, gameData))
    }

    suspend fun editGameInfo(
        fileName: String,
        id: String,
        title: String,
        text: String,
    ) = withContext(Dispatchers.IO) {
        val file = getMySetFile(fileName)
        val mysetObject = JSONObject(file.readText())

        val gameObject = mysetObject.optJSONObject(MySetJsonPropaty.GAME_DATA) ?: JSONObject()

        val gameData = gameObject.getJSONObject(id).apply {
            put(MySetJsonPropaty.GAME_TITLE, title)
            put(MySetJsonPropaty.GAME_TEXT, text)
        }

        gameObject.put(id, gameData)
        mysetObject.put(MySetJsonPropaty.GAME_DATA, gameObject)
        file.writeText(mysetObject.toString())
    }

    suspend fun removeGameInfo(
        fileName: String,
        id: String,
    ) = withContext(Dispatchers.IO) {
        val file = getMySetFile(fileName)
        val mysetObject = JSONObject(file.readText())

        val gameObject = mysetObject.getJSONObject(MySetJsonPropaty.GAME_DATA)

        gameObject.remove(id)

        mysetObject.put(MySetJsonPropaty.GAME_DATA, gameObject)
        file.writeText(mysetObject.toString())
    }

    suspend fun importGameInfo(
        targetFileName: String,
        joinJsonObject: JSONObject,
        overwrite: Boolean = false,
    ): Result<List<GameInfo>> = withContext(Dispatchers.IO) {

        val newGames = mutableListOf<GameInfo>()

        val file = getMySetFile(targetFileName)
        val targetJsonObject = JSONObject(file.readText())
        val targetGameObject =
            targetJsonObject.optJSONObject(MySetJsonPropaty.GAME_DATA) ?: JSONObject()

        val targetIds = targetGameObject.keyList()

        val joinGameObject =
            joinJsonObject.optJSONObject(MySetJsonPropaty.GAME_DATA) ?: JSONObject()

        joinGameObject.forEach join@{ joinId: String, joinGameData: JSONObject ->
            if (joinId in targetIds) {
                if (overwrite) {
                    targetGameObject.forEach target@{ targetId: String, targetGameData: JSONObject ->
                        if (joinId == targetId) {
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
                                        joinGameData.getString(MySetJsonPropaty.GAME_TEXT)
                                    )
                                } catch (e: JSONException) {
                                    put(MySetJsonPropaty.GAME_TEXT, "")
                                }
                            }
                            targetGameObject.put(targetId, targetGameData)
                            return@target
                        }
                    }
                }
            } else {
                val gameData = JSONObject().apply {
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
                        put(MySetJsonPropaty.GAME_TEXT, "")
                    }
                }
                targetGameObject.put(joinId, gameData)
                newGames.add(GameInfo(joinId, gameData))
            }
        }

        targetJsonObject.put(MySetJsonPropaty.GAME_DATA, targetGameObject)
        file.writeText(targetJsonObject.toString())

        return@withContext Result.success(newGames)
    }

    suspend fun changeShareEnabled(packageName: String, isEnable: Boolean) = withContext(Dispatchers.IO) {
        val file = getAppDataFile()
        val packageObject = try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            JSONObject()
        }

        val packageData = packageObject.optJSONObject(packageName) ?: JSONObject()

        packageData.put(AppJsonPropaty.PAKCAGE_ENABLED, isEnable)
        packageObject.put(packageName, packageData)

        file.writeText(packageObject.toString())
    }

    suspend fun changeShareType(packageName: String, name: String) = withContext(Dispatchers.IO) {
        val file = getAppDataFile()
        val packageObject = try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            JSONObject()
        }

        val packageData = packageObject.optJSONObject(packageName) ?: JSONObject()

        packageData.put(AppJsonPropaty.PACKAGE_TYPE, name)
        packageObject.put(packageName, packageData)


        file.writeText(packageObject.toString())
    }

    suspend fun updateShareType(newType: String, lastType: String) = withContext(Dispatchers.IO) {

        val file = getAppDataFile()
        val packageObject = try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            return@withContext
        }

        packageObject.forEach { packageName: String, packageData: JSONObject ->
            if (packageData.optString(AppJsonPropaty.PACKAGE_TYPE) == lastType) {
                packageData.put(AppJsonPropaty.PACKAGE_TYPE, newType)
                packageObject.put(packageName, packageData)
            }
        }

        file.writeText(packageObject.toString())
    }

    fun createNewMySetTitle(count: Int = 1): String {
        val title = context.getString(R.string.default_myset_title, count)
        return if (getMySetFileByTitle(title).isSuccess) {
            createNewMySetTitle(count + 1)
        } else {
            title
        }
    }

    fun createNewGameTitle(): String {
        return context.getString(R.string.default_game_title)
    }
}
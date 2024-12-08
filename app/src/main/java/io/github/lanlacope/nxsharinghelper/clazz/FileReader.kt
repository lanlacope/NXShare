package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.collection.json.forEach
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.AppJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.MySetJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.getGameId
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

/*
 * ファイルの内容を読み取る
 */
@Immutable
class FileReader(context: Context) : FileSelector(context) {

    fun getShareEnabled(packageName: String): Boolean {
        try {
            val file = getAppDataFile()
            val packageObject = JSONObject(file.readText())

            val packageData = packageObject.getJSONObject(packageName)

            return packageData.optBoolean(AppJsonPropaty.PAKCAGE_ENABLED)
        } catch (e: JSONException) {
            return false
        }
    }

    fun getShareMyset(packageName: String): String {
        try {
            val file = getAppDataFile()
            val packageObject = JSONObject(file.readText())

            val packageData = packageObject.getJSONObject(packageName)

            return packageData.optString(AppJsonPropaty.PACKAGE_TYPE, AppJsonPropaty.MYSET_NONE)
        } catch (e: JSONException) {
            return AppJsonPropaty.MYSET_NONE
        }
    }

    // ファイルの表示用名 + 非選択用名
    fun getMysetNames(): List<String> {
        val defaultType = listOf(AppJsonPropaty.MYSET_NONE)
        try {
            val files = getMySetFiles()
            val mysets = files.map { file ->
                val jsonObject = JSONObject(file.readText())
                jsonObject.getString(MySetJsonPropaty.MYSET_TITLE)
            }
            return defaultType + mysets
        } catch (e: JSONException) {
            return defaultType
        }
    }

    fun createCopyText(fileNames: List<String>, packageName: String): String? {
        try {
            val ids = getGameId(fileNames)
            val mysetName = getShareMyset(packageName)
            val file = getMySetFileByTitle(mysetName).getOrNull()

            val mysetObject = JSONObject(file!!.readText())

            val resultText = buildString {
                try {
                    val text = mysetObject.getString(MySetJsonPropaty.PREFIX_TEXT)
                    append(text)
                } catch (e: JSONException) {
                    // do nothing
                }
                try {
                    val gameObject = mysetObject.getJSONObject(MySetJsonPropaty.GAME_DATA)
                    gameObject.forEach { id: String, gameData: JSONObject ->
                        try {
                            if (id in ids) {
                                val text = gameData.getString(MySetJsonPropaty.GAME_TEXT)
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
                    val text = mysetObject.getString(MySetJsonPropaty.SUFFIX_TEXT)
                    append(text)
                } catch (e: JSONException) {
                    // do nothing
                }
            }
            return resultText
        } catch (e: Exception) {
            return null
        }
    }
}
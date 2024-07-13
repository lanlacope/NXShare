package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SETTING_GAME_JSON_PROPATY
import org.json.JSONObject
import java.io.File

val FOLDER_THIS: String = "NXShare"
val FOLDER_SETTING = "Setting"
val FILE_APP = "app.json"
val FOLDER_MYSET = "myset"

open class FileSelector(private val context: Context) {

    fun getSettingFolder(): File {
        val file = File(context.getExternalFilesDir(null), FOLDER_SETTING)
        if (!file.exists()) {
            file.mkdir()
        }
        return file
    }

    fun getMySetFolder(): File {
        val file =  File(getSettingFolder(), FOLDER_MYSET)
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

    // マイセットファイルを個別取得
    fun getMySetFile(fileName: String): File {
        val file = File(getMySetFolder(), fileName)
        file.createNewFile()
        return file
    }

    fun getMySetFileByTitle(typeName: String): Result<File> {
        val files = getMySetFiles()
        files.forEach { file ->
            val jsonObject = JSONObject(file.readText())
            if (jsonObject.getString(SETTING_GAME_JSON_PROPATY.COMMON_TITLE) == typeName) {
                return Result.success(file)
            }
        }
        return Result.failure(Exception())
    }

    fun createNewMySetFile(fileName: String): Result<File> {
        try {
            val file = File(getMySetFolder(), fileName)
            val isSucces = file.createNewFile()

            if (isSucces) {
                return Result.success(file)
            } else {
                return createNewMySetFile("${fileName}_")
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    // マイセットファイルを全て取得
    fun getMySetFiles(): List<File> {
        return getMySetFolder().listFiles()?.toList()?: emptyList()
    }
}
package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import org.json.JSONObject
import java.io.File

val FOLDER_THIS: String = "NXShare"
val FOLDER_SHARE = "share"
val FILE_APP = "app.json"
val FOLDER_GAME = "game"

open class FileSelector(private val context: Context) {

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

    // マイセットファイルを個別取得
    fun getTypeFile(fileName: String): File {
        val file = File(getTypeFolder(), fileName)
        file.createNewFile()
        return file
    }

    fun getTypeFileByTitle(typeName: String): File? {
        val files = getTypeFiles()
        files.forEach { file ->
            val jsonObject = JSONObject(file.readText())
            if (jsonObject.getString(SHARE_JSON_PROPATY.COMMON_TITLE) == typeName) {
                return file
            }
        }
        return null
    }

    fun getNewTypeFile(fileName: String): Result<File> {
        try {
            val file = File(getTypeFolder(), fileName)
            val isSucces = file.createNewFile()

            if (isSucces) {
                return Result.success(file)
            } else {
                return getNewTypeFile("${fileName}_")
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    // マイセットファイルを全て取得
    fun getTypeFiles(): List<File> {
        return getTypeFolder().listFiles()?.toList() ?: listOf()
    }
}
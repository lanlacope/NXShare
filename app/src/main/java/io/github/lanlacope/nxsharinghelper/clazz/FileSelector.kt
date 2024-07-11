package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import org.json.JSONObject
import java.io.File

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

    fun getTypeFileByType(typeName: String): File? {
        val files = getTypeFiles()
        files.forEach { file ->
            val jsonObject = JSONObject(file.readText())
            if (jsonObject.getString(SHARE_JSON_PROPATY.DATA_NAME) == typeName) {
                return file
            }
        }
        return null
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
}
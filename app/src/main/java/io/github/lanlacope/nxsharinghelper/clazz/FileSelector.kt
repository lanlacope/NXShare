package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.GameJsonPropaty
import org.json.JSONObject
import java.io.File

@Suppress("unused")
@Composable
fun rememberFileSelector(): FileSelector {
    val context = LocalContext.current
    return remember {
        FileSelector(context)
    }
}

@Immutable
open class FileSelector(private val context: Context) {

    companion object {
        val FOLDER_THIS: String = "NXShare"
        val FOLDER_SETTING: String = "setting"
        val FILE_SETTING: String = "setting"
        val FOLDER_DATA = "data"
        val FILE_APP = "app.json"
        val FOLDER_MYSET = "myset"
    }

    fun getSettingFolder(): File {
        val file = File(context.getExternalFilesDir(null), FOLDER_SETTING)
        if (!file.exists()) {
            file.mkdir()
        }
        return file
    }

    fun getSettingFile(): File {
        val file = File(getSettingFolder(), FILE_SETTING)
        file.createNewFile()
        return file
    }

    fun getDataFolder(): File {
        val file = File(context.getExternalFilesDir(null), FOLDER_DATA)
        if (!file.exists()) {
            file.mkdir()
        }
        return file
    }

    fun getMySetFolder(): File {
        val file =  File(getDataFolder(), FOLDER_MYSET)
        if (!file.exists()) {
            file.mkdir()
        }
        return file
    }

    // パッケージごとの設定ファイルを個別取得
    fun getAppDataFile(): File {
        val file = File(getDataFolder(), FILE_APP)
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
            if (jsonObject.getString(GameJsonPropaty.COMMON_TITLE) == typeName) {
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
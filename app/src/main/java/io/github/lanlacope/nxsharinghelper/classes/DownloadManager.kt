package io.github.lanlacope.nxsharinghelper.classes

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.SWITCH_LOCALHOST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

// TODO: 読み取りの高速化

class DownloadManager(val context: Context) {

    private data class DownloadData(
        val fileType: String = "",
        val consoleName: String = "",
        val fileNames: List<String> = listOf()
    )

    private val APP_FOLDER: String = "NXShare"

    private object JSON_PROPATY {
        val FILETYPE: String = "FileType"
        val FILENAMES: String = "FileNames"
        val CONSOLENAME: String = "ConsoleName"
        val FILETYPE_PHOTO: String = "photo"
        val FILETYPE_MOVIE: String = "movie"
    }

    private var downloadData = DownloadData()
        private set

    suspend fun start() {
        // 初期化
        clearCache()
        downloadData = DownloadData()

        parseJson(getData())
        getContents()
    }

    private suspend fun getData(): JSONObject = withContext(Dispatchers.IO) {
        val rawJson = URL(SWITCH_LOCALHOST.DATA).readText()
        return@withContext JSONObject(rawJson)
    }

    /*
     * Switchから受け取るJSONデータは以下の形式
     * {
     * "FileType":"photo",
     * "DownloadMes":"ダウンロード",
     * "PhotoHelpMes":"画面写真を保存するには、画面写真を長押しして表示されたメニューから保存を選んでください｡\n※操作は端末やブラウザーによって異なります｡",
     * "MovieHelpMes":"動画を保存するには、ダウンロードリンクをタップまたは長押しして表示されたメニューから保存を選んでください｡\n※操作は端末やブラウザーによって異なります｡",
     * "ConsoleName":"userのSwitch",
     * "FileNames":[
     * "yyyyMMddhhmmsscc-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.jpg",
     * "yyyyMMddhhmmsscc-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.jpg",
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * "yyyyMMddhhmmsscc-BF19FBEA37724338D87F26F17A3B97B2.jpg"
     * ]
     * }
     *
     * {
     * "FileType":"movie",
     * "DownloadMes":"ダウンロード",
     * "PhotoHelpMes":"画面写真を保存するには、画面写真を長押しして表示されたメニューから保存を選んでください｡\n※操作は端末やブラウザーによって異なります｡",
     * "MovieHelpMes":"動画を保存するには、ダウンロードリンクをタップまたは長押しして表示されたメニューから保存を選んでください｡\n※操作は端末やブラウザーによって異なります｡",
     * "ConsoleName":"userのSwitch",
     * "FileNames":[
     * "yyyyMMddhhmmsscc-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.mp4"
     * ]
     * }
     */
    private fun parseJson(rawJson: JSONObject) {
        val fileType = rawJson.getString(JSON_PROPATY.FILETYPE)
        val consoleName = rawJson.getString(JSON_PROPATY.CONSOLENAME)
        val fileNames: MutableList<String> = mutableListOf()
        val jsonArray = rawJson.getJSONArray(JSON_PROPATY.FILENAMES)

        for (index in 0..<jsonArray.length()) {
            fileNames.add(jsonArray.getString(index))
        }

        downloadData = DownloadData(
            fileType = fileType,
            consoleName = consoleName,
            fileNames = fileNames
        )
    }

    private suspend fun getContents() = withContext(Dispatchers.IO) {
        for (fileName in downloadData.fileNames) {
            val connection: HttpURLConnection =
                URL(SWITCH_LOCALHOST.IMAGE + fileName).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                saveFileToCashe(fileName, connection.inputStream)
            }

            connection.disconnect()
        }
    }

    private fun saveFileToCashe(fileName: String, imputStream: InputStream) {
        try {
            if (!context.cacheDir.exists()) {
                context.cacheDir.mkdirs()
            }
            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { output ->
                imputStream.copyTo(output)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private var isSaving = false

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun saveFileToStorage() = withContext(Dispatchers.IO) {
        // NOTE: 削除される可能性があるため
        val data = downloadData.copy()
        isSaving = true
        try {
            for (fileName in data.fileNames) {
                val inputUri = Uri.fromFile(File(context.cacheDir, fileName))
                val collection: Uri = createCollection(data.fileType)
                val values: ContentValues = createContentsValue(data.fileType, fileName, removeAscii(data.consoleName))
                val redsober: ContentResolver = context.contentResolver
                val outputUri = redsober.insert(collection, values)

                if (outputUri != null) { // HACK: null check
                    redsober.openInputStream(inputUri).use { input ->
                        redsober.openOutputStream(outputUri, "w").use { output ->
                            if (output != null) {
                                input!!.copyTo(output)
                            }
                        }
                    }
                    redsober.update(outputUri, updateContentsValue(data.fileType), null, null)
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "${data.fileNames.size}${context.getString(R.string.saved_notification)}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            throw e
        } finally {
            isSaving = false
        }
    }

    /*
     * _@以外のascii記号を削除
     */
    private fun removeAscii(value: String): String {
        return value.replace("""[\x21-\x2f\x3a-\x3f\x5b-\x5e\x60\x7b-\x7e\\]""".toRegex(), "")
    }

    private object MINETYPE {
        val JPG: String = "image/jpg"
        val MP4: String = "video/mp4"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createCollection(type: String): Uri {
        when(type) {
            JSON_PROPATY.FILETYPE_PHOTO -> {
                return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
            JSON_PROPATY.FILETYPE_MOVIE -> {
                return MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
        }
        return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createContentsValue(type: String, fileName: String, consoleName: String): ContentValues {
        return ContentValues().apply {
            when(type) {
                JSON_PROPATY.FILETYPE_PHOTO -> {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, MINETYPE.JPG)
                    put(MediaStore.Images.Media.IS_PENDING, true)
                    put(MediaStore.Images.ImageColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/$APP_FOLDER/$consoleName/")
                }
                JSON_PROPATY.FILETYPE_MOVIE -> {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, MINETYPE.MP4)
                    put(MediaStore.Video.Media.IS_PENDING, true)
                    put(MediaStore.Video.VideoColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_MOVIES}/$APP_FOLDER/$consoleName/")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateContentsValue(type: String): ContentValues {
        return ContentValues().apply {
            when(type) {
                JSON_PROPATY.FILETYPE_PHOTO -> {
                    put(MediaStore.Images.Media.IS_PENDING, false)
                }
                JSON_PROPATY.FILETYPE_MOVIE -> {
                    put(MediaStore.Video.Media.IS_PENDING, false)
                }
            }
        }
    }

    fun clearCache() {
        if (!isSaving) {
            context.cacheDir.listFiles()?.forEach { it.delete() }
        }
    }

    /*
     *    FOR UNDER API 28
     */
    // TODO: create
    suspend fun saveFileToStorageLegasy() = withContext(Dispatchers.IO) {
        // NOTE: 削除される可能性がある
        val data = downloadData.copy()
        isSaving = true

        try {
            val appFolder = File(Environment.getExternalStorageDirectory(), APP_FOLDER)

            val consoleFolder = File(appFolder, removeAscii(data.consoleName))

            val contentsFolder = File(consoleFolder, selectDirectory(data.fileType))
            if (!consoleFolder.exists()) {
                consoleFolder.mkdirs()
            }

            for (fileName in data.fileNames) {
                val inputFile = File(context.cacheDir, fileName)
                val outputFile = File(contentsFolder, fileName)

                FileInputStream(inputFile).use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "${data.fileNames.size}/${context.getString(R.string.saved_notification)}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            throw e
        } finally {
            isSaving = false
        }
    }

    private fun selectDirectory(type: String): String {
        when (type) {
            JSON_PROPATY.FILETYPE_PHOTO -> return Environment.DIRECTORY_PICTURES
            JSON_PROPATY.FILETYPE_MOVIE -> return Environment.DIRECTORY_MOVIES
            else -> throw IllegalAccessException()
        }
    }
}
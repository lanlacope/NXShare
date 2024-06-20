package io.github.lanlacope.nxsharinghelper.classes

import android.content.Context
import android.os.Environment
import android.widget.Toast
import io.github.lanlacope.nxsharinghelper.SWITCH_LOCALHOST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

// HACK: launch周りを見直す

class DownloadManager(val context: Context) {

    private data class DownloadData(
        val fileType: String = "",
        val consoleName: String = "",
        val fileNames: List<String> = listOf()
    )

    object DownloadStates {
        const val SUCCESSFUL: Int = 1
        const val FAILED: Int = -1
    }

    private var downloadData = DownloadData()
        private set

    var downloadState = DownloadStates.FAILED
        private set

    private val downloadLaunch = CoroutineScope(Dispatchers.IO)

    private val saveLaunch = CoroutineScope(Dispatchers.IO)

    suspend fun start() {
        Toast.makeText(context, "stt dl", Toast.LENGTH_LONG).show()

        // 初期化
        clearCashe()
        downloadState = DownloadStates.SUCCESSFUL
        downloadData = DownloadData()

        Toast.makeText(context, "js get", Toast.LENGTH_LONG).show()
        val jsonData = getData()
        Toast.makeText(context, "js get $jsonData", Toast.LENGTH_LONG).show()
        parseJson(jsonData)

        Toast.makeText(context, downloadData.fileType, Toast.LENGTH_LONG).show()
        Toast.makeText(context, downloadData.fileNames[0], Toast.LENGTH_LONG).show()
        Toast.makeText(context, "getpm", Toast.LENGTH_LONG).show()

        getContents()

        Toast.makeText(context, "getpm2", Toast.LENGTH_LONG).show()
    }

    private suspend fun getData(): JSONObject = withContext(Dispatchers.IO) {
        var result = JSONObject()
        try {
            val rawJson = URL(SWITCH_LOCALHOST.INDEX).readText()
            result = JSONObject(rawJson)
        } catch (e: Exception) {
            Toast.makeText(context, "getjs_httpb ${e.toString()}", Toast.LENGTH_LONG).show()
            downloadState = DownloadStates.FAILED
        }

        Toast.makeText(context, "js get $result", Toast.LENGTH_LONG).show()
        return@withContext result
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
    private fun parseJson(originalData: JSONObject) {
        try {
            val fileType = originalData.getString("FileType")
            val consoleName = originalData.getString("ConsoleName")

            val fileNames: MutableList<String> = mutableListOf()
            val jsonArray = originalData.getJSONArray("FileNames")
            for (index in 0..jsonArray.length()) {
                fileNames.add(jsonArray.getString(index))
            }

            downloadData = DownloadData(
                fileType = fileType,
                consoleName = consoleName,
                fileNames = fileNames
            )
        } catch (e: Exception) {
            downloadState = DownloadStates.FAILED
        }
    }

    private suspend fun getContents() = withContext(Dispatchers.IO) {
        try {
            Toast.makeText(context, "getp2", Toast.LENGTH_LONG).show()
            for (fileName in downloadData.fileNames) {
                val connection: HttpURLConnection =
                    URL(SWITCH_LOCALHOST.IMAGE + fileName).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    Toast.makeText(context, "httpok", Toast.LENGTH_LONG).show()
                    saveFileToCashe(fileName, connection.inputStream)
                } else {
                    downloadState = DownloadStates.FAILED
                }
                Toast.makeText(context, "getp3", Toast.LENGTH_LONG).show()
                connection.disconnect()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "getp-e ${e.toString()}", Toast.LENGTH_LONG).show()
            downloadState = DownloadStates.FAILED
            this.cancel()
        }
    }

    private fun saveFileToCashe(fileName: String, imputStream: InputStream) {
        try {
            val file = File("${context.cacheDir.path}/${fileName}")

            FileOutputStream(file).use { output ->
                imputStream.copyTo(output)
            }
        } catch (e: Exception) {
            downloadState = DownloadStates.FAILED
        }
    }

    private var isSaving = false

    suspend fun saveFileToStorage() = withContext(Dispatchers.IO) {
            // NOTE: 削除される可能性がある
            val data = downloadData.copy()
            isSaving = true
            try {
                for (fileName in data.fileNames) {
                    val inputFile = File("${context.cacheDir.path}/${fileName}")
                    val outputFile = File(
                        Environment.getExternalStorageDirectory().path +
                                "/${selectDirectory(data.fileType)}" +
                                "/${data.consoleName}" +
                                "/${fileName}")

                    FileInputStream(inputFile).use { input ->
                        FileOutputStream(outputFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            } catch (e: Exception) {
                this.cancel()
            } finally {
                isSaving = false
            }
        }

    private fun selectDirectory(type: String): String {
        when (type) {
            "photo" -> return Environment.DIRECTORY_PICTURES
            "movie" -> return Environment.DIRECTORY_MOVIES
        }
        return ""
    }

    fun clearCashe() {
        if (!isSaving) {
            context.cacheDir.delete()
        }

        URL("").file
    }
}
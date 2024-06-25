package io.github.lanlacope.nxsharinghelper.classes

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ContentsDownloader(val context: Context) {

    var downloadData = DownloadData()
        private set

    suspend fun start() {
        // 初期化
        downloadData = DownloadData()

        parseJson(getData())
        getContents()
    }

    private suspend fun getData(): JSONObject = withContext(Dispatchers.IO) {
        val rawJson = URL(SWITCH_LOCALHOST.DATA).readText()
        return@withContext JSONObject(rawJson)
    }

    /*
     *  Switchから受け取るJSONデータは以下の形式
     * {
     * "FileType":"photo",
     * "DownloadMes":"ダウンロード",
     * "PhotoHelpMes":"画面写真を保存するには、画面写真を長押しして表示されたメニューから保存を選んでください｡\n※操作は端末やブラウザーによって異なります｡",
     * "MovieHelpMes":"動画を保存するには、ダウンロードリンクをタップまたは長押しして表示されたメニューから保存を選んでください｡\n※操作は端末やブラウザーによって異なります｡",
     * "ConsoleName":"userのSwitch",
     * "FileNames":[
     * "yyyyMMddhhmmsscc-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.jpg",
     * "yyyyMMddhhmmsscc-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.jpg",
     * "yyyyMMddhhmmsscc-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.jpg"
     * ]
     * }
     */
    /*
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

}
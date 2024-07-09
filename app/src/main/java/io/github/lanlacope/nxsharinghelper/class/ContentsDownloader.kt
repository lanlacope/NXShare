package io.github.lanlacope.nxsharinghelper.`class`

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

    private fun parseJson(rawJson: JSONObject) {
        val fileType = rawJson.getString(DOWNLOAD_JSON_PROPATY.FILETYPE)
        val consoleName = rawJson.getString(DOWNLOAD_JSON_PROPATY.CONSOLENAME)
        val jsonArray = rawJson.getJSONArray(DOWNLOAD_JSON_PROPATY.FILENAMES)
        val fileNames = arrayListOf<String>()

        List(jsonArray.length()) { index ->
            val fileName = jsonArray.getString(index)
            fileNames.add(fileName)
        }

        downloadData = DownloadData(
            fileType = fileType,
            consoleName = consoleName,
            fileNames = fileNames
        )
    }

    private suspend fun getContents() = withContext(Dispatchers.IO) {

        downloadData.fileNames.forEach { fileName ->
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
package io.github.lanlacope.nxsharinghelper.classes

import android.os.Environment
import io.github.lanlacope.nxsharinghelper.DownloadData
import io.github.lanlacope.nxsharinghelper.SWITCH_LOCAL_HOST
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DataDownloader {

    object DownloadStates {
        val SUCCESED: Int = 1
        val FAILED: Int = -1
    }

    var downloadData = DownloadData()

    var downloadState = DownloadStates.FAILED
        private set


    fun startDownload() {

        downloadState = DownloadStates.SUCCESED

        val jsonData = getData()
        parseJson(jsonData)

        when (downloadData.fileType) {
            "phote" -> getPhotos()
            "movie" -> getMovie()
            else -> {
                downloadState = DownloadStates.FAILED
                return
            }
        }
    }

    private fun getData(): JSONObject {

        var result = JSONObject()
        runBlocking {
            try {
                val connection: HttpURLConnection =
                    URL(SWITCH_LOCAL_HOST.DATA).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    result = JSONObject(
                        reader.use { it.readText() }
                    )
                } else{
                    downloadState = DownloadStates.FAILED
                }
                connection.disconnect()
            } catch (e: Exception) {
                downloadState = DownloadStates.FAILED
            }
        }
        return result
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

        downloadData.fileType = originalData.getString("FileType")
        downloadData.consoleName = originalData.getString("ConsoleName")

        val fileNames = originalData.getJSONArray("FileNames")

        for (index in 0..fileNames.length()) {
            downloadData.fileNames.add(fileNames.getString(index))
        }
    }

    private fun getPhotos() {
        runBlocking {
            try {
                for (fileName in downloadData.fileNames) {
                    val connection: HttpURLConnection =
                        URL(SWITCH_LOCAL_HOST.IMAGE + fileName).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        saveFile(fileName, connection.inputStream)
                    } else {
                        downloadState = DownloadStates.FAILED
                    }
                    connection.disconnect()
                }
            } catch (e: Exception) {
                downloadState = DownloadStates.FAILED
            }
        }
    }

    private fun getMovie() {
        runBlocking {
            try {
                val filename = downloadData.fileNames[0]
                val connection: HttpURLConnection =
                    URL(SWITCH_LOCAL_HOST.IMAGE + filename).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    saveFile(filename, connection.inputStream)
                } else{
                    downloadState = DownloadStates.FAILED
                }
                connection.disconnect()
            } catch (e: Exception) {
                downloadState = DownloadStates.FAILED
            }
        }
    }

    private fun saveFile(fileName: String, imputStream: InputStream) {
        val directory =
            Environment.getExternalStorageDirectory().toString() +
                    "/" + choiseDirectory() +
                    "/" + downloadData.consoleName +
                    "/" + fileName
        imputStream.use { input ->
            FileOutputStream(directory).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun choiseDirectory(): String {
        return if (downloadData.fileType == "photo") {
            Environment.DIRECTORY_PICTURES
        } else { // == movie
            Environment.DIRECTORY_MOVIES
        }
    }
}
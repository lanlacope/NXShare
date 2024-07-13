package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SWITCH_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SWITCH_LOCALHOST
import io.github.lanlacope.nxsharinghelper.clazz.propaty.forEachIndexOnly
import io.github.lanlacope.nxsharinghelper.clazz.propaty.mapIndexOnly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class DownloadData(
    val fileType: String = "",
    val consoleName: String = "",
    val fileNames: List<String> = emptyList()
)

data class DownloadDataState(
    private val context: Context,
    private val data: MutableState<DownloadData>
) {
    val value = data.value

    suspend fun download() {
        val contentsDownloader = ContentsDownloader(context)
        contentsDownloader.start()
        data.value = contentsDownloader.downloadData
    }
}

@Composable
fun rememberContentsData(): DownloadDataState {
    val context = LocalContext.current
    val downloadData = rememberSaveable(stateSaver = DownloadDataSaver) {
        mutableStateOf(DownloadData())
    }
    return remember(downloadData) {
        DownloadDataState(
            context = context,
            data = downloadData
        )
    }
}

private val DownloadDataSaver = Saver<DownloadData, Bundle>(
    save = { data ->
        bundleOf(
            SWITCH_JSON_PROPATY.FILETYPE.to(data.fileType),
            SWITCH_JSON_PROPATY.CONSOLENAME.to(data.consoleName),
            SWITCH_JSON_PROPATY.FILENAMES.to(data.fileNames.toTypedArray())
        )
    },
    restore = { bundle ->
        DownloadData(
            fileType = bundle.getString(SWITCH_JSON_PROPATY.FILETYPE, ""),
            consoleName = bundle.getString(SWITCH_JSON_PROPATY.CONSOLENAME, ""),
            fileNames = bundle.getStringArray(SWITCH_JSON_PROPATY.FILENAMES)?.toList()?: emptyList()
        )
    }
)

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
        val fileType = rawJson.getString(SWITCH_JSON_PROPATY.FILETYPE)
        val consoleName = rawJson.getString(SWITCH_JSON_PROPATY.CONSOLENAME)
        val jsonArray = rawJson.getJSONArray(SWITCH_JSON_PROPATY.FILENAMES)
        val fileNames = jsonArray.mapIndexOnly { index ->
            jsonArray.getString(index)
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
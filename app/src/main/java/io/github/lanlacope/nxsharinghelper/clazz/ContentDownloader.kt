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
import io.github.lanlacope.collection.json.map
import io.github.lanlacope.nxsharinghelper.clazz.propaty.SwitchJsonPropaty
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
    val fileNames: List<String> = emptyList(),
)

data class DownloadDataState(
    private val context: Context,
    private val data: MutableState<DownloadData>,
) {
    fun getData(): DownloadData {
        return data.value
    }

    suspend fun download() {
        val contentDownloader = ContentDownloader(context)
        contentDownloader.start()
        data.value = contentDownloader.downloadData
    }
}

@Composable
fun rememberContentData(): DownloadDataState {
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
            SwitchJsonPropaty.FILETYPE to (data.fileType),
            SwitchJsonPropaty.CONSOLENAME to (data.consoleName),
            SwitchJsonPropaty.FILENAMES to (data.fileNames.toTypedArray())
        )
    },
    restore = { bundle ->
        DownloadData(
            fileType = bundle.getString(SwitchJsonPropaty.FILETYPE, ""),
            consoleName = bundle.getString(SwitchJsonPropaty.CONSOLENAME, ""),
            fileNames = bundle.getStringArray(SwitchJsonPropaty.FILENAMES)?.toList() ?: emptyList()
        )
    }
)

/*
 * ファイル情報の取得、一時ファイルへの保存を行う
 * ファイル名などは`DownloadData`に格納する
 */
class ContentDownloader(val context: Context) {

    var downloadData = DownloadData()
        private set

    suspend fun start() {
        // 初期化
        downloadData = DownloadData()

        parseJson(getData())
        getContent()
    }

    private suspend fun getData(): JSONObject = withContext(Dispatchers.IO) {
        val rawJson = URL(SwitchLocalHost.DATA).readText()
        return@withContext JSONObject(rawJson)
    }

    private fun parseJson(rawJson: JSONObject) {
        val fileType = rawJson.getString(SwitchJsonPropaty.FILETYPE)
        val consoleName = rawJson.getString(SwitchJsonPropaty.CONSOLENAME)
        val jsonArray = rawJson.getJSONArray(SwitchJsonPropaty.FILENAMES)
        val fileNames = jsonArray.map { fileName: String -> fileName }

        downloadData = DownloadData(
            fileType = fileType,
            consoleName = consoleName,
            fileNames = fileNames
        )
    }

    private suspend fun getContent() = withContext(Dispatchers.IO) {

        ContentSaver(context).clearCache()

        downloadData.fileNames.forEach { fileName ->
            val connection: HttpURLConnection =
                URL(SwitchLocalHost.IMAGE + fileName).openConnection() as HttpURLConnection
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

object SwitchLocalHost {
    const val INDEX: String = "http://192.168.0.1/index.html"
    const val DATA: String = "http://192.168.0.1/data.json"
    const val IMAGE: String = "http://192.168.0.1/img/"
}
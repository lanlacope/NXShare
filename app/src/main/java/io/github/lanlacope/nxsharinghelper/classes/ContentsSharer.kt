package io.github.lanlacope.nxsharinghelper.classes

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import io.github.lanlacope.nxsharinghelper.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class ContentsSharer(val context: Context) {

    fun createChooserIntent(data: DownloadData): Intent {

        val intent = ShareCompat.IntentBuilder(context).apply {
            when (data.fileType) {
                DOWNLOAD_JSON_PROPATY.FILETYPE_PHOTO -> {
                    setType(MINETYPE.JPG)
                    setChooserTitle(context.getString(R.string.permission_share_photo))
                }

                DOWNLOAD_JSON_PROPATY.FILETYPE_MOVIE -> {
                    setType(MINETYPE.MP4)
                    setChooserTitle(context.getString(R.string.permission_share_movie))
                }
            }

            if (data.fileNames.size == 1) {
                val file = File(context.cacheDir, data.fileNames[0])
                val uri =
                    FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", file)
                setStream(uri)
                    .createChooserIntent()
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            } else {
                val uri: ArrayList<Uri> = arrayListOf()
                data.fileNames.forEach { fileName ->
                    val file = File(context.cacheDir, fileName)
                    uri.add(
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileProvider",
                            file
                        )
                    )
                }
                setStream(uri[0])
                    .createChooserIntent()
                    .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            }
        }

        return intent.createChooserIntent()
    }

    fun createPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, ShareReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    class ShareReceiver : BroadcastReceiver() {

        lateinit var context: Context

        override fun onReceive(_context: Context, intent: Intent) {

            context = _context

            val type = getType(intent.component?.packageName) ?: ""

            val text = getText(intent.data.toString(), type)

            if (text.isNullOrEmpty()) {
                val clipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText("", text))
            }
        }

        private fun getType(packageName: String?): String? {
            try {
                val folder = File(context.filesDir, FOLDER_SHARE)
                val file = File(folder, FILE_OTHERAPPS)
                var rawJson: JSONArray
                FileInputStream(file).use { input ->
                    InputStreamReader(input).use { reader ->
                        BufferedReader(reader).use {
                            rawJson = JSONArray(it.readText())
                        }
                    }
                }

                List(rawJson.length()) { index ->
                    val partJson = rawJson.getJSONObject(index)
                    if (partJson.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == packageName) {
                        return partJson.getString(SHARE_JSON_PROPATY.PACKAGE_TYPE)
                    }
                }
                return null
            } catch (e: Exception) {
                return null
            }
        }

        private fun getText(hash: String, type: String): String? {
            try {
                val folder = File(context.filesDir, FOLDER_SHARE)
                val file = File(folder, type)
                var rawJson: JSONObject
                FileInputStream(file).use { input ->
                    InputStreamReader(input).use { reader ->
                        BufferedReader(reader).use {
                            rawJson = JSONObject(it.readText())
                        }
                    }
                }

                return StringBuilder().apply {
                    try {
                        val text = rawJson.getString(SHARE_JSON_PROPATY.COMMON_TEXT)
                        append(text)
                    } catch (e: Exception) {
                        // do nothing
                    }
                    try {
                        val arrayData = rawJson.getJSONArray(SHARE_JSON_PROPATY.GAME_DATA)
                        List(arrayData.length()) { index ->
                            val partJson = arrayData.getJSONObject(index)
                            if (partJson.getString(SHARE_JSON_PROPATY.GAME_HASH) == hash) {
                                val text = partJson.getString(SHARE_JSON_PROPATY.GAME_TEXT)
                                append(text)
                            }
                        }
                    } catch (e: Exception) {
                        // do nothing
                    }
                }.toString()
            } catch (e: Exception) {
                return null
            }
        }
    }
}
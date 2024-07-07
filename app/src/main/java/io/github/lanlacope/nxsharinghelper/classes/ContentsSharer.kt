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
import java.io.File

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

        override fun onReceive(context: Context, intent: Intent) {

            val fileManager = FileManager(context)

            val type = fileManager.getShareType(intent.component?.packageName) ?: ""

            val text = fileManager.createCopyText(intent.data.toString(), type)

            if (text.isNullOrEmpty()) {
                val clipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText("", text))
            }
        }
    }
}
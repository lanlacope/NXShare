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
import io.github.lanlacope.nxsharinghelper.isAfterAndroidX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ContentsSaver(val context: Context) {

    suspend fun save(data: DownloadData) {

        if (isAfterAndroidX()) {
            saveFileToStorage(data)
        } else {
            saveFileToStorageLegasy(data)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveFileToStorage(data: DownloadData) = withContext(Dispatchers.IO) {
        try {
            data.fileNames.forEach { fileName ->
                val inputFile = File(context.cacheDir, fileName)
                val inputUri = Uri.fromFile(inputFile)
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
        }

    }

    /**
     * _@以外のascii記号を削除
     */
    private fun removeAscii(value: String): String {
        return value.replace("""[\x21-\x2f\x3a-\x3f\x5b-\x5e\x60\x7b-\x7e\\]""".toRegex(), "")
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
                    put(
                        MediaStore.Images.ImageColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/$APP_FOLDER/$consoleName/")
                }
                JSON_PROPATY.FILETYPE_MOVIE -> {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, MINETYPE.MP4)
                    put(MediaStore.Video.Media.IS_PENDING, true)
                    put(
                        MediaStore.Video.VideoColumns.RELATIVE_PATH,
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

    /*****FOR BEFORE API 28 *****/

    private suspend fun saveFileToStorageLegasy(data: DownloadData) = withContext(Dispatchers.IO) {

        try {
            val appFolder = File(Environment.getExternalStorageDirectory(), APP_FOLDER)

            val consoleFolder = File(appFolder, removeAscii(data.consoleName))

            val contentsFolder = File(consoleFolder, selectDirectory(data.fileType))
            if (!consoleFolder.exists()) {
                consoleFolder.mkdirs()
            }

            data.fileNames.forEach { fileName ->
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
        }
    }

    private fun selectDirectory(type: String): String {
        when (type) {
            JSON_PROPATY.FILETYPE_PHOTO -> return Environment.DIRECTORY_PICTURES
            JSON_PROPATY.FILETYPE_MOVIE -> return Environment.DIRECTORY_MOVIES
            else -> throw IllegalAccessException()
        }
    }

    /*****FOR BEFORE API 28 *****/
}
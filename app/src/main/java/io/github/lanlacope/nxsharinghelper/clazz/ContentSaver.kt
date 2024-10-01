package io.github.lanlacope.nxsharinghelper.clazz

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SwitchJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.MineType
import io.github.lanlacope.nxsharinghelper.clazz.propaty.DevicePropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.removeStringsForFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@Suppress("unused")
@Composable
fun rememberContentSaver(): ContentSaver {
    val context = LocalContext.current
    return remember {
        ContentSaver(context)
    }
}

/*
 * 一時ファイルから外部ストレージへの保存を行う
 */
@Stable
class ContentSaver(_context: Context) {

    private val context = _context.applicationContext

    companion object {
        private var isSaving = false
    }

    suspend fun save(data: DownloadData) {

        isSaving = true

        if (DevicePropaty.isAfterAndroidX()) {
            saveFileToStorage(data)
        } else {
            saveFileToStorageLegasy(data)
        }

        isSaving = false
    }

    fun clearCache() {
        if (!isSaving) {
            context.cacheDir.listFiles()?.forEach { file ->
                file.delete()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveFileToStorage(data: DownloadData) = withContext(Dispatchers.IO) {
        try {
            data.fileNames.forEach { fileName ->
                val inputFile = File(context.cacheDir, fileName)
                val inputUri = Uri.fromFile(inputFile)
                val collection: Uri = createCollection(data.fileType)
                val values: ContentValues =
                    createContentsValue(data.fileType, fileName, removeStringsForFile(data.consoleName))
                val redsober: ContentResolver = context.contentResolver
                val outputUri = redsober.insert(collection, values)

                redsober.openInputStream(inputUri).use { input ->
                    redsober.openOutputStream(outputUri!!, "w").use { output ->
                        input!!.copyTo(output!!)
                    }
                }
                redsober.update(outputUri!!, updateContentsValue(data.fileType), null, null)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.saved_notification, data.fileNames.size),
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createCollection(type: String): Uri {
        when(type) {
            SwitchJsonPropaty.FILETYPE_PHOTO -> {
                return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
            SwitchJsonPropaty.FILETYPE_MOVIE -> {
                return MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
        }
        return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createContentsValue(type: String, fileName: String, consoleName: String): ContentValues {
        return ContentValues().apply {
            when(type) {
                SwitchJsonPropaty.FILETYPE_PHOTO -> {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, MineType.JPG)
                    put(MediaStore.Images.Media.IS_PENDING, true)
                    put(
                        MediaStore.Images.ImageColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/${FileSelector.FOLDER_THIS}/$consoleName/")
                }
                SwitchJsonPropaty.FILETYPE_MOVIE -> {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, MineType.MP4)
                    put(MediaStore.Video.Media.IS_PENDING, true)
                    put(
                        MediaStore.Video.VideoColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_MOVIES}/${FileSelector.FOLDER_THIS}/$consoleName/")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateContentsValue(type: String): ContentValues {
        return ContentValues().apply {
            when(type) {
                SwitchJsonPropaty.FILETYPE_PHOTO -> {
                    put(MediaStore.Images.Media.IS_PENDING, false)
                }
                SwitchJsonPropaty.FILETYPE_MOVIE -> {
                    put(MediaStore.Video.Media.IS_PENDING, false)
                }
            }
        }
    }

    private suspend fun saveFileToStorageLegasy(data: DownloadData) = withContext(Dispatchers.IO) {

        try {
            val appFolder = File(Environment.getExternalStorageDirectory(), FileSelector.FOLDER_THIS)

            val consoleFolder = File(appFolder, removeStringsForFile(data.consoleName))

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
                    context.getString(R.string.saved_notification, data.fileNames.size),
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun selectDirectory(type: String): String {
        when (type) {
            SwitchJsonPropaty.FILETYPE_PHOTO -> return Environment.DIRECTORY_PICTURES
            SwitchJsonPropaty.FILETYPE_MOVIE -> return Environment.DIRECTORY_MOVIES
            else -> throw IllegalAccessException()
        }
    }
}
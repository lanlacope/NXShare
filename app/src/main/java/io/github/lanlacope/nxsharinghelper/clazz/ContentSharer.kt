package io.github.lanlacope.nxsharinghelper.clazz

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcelable
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SwitchJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.MineType
import io.github.lanlacope.nxsharinghelper.clazz.propaty.DevicePropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.toArrayList
import java.io.File

@Suppress("unused")
@Composable
fun rememberContentSharer(): ContentSharer {
    val context = LocalContext.current
    return remember {
        ContentSharer(context)
    }
}

@Immutable
class ContentSharer(val context: Context) {

    fun createChooserIntent(data: DownloadData): Intent {

        val intent = ShareCompat.IntentBuilder(context).apply {
            when (data.fileType) {
                SwitchJsonPropaty.FILETYPE_PHOTO -> {
                    setType(MineType.JPG)
                    setChooserTitle(context.getString(R.string.permission_share_photo))
                }

                SwitchJsonPropaty.FILETYPE_MOVIE -> {
                    setType(MineType.MP4)
                    setChooserTitle(context.getString(R.string.permission_share_movie))
                }
            }

            if (data.fileNames.size <= 1) {
                val file = File(context.cacheDir, data.fileNames[0])
                val uri =
                    FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", file)
                setStream(uri)
                    .createChooserIntent()
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            } else {
                val uri = data.fileNames.map { fileName ->
                    val file = File(context.cacheDir, fileName)
                    FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", file)
                }.toArrayList()

                setStream(uri[0])
                    .createChooserIntent()
                    .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            }
        }

        return intent.createChooserIntent()
    }

    fun createCustomChooserIntrnt(data: DownloadData) : Intent {

        val title = when (data.fileType) {
            SwitchJsonPropaty.FILETYPE_PHOTO -> {
                context.getString(R.string.permission_share_photo)
            }

            else -> {
                context.getString(R.string.permission_share_movie)
            }
        }

        val pendingIntent = createPendingIntent(data)

        val sendablentent = createSendableIntent(data)

        val packageManager = context.packageManager
        val fileReader = FileReader(context)

        val sendablePackages =
            packageManager.queryIntentActivities(
                sendablentent,
                PackageManager.MATCH_DEFAULT_ONLY
            )

        if (DevicePropaty.isAfterAndroidX()) {

            val chooserIntent = Intent.createChooser(sendablentent, title, pendingIntent.intentSender)

            val filteredPackages = sendablePackages.filterNot { sendablePackage ->
                val packageName = sendablePackage.activityInfo.packageName
                fileReader.getShareEnabled(packageName)
            }

            val excludeComponents = filteredPackages.map { filteredPackage ->
                ComponentName(
                    filteredPackage.activityInfo.packageName,
                    filteredPackage.activityInfo.name
                )
            }

            chooserIntent.putExtra(
                Intent.EXTRA_EXCLUDE_COMPONENTS,
                excludeComponents.toTypedArray<Parcelable>()
            )

            return chooserIntent
        } else {

            val chooserIntent = Intent.createChooser(Intent(), title, pendingIntent.intentSender)

            val filteredPackages = sendablePackages.filter { sendablePackage ->
                val packageName = sendablePackage.activityInfo.packageName
                !fileReader.getShareEnabled(packageName)
            }

            val filteredIntents = filteredPackages.map { filteredPackage ->
                Intent(sendablentent).apply {
                    setComponent(
                        ComponentName(
                            filteredPackage.activityInfo.packageName,
                            filteredPackage.activityInfo.name
                        )
                    )
                }
            }

            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                filteredIntents.toTypedArray<Parcelable>()
            )
            return chooserIntent
        }
    }

    fun createSendableIntent(data: DownloadData): Intent {
        return Intent().apply {
            when (data.fileType) {
                SwitchJsonPropaty.FILETYPE_PHOTO -> {
                    setType(MineType.JPG)
                }

                SwitchJsonPropaty.FILETYPE_MOVIE -> {
                    setType(MineType.MP4)
                }
            }

            if (data.fileNames.size == 1) {
                setAction(Intent.ACTION_SEND)

                val file = File(context.cacheDir, data.fileNames[0])
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", file)
                putExtra(Intent.EXTRA_STREAM, uri)

            } else {
                setAction(Intent.ACTION_SEND_MULTIPLE)

                val uri = data.fileNames.map { fileName ->
                    val file = File(context.cacheDir, fileName)

                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileProvider",
                        file
                    )
                }.toArrayList()
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uri)
            }

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createPendingIntent(data: DownloadData): PendingIntent {

        val fileNames = ArrayList(data.fileNames)

        val intent = Intent(context, ShareReceiver::class.java).apply {
            putStringArrayListExtra(Intent.ACTION_SEND_MULTIPLE, fileNames)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        return pendingIntent
    }

    @Immutable
    class ShareReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val fileReader = FileReader(context)

            val fileNames = intent.getStringArrayListExtra(Intent.ACTION_SEND_MULTIPLE)?.toList()?: emptyList()

            val componentName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT, ComponentName::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT)
            }

            val packageName = componentName?.packageName?: ""

            val text = fileReader.createCopyText(fileNames, packageName)

            if (!text.isNullOrEmpty()) {
                val clipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText("", text))

                Toast.makeText(context, context.getString(R.string.copy_clipboard), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
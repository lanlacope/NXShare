package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SWITCH_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.APP_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.GAME_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.clazz.propaty.mapIndexOnly
import kotlinx.collections.immutable.toImmutableList
import org.json.JSONObject
import java.io.File

@Suppress("unused")
@Composable
fun rememberInfoManager(): InfoManager {
    val context = LocalContext.current
    return remember {
        InfoManager(context)
    }
}

@Immutable
class InfoManager(private val context: Context) : FileSelector(context) {

    data class AppInfo(
        private val applicationInfo: ApplicationInfo,
        private val packageManager: PackageManager,
    ) {
        val name = applicationInfo.loadLabel(packageManager).toString()
        val icon = applicationInfo.loadIcon(packageManager)
        val packageName = applicationInfo.packageName
    }


    data class ShareInfo(
        private val packageName: String,
        private val context: Context
    ) {
        private val fileReader = FileReader(context)
        val shareEnabled = fileReader.getShareEnabled(packageName)
        val type = fileReader.getShareType(packageName)?: APP_JSON_PROPATY.TYPE_NONE
        val types = fileReader.getTypeNamesWithNone().toImmutableList()
    }


    data class CommonInfo(
        private val jsonObject: JSONObject
    ) {
        val title = jsonObject.getString(GAME_JSON_PROPATY.COMMON_TITLE)
        val text = jsonObject.getString(GAME_JSON_PROPATY.COMMON_TEXT)
    }


    data class GameInfo(
        private val jsonObject: JSONObject
    ) {
        val title = jsonObject.getString(GAME_JSON_PROPATY.GAME_TITLE)
        val id = jsonObject.getString(GAME_JSON_PROPATY.GAME_ID)
        val text = jsonObject.getString(GAME_JSON_PROPATY.GAME_TEXT)
    }

    fun getAppInfo(): List<AppInfo> {
        val contentsSharer = ContentsSharer(context)

        val sendJpgIntent = contentsSharer.createSendableIntent(
            DownloadData(
                fileType = SWITCH_JSON_PROPATY.FILETYPE_PHOTO,
                fileNames = listOf("a.jpg")
            )
        )
        val sendJpgsIntent = contentsSharer.createSendableIntent(
            DownloadData(
                fileType = SWITCH_JSON_PROPATY.FILETYPE_PHOTO,
                fileNames = listOf("a.jpg", "b.jpg")
            )
        )
        val sendMp4Intent = contentsSharer.createSendableIntent(
            DownloadData(
                fileType = SWITCH_JSON_PROPATY.FILETYPE_MOVIE,
                fileNames = listOf("a.mp4")
            )
        )

        val packageManager = context.packageManager

        val receiveJpgPackages =
            packageManager.queryIntentActivities(sendJpgIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val receiveJpgsPackages =
            packageManager.queryIntentActivities(sendJpgsIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val receiveMp4Packages =
            packageManager.queryIntentActivities(sendMp4Intent, PackageManager.MATCH_DEFAULT_ONLY)

        val allPackages = receiveJpgPackages + receiveJpgsPackages + receiveMp4Packages

        val parsedPackageNames = allPackages.map { _package ->
            _package.activityInfo.packageName
        }.distinct()

        val appInfo = parsedPackageNames.map { packageName ->
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            AppInfo(applicationInfo, packageManager)
        }

        return appInfo
    }

    fun getCommonInfo(file: File): CommonInfo {
        val jsonObject = JSONObject(file.readText())
        return CommonInfo(jsonObject)
    }

    fun getGameInfo(file: File): List<GameInfo> {
        val jsonObject = JSONObject(file.readText())
        val jsonArray = jsonObject.getJSONArray(GAME_JSON_PROPATY.GAME_DATA)
        val info = jsonArray.mapIndexOnly { index ->
            val gameData = jsonArray.getJSONObject(index)
            GameInfo(gameData)
        }
        return info
    }
}
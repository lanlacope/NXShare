package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import org.json.JSONObject
import java.io.File

data class AppInfo(
    private val resolveInfo: ResolveInfo,
    private val packageManager: PackageManager,
) {
    val appName = resolveInfo.loadLabel(packageManager).toString()
    val packageName = resolveInfo.activityInfo?.packageName ?: "com.example.com"
    val icon = resolveInfo.loadIcon(packageManager)
}

data class ShareInfo(
    private val appInfo: AppInfo,
    private val context: Context
) {
    private val fileReader = FileReader(context)
    val shareEnabled = fileReader.getShareEnabled(appInfo)
    val type = fileReader.getShareType(appInfo) ?: SHARE_JSON_PROPATY.TYPE_NONE
    val types = fileReader.getTypeNamesWithNone()
}

data class CommonInfo(
    private val jsonObject: JSONObject
) {
    val title = jsonObject.getString(SHARE_JSON_PROPATY.COMMON_TITLE)
    val text = jsonObject.getString(SHARE_JSON_PROPATY.COMMON_TEXT)
}

data class GameInfo(
    private val jsonObject: JSONObject
) {
    val title = jsonObject.getString(SHARE_JSON_PROPATY.GAME_TITLE)
    val id = jsonObject.getString(SHARE_JSON_PROPATY.GAME_ID)
    val text = jsonObject.getString(SHARE_JSON_PROPATY.GAME_TEXT)
}

class InfoManager(private val context: Context) : FileSelector(context) {

    fun getAppInfo(): List<AppInfo> {
        val contentsSharer = ContentsSharer(context)

        val sendJpgIntent = contentsSharer.createSendableIntent(
            DownloadData(
                fileType = DOWNLOAD_JSON_PROPATY.FILETYPE_PHOTO,
                fileNames = listOf("a.jpg")
            )
        )
        val sendJpgsIntent = contentsSharer.createSendableIntent(
            DownloadData(
                fileType = DOWNLOAD_JSON_PROPATY.FILETYPE_PHOTO,
                fileNames = listOf("a.jpg", "b.jpg")
            )
        )
        val sendMp4Intent = contentsSharer.createSendableIntent(
            DownloadData(
                fileType = DOWNLOAD_JSON_PROPATY.FILETYPE_MOVIE,
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
            val resolveInfo = allPackages.first { rawPackage ->
                rawPackage.activityInfo.packageName == packageName
            }
            AppInfo(resolveInfo, packageManager)
        }

        return appInfo
    }

    fun getCommonInfo(file: File): CommonInfo {
        val jsonObject = JSONObject(file.readText())
        return CommonInfo(jsonObject)
    }

    fun getGameInfo(file: File): List<GameInfo> {
        val jsonObject = JSONObject(file.readText())
        val jsonArray = jsonObject.getJSONArray(SHARE_JSON_PROPATY.GAME_DATA)
        val info = jsonArray.mapIndexOnly { index ->
            val gameData = jsonArray.getJSONObject(index)
            GameInfo(gameData)
        }
        return info
    }
}
package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.collection.json.map
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SwitchJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.MySetJsonPropaty
import kotlinx.collections.immutable.toImmutableList
import org.json.JSONObject
import java.io.File

@Suppress("unused")
@Composable
fun rememberInfoManager(): InfoManager {
    val context = LocalContext.current
    return remember { InfoManager(context) }
}

/*
 *　アプリ内で使用するデータの管理、取得を行う
 */
@Immutable
class InfoManager(private val context: Context) : FileSelector(context) {

    data class AppInfo(
        private val applicationInfo: ApplicationInfo,
        private val packageManager: PackageManager,
    ) {
        val name = applicationInfo.loadLabel(packageManager).toString()
        val icon: Drawable = applicationInfo.loadIcon(packageManager)
        val packageName: String = applicationInfo.packageName
    }


    data class ShareInfo(
        private val packageName: String,
        private val context: Context,
    ) {
        private val fileReader = FileReader(context)
        val shareEnabled = fileReader.getShareEnabled(packageName)
        val type = fileReader.getShareMyset(packageName)
        val types = fileReader.getMysetNames().toImmutableList()
    }


    data class MysetInfo(
        private val jsonObject: JSONObject,
    ) {
        val title: String = jsonObject.optString(MySetJsonPropaty.MYSET_TITLE, "ERROR")
        val prefixText: String = jsonObject.optString(MySetJsonPropaty.PREFIX_TEXT)
        val suffixText: String = jsonObject.optString(MySetJsonPropaty.SUFFIX_TEXT)
    }


    data class GameInfo(
        val id: String,
        private val gameData: JSONObject,
    ) {
        val title: String = gameData.optString(MySetJsonPropaty.GAME_TITLE, "ERROR")
        val text: String = gameData.optString(MySetJsonPropaty.GAME_TEXT)

    }

    fun getAppInfo(): List<AppInfo> {
        val contentSharer = ContentSharer(context)

        val sendJpgIntent = contentSharer.createSendableIntent(
            DownloadData(
                fileType = SwitchJsonPropaty.FILETYPE_PHOTO,
                fileNames = listOf("a.jpg")
            )
        )
        val sendJpgsIntent = contentSharer.createSendableIntent(
            DownloadData(
                fileType = SwitchJsonPropaty.FILETYPE_PHOTO,
                fileNames = listOf("a.jpg", "b.jpg")
            )
        )
        val sendMp4Intent = contentSharer.createSendableIntent(
            DownloadData(
                fileType = SwitchJsonPropaty.FILETYPE_MOVIE,
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

    fun getMysetInfo(file: File): MysetInfo {
        val jsonObject = JSONObject(file.readText())
        return MysetInfo(jsonObject)
    }

    fun getGameInfo(file: File): List<GameInfo> {
        val mysetObject = JSONObject(file.readText())
        val gameObject = mysetObject.optJSONObject(MySetJsonPropaty.GAME_DATA)

        if (gameObject != null) {
            return gameObject.map { id, gameData: JSONObject ->
                GameInfo(id, gameData)
            }
        } else {
            return emptyList()
        }
    }
}
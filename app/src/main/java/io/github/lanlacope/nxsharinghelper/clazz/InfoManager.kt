package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SwitchJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.AppJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.MySetJsonPropaty
import io.github.lanlacope.nxsharinghelper.widgit.mapIndexOnly
import kotlinx.collections.immutable.toImmutableList
import org.json.JSONException
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
        val type = fileReader.getShareType(packageName)?: AppJsonPropaty.TYPE_NONE
        val types = fileReader.getTypeNames().toImmutableList()
    }


    data class CommonInfo(
        private val jsonObject: JSONObject
    ) {
        val title = jsonObject.getString(MySetJsonPropaty.MYSET_TITLE)
        val haedText = try {
            jsonObject.getString(MySetJsonPropaty.HEAD_TEXT)
        } catch (e: JSONException) {
            ""
        }
        val tailText = try {
            jsonObject.getString(MySetJsonPropaty.TAIL_TEXT)
        } catch (e: JSONException) {
            ""
        }
    }


    data class GameInfo(
        private val jsonObject: JSONObject
    ) {
        val id = jsonObject.getString(MySetJsonPropaty.GAME_ID)
        val title = try {
            jsonObject.getString(MySetJsonPropaty.GAME_TITLE)
        } catch (e: JSONException) {
            ""
        }
        val text = try {
            jsonObject.getString(MySetJsonPropaty.GAME_TEXT)
        } catch (e: JSONException) {
            ""
        }
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

    fun getCommonInfo(file: File): CommonInfo {
        val jsonObject = JSONObject(file.readText())
        return CommonInfo(jsonObject)
    }

    fun getGameInfo(file: File): List<GameInfo> {
        val jsonObject = JSONObject(file.readText())
        val jsonArray = jsonObject.getJSONArray(MySetJsonPropaty.GAME_DATA)
        val info = jsonArray.mapIndexOnly { index ->
            val gameData = jsonArray.getJSONObject(index)
            GameInfo(gameData)
        }
        return info
    }
}
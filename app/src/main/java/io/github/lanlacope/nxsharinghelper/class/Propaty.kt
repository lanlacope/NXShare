package io.github.lanlacope.nxsharinghelper.`class`

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

fun isAfterAndroidX(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

}

inline fun JSONArray.forEachIndexOnly(action: (Int) -> Unit): Unit {
    for (index in 0 until length()) action(index)
}

inline fun <R> JSONArray.mapIndexOnly(action: (Int) -> R): List<R> {
    return (0 until length()).map { index -> action(index) }
}

fun removeStringsForFile(value: String): String {
    return value.replace(Regex("""[\x21-\x2f\x3a-\x3f\x5b-\x5e\x60\x7b-\x7e\\]"""), "")
}

object SWITCH_LOCALHOST {
    val INDEX: String = "http://192.168.0.1/index.html"
    val DATA: String = "http://192.168.0.1/data.json"
    val IMAGE: String = "http://192.168.0.1/img/"
}

val FOLDER_THIS: String = "NXShare"

object MINETYPE {
    val JPG: String = "image/jpg"
    val MP4: String = "video/mp4"
}

object DOWNLOAD_JSON_PROPATY {
    val FILETYPE: String = "FileType"
    val FILENAMES: String = "FileNames"
    val CONSOLENAME: String = "ConsoleName"
    val FILETYPE_PHOTO: String = "photo"
    val FILETYPE_MOVIE: String = "movie"
}

data class DownloadData(
    val fileType: String = "",
    val consoleName: String = "",
    val fileNames: List<String> = listOf()
)

val FOLDER_SHARE = "share"
val FILE_APP = "app.json"
val FOLDER_GAME = "game"

object SHARE_JSON_PROPATY {
    val PACKAGE_NAME: String = "PackageName"
    val PACKAGE_TYPE: String = "PackageType"
    val TYPE_NONE: String = "NotUse"
    val PAKCAGE_ENABLED: String = "PackageEnabled"
    val DATA_NAME: String = "DataName"
    val COMMON_TEXT: String = "CommonText"
    val GAME_DATA: String = "GameData"
    val GAME_TITLE: String = "GameTitle"
    val GAME_HASH: String = "GameHash"
    val GAME_TEXT: String = "GameText"
}

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
    private val fileManager = FileManager(context)
    val shareEnabled = fileManager.getShareEnabled(appInfo)
    val type = fileManager.getShareType(appInfo) ?: SHARE_JSON_PROPATY.TYPE_NONE
    val types = fileManager.getTypeNamesWithNone()
}

data class TypeInfo(
    private val file: File,
    private val name: String
) {
    val typeFile = file
    val fileName = name
}

data class CommonInfo(
    private val jsonObject: JSONObject
) {
    val name = jsonObject.getString(SHARE_JSON_PROPATY.DATA_NAME)
    val text = jsonObject.getString(SHARE_JSON_PROPATY.COMMON_TEXT)
}

data class GameInfo(
    private val jsonObject: JSONObject
) {
    val title = jsonObject.getString(SHARE_JSON_PROPATY.GAME_TITLE)
    val hash = jsonObject.getString(SHARE_JSON_PROPATY.GAME_HASH)
    val text = jsonObject.getString(SHARE_JSON_PROPATY.GAME_TEXT)
}

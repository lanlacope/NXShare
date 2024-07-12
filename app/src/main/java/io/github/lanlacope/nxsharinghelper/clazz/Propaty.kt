package io.github.lanlacope.nxsharinghelper.clazz

import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray

fun isAfterAndroidX(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

}

inline fun JSONArray.forEachIndexOnly(action: (Int) -> Unit) {
    for (index in 0 until length()) action(index)
}

inline fun <R> JSONArray.mapIndexOnly(action: (Int) -> R): List<R> {
    return (0 until length()).map { index -> action(index) }
}

fun removeStringsForFile(value: String): String {
    return value.replace(Regex("""[\x21-\x2f\x3a-\x3f\x5b-\x5e\x60\x7b-\x7e\\]"""), "")
}

fun getGameId(fileNames: List<String>): List<String> {
    val regex = Regex(""".*-(.*?)\..*?$""")
    val ids = fileNames.map { rawId ->
        val matchResult = regex.find(rawId)
        matchResult?.groupValues?.get(1) ?: ""
    }.distinct()
    return ids
}

object SWITCH_LOCALHOST {
    val INDEX: String = "http://192.168.0.1/index.html"
    val DATA: String = "http://192.168.0.1/data.json"
    val IMAGE: String = "http://192.168.0.1/img/"
}

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

object SHARE_JSON_PROPATY {
    val PACKAGE_NAME: String = "PackageName"
    val PACKAGE_TYPE: String = "PackageType"
    val TYPE_NONE: String = "NotUse"
    val PAKCAGE_ENABLED: String = "PackageEnabled"
    val COMMON_TITLE: String = "CommonTitle"
    val COMMON_TEXT: String = "CommonText"
    val GAME_DATA: String = "GameData"
    val GAME_TITLE: String = "GameTitle"
    val GAME_ID: String = "GameID"
    val GAME_TEXT: String = "GameText"
}

@Composable
fun makeToast(
    text: String,
    duration: Int = Toast.LENGTH_SHORT
):Toast {
    return Toast.makeText(LocalContext.current, text, duration)
}
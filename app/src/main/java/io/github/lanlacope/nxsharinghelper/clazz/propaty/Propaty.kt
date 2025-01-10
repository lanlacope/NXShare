package io.github.lanlacope.nxsharinghelper.clazz.propaty

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun isAfterAndroidX(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}

fun getSimpleDate(): String {
    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    return dateFormat.format(Date())
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

const val ERROR = "ERROR"

object MineType {
    const val JPG: String = "image/jpg"
    const val MP4: String = "video/mp4"
    const val JSON: String = "application/json"
}


object AppJsonPropaty {
    const val PACKAGE_TYPE: String = "PackageType"
    const val MYSET_NONE: String = "NONE"
    const val PAKCAGE_ENABLED: String = "PackageEnabled"
}

object MySetJsonPropaty {
    const val MYSET_TITLE: String = "Title"
    const val PREFIX_TEXT: String = "PrefixText"
    const val SUFFIX_TEXT: String = "SuffixText"
    const val GAME_DATA: String = "GameData"
    const val GAME_TITLE: String = "GameTitle"
    const val GAME_TEXT: String = "GameText"
}

object SwitchJsonPropaty {
    const val FILETYPE: String = "FileType"
    const val FILENAMES: String = "FileNames"
    const val CONSOLENAME: String = "ConsoleName"
    const val FILETYPE_PHOTO: String = "photo"
    const val FILETYPE_MOVIE: String = "movie"
}




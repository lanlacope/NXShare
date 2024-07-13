package io.github.lanlacope.nxsharinghelper.clazz.propaty

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import io.github.lanlacope.nxsharinghelper.clazz.ContentsDownloader
import io.github.lanlacope.nxsharinghelper.clazz.DownloadData
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SWITCH_JSON_PROPATY
import org.json.JSONArray



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

@Composable
fun makeToast(
    text: String,
    duration: Int = Toast.LENGTH_SHORT
):Toast {
    return Toast.makeText(LocalContext.current, text, duration)
}

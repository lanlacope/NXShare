package io.github.lanlacope.nxsharinghelper.clazz.propaty

import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL


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
fun versionName(): String? {
    val activity = LocalContext.current as Activity
    val name = activity.getPackageName()

    val pm: PackageManager = activity.getPackageManager()

    val info = pm.getPackageInfo(name, PackageManager.GET_META_DATA)

    return info.versionName
}

suspend fun getLatestVersion(): String? = withContext(Dispatchers.Default) {
    try {
        val response = URL(AppGitHost.LATEST_API).readText()
        println(response)
        val jsonObject = JSONObject(response)
        return@withContext jsonObject.getString(AppGitHost.LATEST_TAG)
    } catch (e: Exception) {
        null
    }
}


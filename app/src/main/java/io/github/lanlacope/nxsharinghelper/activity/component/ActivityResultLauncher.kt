package io.github.lanlacope.nxsharinghelper.activity.component

import android.app.Activity.RESULT_OK
import android.app.Activity.WIFI_SERVICE
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.journeyapps.barcodescanner.ScanOptions
import io.github.lanlacope.nxsharinghelper.activity.SwitchCaptureActivity
import io.github.lanlacope.nxsharinghelper.clazz.ConnectionManager.WifiConfig
import io.github.lanlacope.nxsharinghelper.clazz.SettingManager
import io.github.lanlacope.nxsharinghelper.clazz.propaty.MineType
import io.github.lanlacope.nxsharinghelper.clazz.propaty.isAfterAndroidX
import io.github.lanlacope.nxsharinghelper.clazz.rememberSettingManager
import org.json.JSONObject

@Stable
data class CaptureResultLauncher(
    private val launcher: ManagedActivityResultLauncher<ScanOptions, Result<WifiConfig>>,
) {
    private val scanOption = ScanOptions()
        .setOrientationLocked(false)
        .setBeepEnabled(false)

    fun launch() {
        launcher.launch(scanOption)
    }
}

@Composable
fun rememberCaptureResult(
    onCapture: (WifiConfig) -> Unit,
): CaptureResultLauncher {
    val launcher = rememberLauncherForActivityResult(
        contract = SwitchCaptureActivity.Contract()
    ) { result ->
        if (result.isSuccess) {
            onCapture(result.getOrNull()!!)
        }
    }
    return remember {
        CaptureResultLauncher(launcher = launcher)
    }
}

@Immutable
class ImportJsonContract(private val context: Context) :
    ActivityResultContract<Unit, Result<JSONObject>>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            setType(MineType.JSON)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Result<JSONObject> {
        if (resultCode == RESULT_OK) {
            return intent?.data?.let { uri ->
                try {
                    val contentResolver = context.contentResolver
                    val inputStream = contentResolver.openInputStream(uri)
                    val jsonText = inputStream?.bufferedReader().use { imput ->
                        imput?.readText()
                    }
                    if (jsonText != null) {
                        Result.success(JSONObject(jsonText))
                    } else {
                        Result.failure(Exception())
                    }
                } catch (e: Exception) {
                    Result.failure(Exception())
                }
            } ?: Result.failure(Exception())
        } else {
            return Result.failure(Exception())
        }
    }
}

@Stable
data class ImportJsonResultLauncher(
    private val launcher: ManagedActivityResultLauncher<Unit, Result<JSONObject>>,
) {
    fun launch() {
        launcher.launch(Unit)
    }
}

@Composable
fun rememberImportJsonResult(
    onSuccess: (JSONObject) -> Unit,
    onFailed: () -> Unit = { },
): ImportJsonResultLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ImportJsonContract(context)
    ) { result ->
        if (result.isSuccess) {
            onSuccess(result.getOrNull()!!)
        } else {
            onFailed()
        }
    }
    return remember {
        ImportJsonResultLauncher(launcher = launcher)
    }
}

data class WifiEnableResultLauncher(
    private val wifiManager: WifiManager,
    private val settingManager: SettingManager,
    private val launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    private val onAlreadyEnabled: () -> Unit,
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    private val intent = Intent(Settings.Panel.ACTION_WIFI)
    fun launch() {
        if (!isEnabled() || settingManager.getAlternativeConnectionEnabled()) {
            if (isAfterAndroidX()) {
                launcher.launch(intent)
            } else {
                @Suppress("DEPRECATION")
                wifiManager.isWifiEnabled = true
            }
        } else {
            onAlreadyEnabled()
        }
    }

    private fun isEnabled(): Boolean {
        return wifiManager.isWifiEnabled()
    }
}

@Composable
fun rememberWifiEnableResult(
    onEnable: () -> Unit,
): WifiEnableResultLauncher {
    val wifiManager =
        LocalContext.current.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    val settingManager = rememberSettingManager()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (wifiManager.isWifiEnabled()) {
            onEnable()
        }
    }
    return remember {
        WifiEnableResultLauncher(
            wifiManager = wifiManager,
            settingManager = settingManager,
            launcher = launcher,
            onAlreadyEnabled = onEnable
        )
    }
}
package io.github.lanlacope.nxsharinghelper.activity.component

import android.app.Activity.WIFI_SERVICE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanOptions
import io.github.lanlacope.nxsharinghelper.activity.SwitchCaptureActivity
import io.github.lanlacope.nxsharinghelper.clazz.ConnectionManager.WifiConfig
import io.github.lanlacope.nxsharinghelper.clazz.propaty.DevicePropaty

@Stable
data class CaptureResultLauncher(
    private val launcher: ManagedActivityResultLauncher<ScanOptions, Result<WifiConfig>>
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
    onCapture: (WifiConfig) -> Unit
): CaptureResultLauncher {
    val launcher =  rememberLauncherForActivityResult(
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

data class PermissionResultLauncher(
    private val context: Context,
    private val permission: String,
    private val launcher: ManagedActivityResultLauncher<String, Boolean>,
    private val onAlreadyGranted: () -> Unit
) {
    fun launch() {
        if (isGranted()) {
            launcher.launch(permission)
        } else {
            onAlreadyGranted()
        }
    }
    private fun isGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun rememberParmissionResult(
    permission: String,
    onGrant: () -> Unit
): PermissionResultLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGrant ->
        if (isGrant) {
            onGrant()
        }
    }
    return remember {
        PermissionResultLauncher(
            context = context,
            permission = permission,
            launcher = launcher,
            onAlreadyGranted = onGrant
        )
    }
}

data class WifiResultLauncher(
    private val wifiManager: WifiManager,
    private val launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    private val onAlreadyEnabled: () -> Unit
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    private val intent = Intent(Settings.Panel.ACTION_WIFI)
    fun launch() {
        if (isEnabled()) {
            if (DevicePropaty.isAfterAndroidX()) {
                launcher.launch(intent)
            }
            else {
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
fun rememberWifiResult(
    onEnable: () -> Unit
): WifiResultLauncher {
    val wifiManager =
        LocalContext.current.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (wifiManager.isWifiEnabled()) {
            onEnable()
        }
    }
    return remember {
        WifiResultLauncher(
            wifiManager = wifiManager,
            launcher = launcher,
            onAlreadyEnabled = onEnable
        )
    }
}
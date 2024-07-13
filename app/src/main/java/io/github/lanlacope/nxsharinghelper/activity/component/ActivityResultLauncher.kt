package io.github.lanlacope.nxsharinghelper.activity.component

import android.Manifest
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanOptions
import io.github.lanlacope.nxsharinghelper.activity.SwitchCaptureActivity
import io.github.lanlacope.nxsharinghelper.clazz.isAfterAndroidX
import io.github.lanlacope.nxsharinghelper.activity.SwitchCaptureActivity.WifiConfig

data class CaptureResultLauncher(
    private val launcher: ManagedActivityResultLauncher<ScanOptions, Result<WifiConfig>>
) {
    private val scanOption = ScanOptions()
        .setOrientationLocked(false)
        .setBeepEnabled(false)

    val launch = launcher.launch(scanOption)
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
    return CaptureResultLauncher(launcher)
}

data class PermissionResultLauncher(
    private val context: Context,
    private val permission: String,
    private val launcher: ManagedActivityResultLauncher<String, Boolean>
) {
    val launch = launcher.launch(permission)
    val isGranted = if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
        true
    } else false
}

@Composable
fun rememberCameraParmissionResult(
    onGrant: () -> Unit
): PermissionResultLauncher {
    val launcher =  rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGrant ->
        if (isGrant) {
            onGrant()
        }
    }
    return PermissionResultLauncher(
        LocalContext.current,
        Manifest.permission.CAMERA,
        launcher
    )
}

@Composable
fun rememberStorageParmissionResult(
    onGrant: () -> Unit
): PermissionResultLauncher {
    val launcher =  rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGrant ->
        if (isGrant) {
            onGrant()
        }
    }
    return PermissionResultLauncher(
        LocalContext.current,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        launcher
    )
}

data class WifiResultLauncher(
    private val context: Context,
    private val launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    private val intent = Intent(Settings.Panel.ACTION_WIFI)
    private val wifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    val launch = if (isAfterAndroidX()) launcher.launch(intent) else { }
    val isEnabled = wifiManager.isWifiEnabled
}

@Composable
fun rememberWifiResult(
    onEnable: () -> Unit
): WifiResultLauncher {
    val launcher =  rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        onEnable()
    }
    return if (isAfterAndroidX()) {
        WifiResultLauncher(
            LocalContext.current,
            launcher
        )
    } else {
        WifiResultLauncher(
            LocalContext.current,
            launcher
        )
    }
}
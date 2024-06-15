package io.github.lanlacope.nxsharinghelper.activitys

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import io.github.lanlacope.nxsharinghelper.NavigationView
import io.github.lanlacope.nxsharinghelper.classes.QrDecoder
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.classes.SwitchConnector
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme

class ScanActivity : ComponentActivity() {

    private val navigationMessage by lazy {
        mutableStateOf(getString(R.string.app_name))
    }

    private val qrDecoder = QrDecoder()

    private val switchConnector by lazy {
        SwitchConnector(this)
    }

    private val scanLouncher by lazy {
        registerForActivityResult(ScanContract()) { result ->
            if (!result.contents.isNullOrEmpty()) {

                println(result.contents)
                println("Succeed Scanning")
                qrDecoder.startDecode(result.contents)
                println(qrDecoder.decordingState)

                if (confirmDecoderResult()) {
                    println("Succeed Decoding")
                    switchConnector.startConnect(qrDecoder.decordingResult)
                    if (comfirmConnectResult()) {
                        println("Succeed Connecting")
                        startDownload()
                    }
                }
            } else {
                navigationMessage.value = getString(R.string.app_name)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startScan()

        setContent {
            NXSharingHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationView(navigationMessage)
                }
            }
        }
    }

    fun startScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val scanOption = ScanOptions().setOrientationLocked(false)
            scanLouncher.launch(scanOption)
        }
        else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            return
        }
    }

    private fun confirmDecoderResult() :Boolean {
        when (qrDecoder.decordingState) {
            QrDecoder.DecordingStates.SUCCESED_CREAR ->
                return true

            QrDecoder.DecordingStates.SUCCESED_FAIR ->
                return showDialog(getString(R.string.warning_low))

            QrDecoder.DecordingStates.SUCCESED_POOR ->
                return showDialog(getString(R.string.warning_middle))

            QrDecoder.DecordingStates.SUCCESED_BAD ->
                return showDialog(getString(R.string.warning_high))

            QrDecoder.DecordingStates.FAILED_UNKNOWN -> {
                navigationMessage.value = getString(R.string.faild_decode_some)
                return false
            }

            QrDecoder.DecordingStates.FAILED_NOTWIFI -> {
                navigationMessage.value = getString(R.string.failed_decode_notwifi)
                return false
            }

            QrDecoder.DecordingStates.FAILED_LOCALHOST -> {
                navigationMessage.value = getString(R.string.failed_decode_islocalhost)
                return false
            }

            else -> {
                navigationMessage.value = getString(R.string.app_name)
                return false
            }
        }
    }

    private fun showDialog(message: String): Boolean {

        var isPositive = false

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.warning_title))
            .setMessage(message)
            .setPositiveButton(
                getString(R.string.action_yes), { _, _ ->
                    isPositive = true
                    return@setPositiveButton
                }
            )
            .setNegativeButton(
                getString(R.string.action_no), { _, _ ->
                    isPositive = false
                    return@setNegativeButton
                }
            )

        return isPositive
    }

    private fun comfirmConnectResult():Boolean {
        when (switchConnector.connectingState) {
            SwitchConnector.ConnectingStates.SUCCESED ->
                return true

            SwitchConnector.ConnectingStates.FAILED -> {
                navigationMessage.value = getString(R.string.failed_connect)
                return false
            }

            else -> {
                navigationMessage.value = getString(R.string.app_name)
                return false
            }
        }
    }

    private fun startDownload() {
        // TODO :
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

}
package io.github.lanlacope.nxsharinghelper.activitys

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.journeyapps.barcodescanner.ScanOptions
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.classes.ConnectionManager
import io.github.lanlacope.nxsharinghelper.classes.DownloadManager
import io.github.lanlacope.nxsharinghelper.isAfterAndroidX
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme
import kotlinx.coroutines.launch

class ManagerHolder : ViewModel() {
    private var connectionManager: ConnectionManager? = null
    private var downloadManager: DownloadManager? = null

    fun getConnectionManager(context: Context): ConnectionManager {
        if (connectionManager == null) {
            connectionManager = ConnectionManager(context)
        }
        return connectionManager as ConnectionManager
    }

    fun getDownloadManager(context: Context): DownloadManager {
        if (downloadManager == null) {
            downloadManager = DownloadManager(context)
        }
        return downloadManager as DownloadManager
    }
}

class ResultActivity : ComponentActivity() {

    private val navigationMessage by lazy {
        mutableStateOf(getString(R.string.app_name))
    }

    private val isScanned by lazy {
        mutableStateOf(false)
    }

    private val managerHolder: ManagerHolder by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val captureLancher =
            registerForActivityResult(SwitchCaptureActivity.Contract()) { result ->
                if (result != null) {
                    val connectionManager = managerHolder.getConnectionManager(applicationContext)
                    isScanned.value = false
                    navigationMessage.value = getString(R.string.app_name)
                    connectionManager.start(result, onConnection)
                    navigationMessage.value = getString(R.string.waiting_connection)
                }
            }

        startScan(captureLancher)

        val share: () -> Unit = {
            // TODO :
        }

        val save: () -> Unit = {
            startSave()
        }

        val scan: () -> Unit = {
            startScan(captureLancher)
        }

        val checkLicense: () -> Unit = {
            val intent = Intent(applicationContext, LicenceActivity::class.java)
            startActivity(intent)
        }

        setContent {
            NXSharingHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationView(navigationMessage, share, save, scan, isScanned, checkLicense)
                }
            }
        }
    }

    fun startScan(louncher: ActivityResultLauncher<ScanOptions>) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val scanOption = ScanOptions().setOrientationLocked(false)
                louncher.launch(scanOption)
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
                return
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    val onConnection: () -> Unit = {
        managerHolder.viewModelScope.launch {
            try {
                println("Succeed Connecting")
                val connectionManager = managerHolder.getConnectionManager(applicationContext)
                val downloadManager = managerHolder.getDownloadManager(applicationContext)
                navigationMessage.value = getString(R.string.waiting_download)
                downloadManager.start()
                connectionManager.disConnection()
                isScanned.value = true
                navigationMessage.value = getString(R.string.succesful_download)
            } catch (e: Exception) {
                navigationMessage.value = getString(R.string.failed_download)
            }
        }
    }

    fun startSave() {
        try {
            val downloadManager = managerHolder.getDownloadManager(applicationContext)
            if (isAfterAndroidX()) {
                managerHolder.viewModelScope.launch {
                    downloadManager.saveFileToStorage()
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    managerHolder.viewModelScope.launch {
                        downloadManager.saveFileToStorage()
                    }
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    return
                }
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "in SAVE : ${e.toString()}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val downloadManager = managerHolder.getDownloadManager(applicationContext)
        downloadManager.clearCache()
        finish()
    }
}

@Composable
fun NavigationView(
    message: MutableState<String>,
    share: () -> Unit,
    save: () -> Unit,
    scan: () -> Unit,
    isScanned: MutableState<Boolean>,
    checkLicense: () -> Unit
) {

    val BUTTON_SIZE = 80.dp
    val BUTTON_PADDING = 30.dp
    val IMAGE_PADDING = 8.dp

    ConstraintLayout (
        modifier = Modifier.fillMaxSize()
    ) {
        val (license, navigation, scanButton, shareButton, saveButton) = createRefs()

        TextButton(
            modifier = Modifier
                .constrainAs(license) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                },
            onClick = checkLicense,
        ) {
            Text(
                text = stringResource(id = R.string.license_cover),
                modifier = Modifier.wrapContentSize()
            )
        }

        Text(
            modifier = Modifier
                .wrapContentSize()
                .constrainAs(navigation) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.matchParent
                    height = Dimension.wrapContent
                },
            textAlign = TextAlign.Center,
            text = message.value
        )

        if (isScanned.value) {
            FloatingActionButton(
                modifier = Modifier
                    .padding(
                        end = BUTTON_PADDING,
                        bottom = BUTTON_PADDING
                    )
                    .constrainAs(shareButton) {
                        end.linkTo(parent.end)
                        bottom.linkTo(saveButton.top)
                        width = Dimension.value(BUTTON_SIZE)
                        height = Dimension.value(BUTTON_SIZE)
                    },
                onClick = share
            ) {
                Image(
                    painter = painterResource(R.drawable.baseline_share_24),
                    contentDescription = "Share",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = IMAGE_PADDING)
                )
            }

            FloatingActionButton(
                modifier = Modifier
                    .padding(
                        end = BUTTON_PADDING,
                        bottom = BUTTON_PADDING
                    )
                    .constrainAs(saveButton) {
                        end.linkTo(parent.end)
                        bottom.linkTo(scanButton.top)
                        width = Dimension.value(BUTTON_SIZE)
                        height = Dimension.value(BUTTON_SIZE)
                    },
                onClick = save
            ) {
                Image(
                    painter = painterResource(R.drawable.baseline_download_24),
                    contentDescription = "Save",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = IMAGE_PADDING)

                )
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .padding(
                    end = BUTTON_PADDING,
                    bottom = BUTTON_PADDING
                )
                .constrainAs(scanButton) {
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.value(BUTTON_SIZE)
                    height = Dimension.value(BUTTON_SIZE)
                },
            onClick = scan
        ) {
            Image(
                painter = painterResource(R.drawable.baseline_qr_code_24),
                contentDescription = "Scan",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = IMAGE_PADDING)
            )
        }
    }
}

@Preview
@Composable
fun NavigationViewPreview(
    message: MutableState<String> = mutableStateOf("Message is here"),
    unit: () -> Unit = {},
    isScanned: MutableState<Boolean> = mutableStateOf(true)
) {
    NXSharingHelperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavigationView(message, unit, unit, unit, isScanned, unit)
        }
    }
}
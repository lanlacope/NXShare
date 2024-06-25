package io.github.lanlacope.nxsharinghelper.activitys

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
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
import io.github.lanlacope.nxsharinghelper.classes.ContentsDownloader
import io.github.lanlacope.nxsharinghelper.classes.ContentsSaver
import io.github.lanlacope.nxsharinghelper.classes.ContentsSharer
import io.github.lanlacope.nxsharinghelper.classes.DownloadData
import io.github.lanlacope.nxsharinghelper.isAfterAndroidX
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme
import kotlinx.coroutines.launch

class ManagerHolder : ViewModel() {

    var downloadData: DownloadData = DownloadData()

    private var connectionManager: ConnectionManager? = null

    fun connectionManager(context: Context): ConnectionManager {
        if (connectionManager == null) {
            connectionManager = ConnectionManager(context)
        }
        return connectionManager as ConnectionManager
    }
}

class ResultActivity : ComponentActivity() {

    private val managerHolder: ManagerHolder by viewModels()

    val wifiManager by lazy {
        applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val navigationMessage by lazy {
        mutableStateOf(getString(R.string.app_name))
    }

    private val isScanned by lazy {
        mutableStateOf(false)
    }

    private var isSaving = false

    private lateinit var captureLancher: ActivityResultLauncher<ScanOptions>
    private lateinit var cameraParemissionLauncher: ActivityResultLauncher<String>
    private lateinit var strageParemissionLauncher: ActivityResultLauncher<String>
    private lateinit var wifiPanelLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ActivityResultLauncherを定義
        captureLancher =
            registerForActivityResult(SwitchCaptureActivity.Contract()) { result ->
                if (result != null) {
                    val connectionManager = managerHolder.connectionManager(applicationContext)

                    // ビューの更新
                    isScanned.value = false
                    navigationMessage.value = getString(R.string.app_name)

                    connectionManager.start(result, onConnection)

                    // ビューの更新
                    navigationMessage.value = getString(R.string.waiting_connection)
                }
            }

        cameraParemissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGrant ->
                if (isGrant) {
                    startScan()
                }
            }

        wifiPanelLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (wifiManager.isWifiEnabled) {
                startScan()
            }
        }

        strageParemissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGrant ->
                if (isGrant) {
                    startSave()
                }
            }

        // 初回起動
        startScan()

        // ボタンの動作を定義
        val share: () -> Unit = {
            startShare()
        }

        val save: () -> Unit = {
            startSave()
        }

        val scan: () -> Unit = {
            startScan()
        }

        val checkLicense: () -> Unit = {
            val intent = Intent(this, LicenceActivity::class.java)
            startActivity(intent)
        }

        setContent {
            NXSharingHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationView(
                        message = navigationMessage,
                        share = share,
                        save = save,
                        scan = scan,
                        isScanned = isScanned,
                        checkLicense = checkLicense
                    )
                }
            }
        }
    }

    fun startScan() {
        try {
            if (checkCameraPermition()) {
                if (checkWifiEnabled()) {
                    val scanOption = ScanOptions()
                        .setOrientationLocked(false)
                        .setBeepEnabled(false)

                    captureLancher.launch(scanOption)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    val onConnection: () -> Unit = {
        managerHolder.viewModelScope.launch {
            try {
                Log.println(Log.INFO, "NXShare", "Successful Connecting")

                val connectionManager = managerHolder.connectionManager(applicationContext)
                val contentsDownloader = ContentsDownloader(applicationContext)

                // ビューの更新
                navigationMessage.value = getString(R.string.waiting_download)

                contentsDownloader.start()
                managerHolder.downloadData = contentsDownloader.downloadData
                connectionManager.disConnection()

                // ビューの更新
                isScanned.value = true
                navigationMessage.value = getString(R.string.succesful_download)
            } catch (e: Exception) {
                // ビューの更新
                navigationMessage.value = getString(R.string.failed_download)
            }
        }
    }

    private fun startSave() {
        try {
            if (ckeckStoragePermission()) {
                managerHolder.viewModelScope.launch {
                    isSaving = true
                    val contentsSaver = ContentsSaver(applicationContext)
                    contentsSaver.save(managerHolder.downloadData.copy())
                    isSaving = false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "in SAVE : ${e.toString()}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun startShare() {
        val contentsSharer = ContentsSharer(this)
        val intent = contentsSharer.createShareIntent(managerHolder.downloadData.copy())
        startActivity(intent)
    }

    fun clearCache() {
        if (!isSaving) {
            this.cacheDir.listFiles()?.forEach { file ->
                file.delete()
            }
        }
    }

    private fun checkCameraPermition(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            cameraParemissionLauncher.launch(Manifest.permission.CAMERA)
            return false
        }
    }

    private fun ckeckStoragePermission(): Boolean {
        if (isAfterAndroidX()) {
            return true
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            } else {
                strageParemissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                return false
            }
        }
    }

    private fun checkWifiEnabled(): Boolean {
        if (isAfterAndroidX()) {
            if (wifiManager.isWifiEnabled) {
                return true
            } else {
                val intent = Intent(Settings.Panel.ACTION_WIFI)
                wifiPanelLauncher.launch(intent)
                return false
            }
        } else {
            return true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearCache()
        finish()
    }
}

@Composable
private fun NavigationView(
    message: MutableState<String>,
    share: () -> Unit,
    save: () -> Unit,
    scan: () -> Unit,
    isScanned: MutableState<Boolean>,
    checkLicense: () -> Unit
) {

    val SOMEBUTTON_SIZE = 80.dp
    val BUTTON_PADDING = 30.dp
    val MAIN_BUTTON_SIZE = 90.dp
    val IMAGE_PADDING = 8.dp

    ConstraintLayout (
        modifier = Modifier
            .fillMaxSize()
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
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.wrapContentSize()
            )
        }

        Text(
            textAlign = TextAlign.Center,
            text = message.value,
            modifier = Modifier
                .wrapContentSize()
                .constrainAs(navigation) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.matchParent
                    height = Dimension.wrapContent
                }

        )

        if (isScanned.value) {
            FloatingActionButton(
                onClick = share,
                containerColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .padding(
                        end = BUTTON_PADDING,
                        bottom = BUTTON_PADDING
                    )
                    .constrainAs(shareButton) {
                        end.linkTo(parent.end)
                        bottom.linkTo(saveButton.top)
                        width = Dimension.value(SOMEBUTTON_SIZE)
                        height = Dimension.value(SOMEBUTTON_SIZE)
                    }

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
                onClick = save,
                containerColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .padding(
                        end = BUTTON_PADDING,
                        bottom = BUTTON_PADDING
                    )
                    .constrainAs(saveButton) {
                        end.linkTo(parent.end)
                        bottom.linkTo(scanButton.top)
                        width = Dimension.value(SOMEBUTTON_SIZE)
                        height = Dimension.value(SOMEBUTTON_SIZE)
                    }

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
            onClick = scan,
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(
                    end = BUTTON_PADDING,
                    bottom = BUTTON_PADDING
                )
                .constrainAs(scanButton) {
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.value(MAIN_BUTTON_SIZE)
                    height = Dimension.value(MAIN_BUTTON_SIZE)
                }

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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun NavigationViewPreviewLight(
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NavigationViewPreviewDark(
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
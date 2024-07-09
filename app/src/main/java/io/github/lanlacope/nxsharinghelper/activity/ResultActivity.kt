package io.github.lanlacope.nxsharinghelper.activity

import android.Manifest
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import io.github.lanlacope.nxsharinghelper.`class`.ConnectionManager
import io.github.lanlacope.nxsharinghelper.`class`.ContentsDownloader
import io.github.lanlacope.nxsharinghelper.`class`.ContentsSaver
import io.github.lanlacope.nxsharinghelper.`class`.ContentsSharer
import io.github.lanlacope.nxsharinghelper.`class`.DownloadData
import io.github.lanlacope.nxsharinghelper.`class`.isAfterAndroidX
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme
import io.github.lanlacope.nxsharinghelper.widgit.FloatingActionButton
import kotlinx.coroutines.launch

class ResultViewModel : ViewModel() {

    var downloadData: DownloadData = DownloadData()
    val isScanned = mutableStateOf(false)
    val navigationMessage = mutableStateOf("")
}

class ResultActivity : ComponentActivity() {

    private val viewModel: ResultViewModel by viewModels()

    private var isSaving = false

    private val wifiManager by lazy {
        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }

    private lateinit var captureLancher: ActivityResultLauncher<ScanOptions>
    private lateinit var cameraParemissionLauncher: ActivityResultLauncher<String>
    private lateinit var strageParemissionLauncher: ActivityResultLauncher<String>
    private lateinit var wifiPanelLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigationMessage.value = getString(R.string.app_name)

        // ActivityResultLauncherを定義
        captureLancher =
            registerForActivityResult(SwitchCaptureActivity.Contract()) { result ->
                if (result != null) {
                    val connectionManager = ConnectionManager(applicationContext)

                    // ビューの更新
                    viewModel.isScanned.value = false
                    viewModel.navigationMessage.value = getString(R.string.app_name)

                    connectionManager.start(result, onConnection)

                    // ビューの更新
                    viewModel.navigationMessage.value = getString(R.string.waiting_connection)
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

        val save: () -> Unit = {
            startSave()
        }

        val scan: () -> Unit = {
            startScan()
        }

        setContent {
            NXSharingHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation(
                        viewModel = viewModel,
                        save = save,
                        scan = scan
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
        viewModel.viewModelScope.launch {
            try {
                Log.println(Log.INFO, "NXShare", "Successful Connecting")

                val contentsDownloader = ContentsDownloader(applicationContext)

                // ビューの更新
                viewModel.navigationMessage.value = getString(R.string.waiting_download)

                contentsDownloader.start()
                viewModel.downloadData = contentsDownloader.downloadData

                // ビューの更新
                viewModel.isScanned.value = true
                viewModel.navigationMessage.value = getString(R.string.succesful_download)
            } catch (e: Exception) {
                // ビューの更新
                viewModel.navigationMessage.value = getString(R.string.failed_download)
            }
        }
    }

    private fun startSave() {
        try {
            if (ckeckStoragePermission()) {
                viewModel.viewModelScope.launch {
                    isSaving = true
                    val contentsSaver = ContentsSaver(applicationContext)
                    contentsSaver.save(viewModel.downloadData.copy())
                    isSaving = false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "in SAVE : ${e.toString()}", Toast.LENGTH_LONG)
                .show()
        }
    }

    fun clearCache() {
        if (!isSaving) {
            cacheDir.listFiles()?.forEach { file ->
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
private fun Navigation(
    viewModel: ResultViewModel,
    save: () -> Unit,
    scan: () -> Unit
) {

    val context = LocalContext.current

    val SOMEBUTTON_SIZE = 80.dp
    val BUTTON_PADDING = 30.dp
    val MAIN_BUTTON_SIZE = 90.dp
    val IMAGE_PADDING = 8.dp

    ConstraintLayout (
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (
            license,
            setting,
            navigation,
            scanButton,
            shareButton,
            saveButton
        ) = createRefs()

        TextButton(
            modifier = Modifier
                .constrainAs(license) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                },
            onClick = {
                val intent = Intent(context, LicenceActivity::class.java)
                context.startActivity(intent)
            },
        ) {
            Text(
                text = stringResource(id = R.string.license_cover),
                modifier = Modifier.wrapContentSize()
            )
        }
        TextButton(
            modifier = Modifier
                .constrainAs(setting) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                },
            onClick = {
                val intent = Intent(context, EditActivity::class.java)
                context.startActivity(intent)
            },
        ) {
            Text(
                text = stringResource(id = R.string.setting_cover),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.wrapContentSize()
            )
        }

        Text(
            textAlign = TextAlign.Center,
            text = viewModel.navigationMessage.value,
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

        if (viewModel.isScanned.value) {

            val onShareButtonClick = {
                val contentsSharer = ContentsSharer(context)
                val chooserIntent = contentsSharer.createCustomChooserIntrnt(viewModel.downloadData.copy())
                val pendingIntent = contentsSharer.createPendingIntent()
                val intent = Intent.createChooser(chooserIntent, null, pendingIntent.intentSender)
                context.startActivity(intent)
            }

            val onShareButtonLongClick = {
                val contentsSharer = ContentsSharer(context)
                val chooserIntent = contentsSharer.createChooserIntent(viewModel.downloadData.copy())
                val pendingIntent = contentsSharer.createPendingIntent()
                val intent = Intent.createChooser(chooserIntent, null, pendingIntent.intentSender)
                context.startActivity(intent)
            }

            FloatingActionButton(
                onClick = onShareButtonClick,
                onLongClick =onShareButtonLongClick,
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
private fun NavigationPreviewLight(
) {
    NXSharingHelperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NavigationPreviewDark(
) {
    NXSharingHelperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
        }
    }
}
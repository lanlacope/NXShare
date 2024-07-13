package io.github.lanlacope.nxsharinghelper.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.rememberCameraParmissionResult
import io.github.lanlacope.nxsharinghelper.activity.component.rememberCaptureResult
import io.github.lanlacope.nxsharinghelper.activity.component.rememberStorageParmissionResult
import io.github.lanlacope.nxsharinghelper.activity.component.rememberWifiResult
import io.github.lanlacope.nxsharinghelper.clazz.ConnectionManager
import io.github.lanlacope.nxsharinghelper.clazz.ContentsSaver
import io.github.lanlacope.nxsharinghelper.clazz.ContentsSharer
import io.github.lanlacope.nxsharinghelper.clazz.getGameId
import io.github.lanlacope.nxsharinghelper.clazz.rememberContentsData
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme
import kotlinx.coroutines.launch
import io.github.lanlacope.nxsharinghelper.widgit.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ResultActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NXSharingHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ContentsSaver(this).clearCache()
        finish()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Navigation() {
    ConstraintLayout (
        modifier = Modifier
            .fillMaxSize()
    ) {
        val context = LocalContext.current
        val scope = CoroutineScope(Dispatchers.Main)

        var isScanned by rememberSaveable {
            mutableStateOf(false)
        }
        var navigationMessage by rememberSaveable() {
            mutableStateOf(context.getString(R.string.app_name))
        }
        val contentsData = rememberContentsData()

        val SOMEBUTTON_SIZE = 80.dp
        val BUTTON_PADDING = 30.dp
        val MAIN_BUTTON_SIZE = 90.dp
        val IMAGE_PADDING = 8.dp

        val (
            license,
            setting,
            navigation,
            scanButton,
            shareButton,
            saveButton
        ) = createRefs()

        TextButton(
            onClick = {
                val intent = Intent(context, LicenceActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .constrainAs(license) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                }

        ) {
            Text(
                text = stringResource(id = R.string.license_cover),
                modifier = Modifier.wrapContentSize()
            )
        }
        TextButton(
            onClick = {
                val intent = Intent(context, EditActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .constrainAs(setting) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                }

        ) {
            Text(
                text = stringResource(id = R.string.setting_cover),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.wrapContentSize()
            )
        }

        Text(
            textAlign = TextAlign.Center,
            text = navigationMessage,
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

        if (isScanned) {

            val onShareButtonClick = {
                val contentsSharer = ContentsSharer(context)
                val intent = contentsSharer.createCustomChooserIntrnt(contentsData.value.copy())
                context.startActivity(intent)
            }

            val onShareButtonLongClick = {
                val contentsSharer = ContentsSharer(context)
                val intent = contentsSharer.createChooserIntent(contentsData.value.copy())
                context.startActivity(intent)
            }

            FloatingActionButton(
                onClick = onShareButtonClick,
                onLongClick = onShareButtonLongClick,
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
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onShareButtonLongClick
                    )

            ) {
                Image(
                    painter = painterResource(R.drawable.baseline_share_24),
                    contentDescription = "Share",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = IMAGE_PADDING)
                )
            }

            val clipboardManager = LocalClipboardManager.current

            val save = {
                scope.launch {
                    val contentsSaver = ContentsSaver(context)
                    contentsSaver.save(contentsData.value.copy())
                }
            }

            val storagePermissionResult = rememberStorageParmissionResult {
                save()
            }

            val onSaveButtonClick: () -> Unit = {
                if (storagePermissionResult.isGranted) {
                    save()
                } else {
                    storagePermissionResult.launch
                }
            }

            val onSaveButtonLongClick = {
                val ids = getGameId(contentsData.value.fileNames)
                ids.forEach { id ->
                    clipboardManager.setText(AnnotatedString(id))
                }
            }

            FloatingActionButton(
                onClick = onSaveButtonClick,
                onLongClick = onSaveButtonLongClick,
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

        val onConnection: () -> Unit = {
            scope.launch {
                try {
                    // ビューの更新
                    navigationMessage = context.getString(R.string.waiting_download)

                    contentsData.download()

                    // ビューの更新
                    isScanned = true
                    navigationMessage = context.getString(R.string.succesful_download)

                } catch (e: Exception) {
                    // ビューの更新
                    navigationMessage = context.getString(R.string.failed_download)
                } finally {
                    ConnectionManager(context).disconnection()
                }
            }
        }

        val captureResulte =  rememberCaptureResult { wifiConfig ->

            // ビューの更新
            isScanned = false
            navigationMessage = context.getString(R.string.app_name)

            ConnectionManager(context).start(wifiConfig, onConnection)

            // ビューの更新
            navigationMessage = context.getString(R.string.waiting_connection)
        }

        val wifiResult = rememberWifiResult {
            captureResulte.launch
        }

        val cameraParmissionResult = rememberCameraParmissionResult {
            wifiResult.launch
        }



        val onScanButtonClick = {
            if (cameraParmissionResult.isGranted) {
                if (wifiResult.isEnabled) {
                    cameraParmissionResult.launch
                } else {
                    wifiResult.launch
                }
            } else {
                cameraParmissionResult.launch
            }
        }

        FloatingActionButton(
            onClick = onScanButtonClick,
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
                painter = painterResource(R.drawable.baseline_qr_code_scanner_24),
                contentDescription = "Scan",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = IMAGE_PADDING)
            )
        }
    }
}
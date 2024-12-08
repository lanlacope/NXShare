package io.github.lanlacope.nxsharinghelper.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
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
import io.github.lanlacope.compose.effect.rememberPermissionGrantResult
import io.github.lanlacope.compose.ui.animation.SlideInAnimated
import io.github.lanlacope.compose.ui.button.CombinedFloatingActionButton
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.rememberCaptureResult
import io.github.lanlacope.nxsharinghelper.activity.component.rememberWifiEnableResult
import io.github.lanlacope.nxsharinghelper.clazz.ConnectionManager
import io.github.lanlacope.nxsharinghelper.clazz.ContentSaver
import io.github.lanlacope.nxsharinghelper.clazz.ContentSharer
import io.github.lanlacope.nxsharinghelper.clazz.propaty.getGameId
import io.github.lanlacope.nxsharinghelper.clazz.rememberContentData
import io.github.lanlacope.nxsharinghelper.ui.theme.AppTheme
import kotlinx.coroutines.launch

/*
 * 読み取りの開始、共有、保存など主要操作を提供する
 */
class ResultActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }
}


@Composable
private fun Navigation() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        val scope = rememberCoroutineScope()

        var isScanned by rememberSaveable { mutableStateOf(false) }
        var navigationMessage by rememberSaveable { mutableStateOf(context.getString(R.string.app_name)) }
        val contentsData = rememberContentData()

        val SOMEBUTTON_SIZE = 80.dp
        val BUTTON_PADDING = 30.dp
        val MAIN_BUTTON_SIZE = 90.dp
        val IMAGE_PADDING = 8.dp

        val (
            licenseRef,
            settingRef,
            navigationRef,
            scanButtonRef,
            shareButtonRef,
            saveButtonRef,
        ) = createRefs()

        TextButton(
            onClick = {
                val intent = Intent(context, LicenceActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .constrainAs(licenseRef) {
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
                .constrainAs(settingRef) {
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

        AnimatedContent(
            targetState = navigationMessage,
            modifier = Modifier
                .wrapContentSize()
                .constrainAs(navigationRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }

        ) { message ->
            Text(
                textAlign = TextAlign.Center,
                text = message,
                modifier = Modifier.wrapContentSize()
            )
        }

        SlideInAnimated(
            visible = isScanned,
            modifier = Modifier
                .wrapContentSize()
                .constrainAs(shareButtonRef) {
                    end.linkTo(parent.end)
                    bottom.linkTo(saveButtonRef.top)
                }
        ) {
            CombinedFloatingActionButton(
                onClick = {
                    val contentSharer = ContentSharer(context)
                    val intent =
                        contentSharer.createCustomChooserIntent(contentsData.getData().copy())
                    context.startActivity(intent)
                },
                onLongClick = {
                    val contentSharer = ContentSharer(context)
                    val intent = contentSharer.createChooserIntent(contentsData.getData().copy())
                    context.startActivity(intent)
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(SOMEBUTTON_SIZE)
                    .padding(
                        end = BUTTON_PADDING,
                        bottom = BUTTON_PADDING
                    )

            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = IMAGE_PADDING)
                )
            }
        }

        SlideInAnimated(
            visible = isScanned,
            modifier = Modifier
                .wrapContentSize()
                .constrainAs(saveButtonRef) {
                    end.linkTo(parent.end)
                    bottom.linkTo(scanButtonRef.top)
                }
        ) {
            val storagePermissionResult = rememberPermissionGrantResult(
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) {
                scope.launch { ContentSaver(context).save(contentsData.getData().copy()) }
            }

            CombinedFloatingActionButton(
                onClick = {
                    storagePermissionResult.launch()
                    scope.launch { ContentSaver(context).save(contentsData.getData().copy()) }
                },
                onLongClick = {
                    val ids = getGameId(contentsData.getData().fileNames)
                    ids.forEach { id ->
                        clipboardManager.setText(AnnotatedString(id))
                    }
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(SOMEBUTTON_SIZE)
                    .padding(
                        end = BUTTON_PADDING,
                        bottom = BUTTON_PADDING
                    )

            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_download_24),
                    contentDescription = "Save",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = IMAGE_PADDING)

                )
            }
        }

        val onConnection = ConnectionManager.OnConnection(
            onSuccesful = { connectionManager ->
                scope.launch {
                    // ビューの更新
                    navigationMessage = context.getString(R.string.waiting_download)

                    try {
                        contentsData.download()
                    } catch (e: Exception) {
                        navigationMessage = context.getString(R.string.failed_download)
                        connectionManager.disconnection()
                    }

                    // ビューの更新
                    isScanned = true
                    navigationMessage = context.getString(R.string.succesful_download)
                    connectionManager.disconnection()
                }
            },
            onFailed = { connectionManager ->
                navigationMessage = context.getString(R.string.failed_connect)
                connectionManager.disconnection()
            }
        )

        val captureResult = rememberCaptureResult { wifiConfig ->
            // ビューの更新
            isScanned = false
            navigationMessage = context.getString(R.string.app_name)

            ConnectionManager(context).start(wifiConfig, onConnection)

            // ビューの更新
            navigationMessage = context.getString(R.string.waiting_connection)
        }

        val wifiResult = rememberWifiEnableResult {
            captureResult.launch()
        }

        val cameraParmissionResult = rememberPermissionGrantResult(
            permission = Manifest.permission.CAMERA
        ) {
            wifiResult.launch()
        }

        FloatingActionButton(
            onClick = { cameraParmissionResult.launch() },
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(
                    end = BUTTON_PADDING,
                    bottom = BUTTON_PADDING
                )
                .constrainAs(scanButtonRef) {
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.value(MAIN_BUTTON_SIZE)
                    height = Dimension.value(MAIN_BUTTON_SIZE)
                }

        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_qr_code_scanner_24),
                contentDescription = "Scan",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = IMAGE_PADDING)
            )
        }
    }
}
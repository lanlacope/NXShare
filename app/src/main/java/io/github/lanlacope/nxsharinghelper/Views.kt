package io.github.lanlacope.nxsharinghelper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import io.github.lanlacope.nxsharinghelper.activitys.ScanActivity
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme

@Composable
fun NavigationView(
    message: MutableState<String>,
    share: () -> Unit,
    save: () -> Unit,
    scan: () -> Unit,
    isScanned: MutableState<Boolean>
) {

    val BUTTON_SIZE = 80.dp
    val scanActivity = LocalContext.current as? ScanActivity

    ConstraintLayout (
        modifier = Modifier.fillMaxSize()
    ) {
        val (text, scanButton, shareButton, saveButton) = createRefs()

        Text(
            modifier = Modifier
                .wrapContentSize()
                .constrainAs(text) {
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
                        end = 30.dp,
                        bottom = 30.dp
                    )
                    .constrainAs(shareButton) {
                        end.linkTo(parent.end)
                        bottom.linkTo(saveButton.top)
                        width = Dimension.value(BUTTON_SIZE)
                        height = Dimension.value(BUTTON_SIZE)
                    },

                onClick = share
            ) {

            }

            FloatingActionButton(
                modifier = Modifier
                    .padding(
                        end = 30.dp,
                        bottom = 30.dp
                    )
                    .constrainAs(saveButton) {
                        end.linkTo(parent.end)
                        bottom.linkTo(scanButton.top)
                        width = Dimension.value(BUTTON_SIZE)
                        height = Dimension.value(BUTTON_SIZE)
                    },
                onClick = save
            ) {

            }
        }

        FloatingActionButton(
            modifier = Modifier
                .padding(
                    end = 30.dp,
                    bottom = 30.dp
                )
                .constrainAs(scanButton) {
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.value(BUTTON_SIZE)
                    height = Dimension.value(BUTTON_SIZE)
                },
            
            onClick = scan
        ) {

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
            NavigationView(message, unit, unit, unit, isScanned)
        }
    }
}
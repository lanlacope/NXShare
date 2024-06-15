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
import io.github.lanlacope.nxsharinghelper.activitys.ScanActivity
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme

@Composable
fun NavigationView(message: MutableState<String>) {

    val scanActivity = LocalContext.current as? ScanActivity

    Box (
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center),
            textAlign = TextAlign.Center,
            text = message.value
        )

        FloatingActionButton(
            modifier = Modifier
                .padding(
                    end = 30.dp,
                    bottom = 30.dp
                )
                .size(50.dp)
                .align(Alignment.BottomEnd),
            
            onClick = {
                scanActivity?.startScan()
            }
        ) {

        }
    }
}

@Preview
@Composable
fun NavigationViewPreview(message: MutableState<String> = mutableStateOf("Message is here")) {
    NXSharingHelperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavigationView(message)
        }
    }
}
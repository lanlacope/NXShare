package io.github.lanlacope.nxsharinghelper.activitys

import android.os.Bundle
import android.text.Html
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme

class LicenceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NXSharingHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LicenceView()
                }
            }
        }
    }
}

@Preview
@Composable
private fun LicenceView() {
    Text(
        text = stringResource(id = R.string.license),
        modifier = Modifier
            .fillMaxSize()
    )
}
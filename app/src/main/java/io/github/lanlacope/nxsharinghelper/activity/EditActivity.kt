package io.github.lanlacope.nxsharinghelper.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.ChangeAppThemeDialog
import io.github.lanlacope.nxsharinghelper.clazz.SettingManager
import io.github.lanlacope.nxsharinghelper.ui.theme.Gray
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme
import io.github.lanlacope.nxsharinghelper.widgit.Row

class EditActivity: ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            NXSharingHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingList()
                }
            }
        }
    }
}

@Composable
fun SettingList() {

    val context = LocalContext.current
    val settingManager = SettingManager(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val TEXT_PADDING = 10.dp

        val shown = remember {
            mutableStateOf(false)
        }
        val selectedTheme = remember {
            mutableStateOf(settingManager.getAppTheme())
        }
        val onClick = {
            shown.value = true
        }
        Row(
            onClick = onClick,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()

        ) {
            Text(
                text = stringResource(id = R.string.summary_theme),
                maxLines = 1,
                minLines = 1,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .padding(all = TEXT_PADDING)

            )
            Text(
                text = selectedTheme.value,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    color = Gray
                ),
                maxLines = 1,
                minLines = 1,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .padding(end = 20.dp)

            )

        }
        ChangeAppThemeDialog(
            shown = shown,
            selectedTheme = selectedTheme
        )

        var substituteConnectionEnabled by remember {
            mutableStateOf(settingManager.getAlternativeConnectionEnabled())
        }
        val onSwitchChange = {
            substituteConnectionEnabled = !substituteConnectionEnabled
            settingManager.changeAlternativeConnectionEnabled(substituteConnectionEnabled)
        }
        Row(
            onClick = onSwitchChange,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()

        ) {
            Text(
                text = stringResource(id = R.string.summary_alternative_connection_enabled),
                maxLines = 1,
                minLines = 1,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .padding(all = TEXT_PADDING)

            )
            Switch(
                checked = substituteConnectionEnabled,
                onCheckedChange = {
                    substituteConnectionEnabled = !substituteConnectionEnabled
                    settingManager.changeAlternativeConnectionEnabled(substituteConnectionEnabled)
                },
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .padding(end = 50.dp)

            )
        }

        Row(
            onClick = {
                val intent = Intent(context, EditPackageInfoActivity::class.java)
                context.startActivity(intent)
            },
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()

        ) {
            Text(
                text = stringResource(id = R.string.setting_app),
                maxLines = 1,
                minLines = 1,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .padding(all = TEXT_PADDING)

            )
        }

        Row(
            onClick = {
                val intent = Intent(context, EditGameInfoActivity::class.java)
                context.startActivity(intent)
            },
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()

        ) {
            Text(
                text = stringResource(id = R.string.setting_myset),
                maxLines = 1,
                minLines = 1,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .padding(all = TEXT_PADDING)

            )
        }
    }
}
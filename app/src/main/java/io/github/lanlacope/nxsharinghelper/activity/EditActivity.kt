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
import io.github.lanlacope.nxsharinghelper.activity.component.ComponentValue
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SettingJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.rememberSettingManager
import io.github.lanlacope.nxsharinghelper.ui.theme.Gray
import io.github.lanlacope.nxsharinghelper.ui.theme.AppTheme
import io.github.lanlacope.nxsharinghelper.widgit.Row

class EditActivity: ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            AppTheme {
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
    val settingManager = rememberSettingManager()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val TEXT_VERTICAL_PADDING = 15.dp

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
                    .padding(
                        start = ComponentValue.DISPLAY_PADDING_START,
                        top = TEXT_VERTICAL_PADDING,
                        bottom = TEXT_VERTICAL_PADDING
                    )

            )
            Text(
                text = when (selectedTheme.value) {
                    SettingJsonPropaty.THEME_LIGHT -> stringResource(id = R.string.summary_theme_light)
                    SettingJsonPropaty.THEME_DARK -> stringResource(id = R.string.summary_theme_dark)
                    else -> stringResource(id = R.string.summary_theme_system)
                },
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    color = Gray
                ),
                maxLines = 1,
                minLines = 1,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .padding(end = TEXT_VERTICAL_PADDING)

            )

        }
        ChangeAppThemeDialog(
            shown = shown,
            selectedTheme = selectedTheme
        )

        var alternativeConnectionEnabled by remember {
            mutableStateOf(settingManager.getAlternativeConnectionEnabled())
        }

        val onSwitchChange = {
            alternativeConnectionEnabled = !alternativeConnectionEnabled
            settingManager.changeAlternativeConnectionEnabled(alternativeConnectionEnabled)
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
                    .padding(
                        start = ComponentValue.DISPLAY_PADDING_START,
                        top = TEXT_VERTICAL_PADDING,
                        bottom = TEXT_VERTICAL_PADDING
                    )

            )
            Switch(
                checked = alternativeConnectionEnabled,
                onCheckedChange = {
                    onSwitchChange()
                },
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .padding(end = ComponentValue.DISPLAY_PADDING_END + 30.dp)

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
                    .padding(
                        start = ComponentValue.DISPLAY_PADDING_START,
                        top = TEXT_VERTICAL_PADDING,
                        bottom = TEXT_VERTICAL_PADDING
                    )

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
                    .padding(
                        start = ComponentValue.DISPLAY_PADDING_START,
                        top = TEXT_VERTICAL_PADDING,
                        bottom = TEXT_VERTICAL_PADDING
                    )

            )
        }
    }
}
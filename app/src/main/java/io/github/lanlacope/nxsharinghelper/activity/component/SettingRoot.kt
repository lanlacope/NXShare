package io.github.lanlacope.nxsharinghelper.activity.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import io.github.lanlacope.compose.ui.action.setting.SettingSwitchToggle
import io.github.lanlacope.compose.ui.action.setting.SettingTextArea
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.dialog.ThemeSelectDialog
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SettingJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.rememberSettingManager
import io.github.lanlacope.nxsharinghelper.ui.theme.updateTheme

/*
 * 設定の一覧を
 * その他の基本設定を提供する
 */

@Composable
fun SettingRoot(navController: NavHostController) {

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {

        val settingManager = rememberSettingManager()

        var themeSelectDialogShown by remember { mutableStateOf(false) }
        var selectedTheme by remember { mutableStateOf(settingManager.getAppTheme()) }
        val themes = remember {
            SettingJsonPropaty.APP_THEME_LIST.associateWith { theme ->
                when (theme) {
                    SettingJsonPropaty.THEME_LIGHT -> context.getString(R.string.summary_theme_light)
                    SettingJsonPropaty.THEME_DARK -> context.getString(R.string.summary_theme_dark)
                    else -> context.getString(R.string.summary_theme_system)
                }
            }.toMutableStateMap()
        }

        SettingTextArea(
            text = stringResource(id = R.string.summary_theme),
            value = themes[selectedTheme]!!,
            onClick = { themeSelectDialogShown = true },
            modifier = Modifier.fillMaxWidth()
        )

        ThemeSelectDialog(
            expanded = themeSelectDialogShown,
            selectedTheme = selectedTheme,
            themes = themes,
            onConfirm = { newTheme ->
                selectedTheme = newTheme
                settingManager.changeAppTheme(newTheme)
                updateTheme()
                themeSelectDialogShown = false
            },
            onCancel = { themeSelectDialogShown = false }
        )

        var alternativeConnectionEnabled by remember { mutableStateOf(settingManager.getAlternativeConnectionEnabled()) }

        SettingSwitchToggle(
            text = stringResource(id = R.string.summary_alternative_connection_enabled),
            checked = alternativeConnectionEnabled,
            onClick = {
                alternativeConnectionEnabled = !alternativeConnectionEnabled
                settingManager.changeAlternativeConnectionEnabled(alternativeConnectionEnabled)
            },
            modifier = Modifier.fillMaxWidth()
        )

        SettingTextArea(
            text = stringResource(id = R.string.setting_app),
            value = "",
            onClick = { navController.navigate("package") },
            modifier = Modifier.fillMaxWidth()
        )

        SettingTextArea(
            text = stringResource(id = R.string.setting_myset),
            value = "",
            onClick = { navController.navigate("myset") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
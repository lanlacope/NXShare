package io.github.lanlacope.nxsharinghelper.activity.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.lanlacope.compose.ui.dialog.option.RadioButtonDialog
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.clazz.AppTheme

@Composable
fun ThemeSelectDialog(
    expanded: Boolean,
    selectedTheme: AppTheme,
    themes: Map<AppTheme, String>,
    onConfirm: (theme: AppTheme) -> Unit,
    onCancel: () -> Unit,
) {
    RadioButtonDialog(
        title = stringResource(id = R.string.dialog_title_theme),
        options = themes,
        selectedOption = selectedTheme,
        expanded = expanded,
        onConfirm = onConfirm,
        confirmText = stringResource(id = R.string.dialog_positive_apply),
        onCancel = onCancel,
        cancelText = stringResource(id = R.string.dialog_negative_cancel)
    )
}
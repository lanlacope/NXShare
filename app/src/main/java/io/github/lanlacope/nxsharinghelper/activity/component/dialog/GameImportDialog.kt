package io.github.lanlacope.nxsharinghelper.activity.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.lanlacope.compose.ui.action.option.CompactOptionCheckBox
import io.github.lanlacope.compose.ui.dialog.GrowDialog
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.rememberImportJsonResult
import org.json.JSONObject

@Composable
fun GameImportDialog(
    expanded: Boolean,
    onConfirm: (overwrite: Boolean, jsonObject: JSONObject) -> Unit,
    onCancel: () -> Unit,
) {
    var overwrite by remember { mutableStateOf(false) }

    val jsonImportResult = rememberImportJsonResult { jsonObject ->
        onConfirm(overwrite, jsonObject)
    }

    GrowDialog(
        title = stringResource(id = R.string.dialog_title_game_import),
        expanded = expanded,
        onConfirm = { jsonImportResult.launch() },
        confirmText = stringResource(id = R.string.dialog_positive_import),
        onCancel = onCancel,
        cancelText = stringResource(id = R.string.dialog_negative_cancel)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            CompactOptionCheckBox(
                text = stringResource(id = R.string.dialog_checkbox_overwrite),
                checked = overwrite,
                onClick = { overwrite = !overwrite }
            )
        }
    }
}
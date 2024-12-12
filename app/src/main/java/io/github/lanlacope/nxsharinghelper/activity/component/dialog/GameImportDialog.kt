package io.github.lanlacope.nxsharinghelper.activity.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.lanlacope.compose.ui.action.option.CompactOptionCheckBox
import io.github.lanlacope.compose.ui.button.layout.FileButton
import io.github.lanlacope.compose.ui.dialog.GrowDialog
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.rememberImportJsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun GameImportDialog(
    expanded: Boolean,
    onConfirm: (overwrite: Boolean, jsonObject: JSONObject) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var overwrite by remember { mutableStateOf(false) }
    var mysetObject: JSONObject? by remember(expanded) { mutableStateOf(null) }

    var isLoading by remember(expanded) { mutableStateOf(false) }
    var isError by remember(expanded) { mutableStateOf(false) }
    var errorText by remember(expanded) { mutableStateOf("") }

    val jsonImportResult = rememberImportJsonResult(
        onSuccess = { jsonObject ->
            scope.launch(Dispatchers.IO) {
                mysetObject = jsonObject
                isLoading = false
            }
        },
        onFailed = {
            mysetObject = null
            isLoading = false
            isError = false
            errorText = context.getString(R.string.dialog_inport_warning_failed)
        }
    )

    GrowDialog(
        title = stringResource(id = R.string.dialog_title_game_import),
        expanded = expanded,
        onConfirm = {
            if (mysetObject != null) {
                onConfirm(overwrite, mysetObject!!)
            } else {
                isError = true
                errorText = context.getString(R.string.dialog_inport_warning_unselected)
            }
        },
        confirmText = stringResource(id = R.string.dialog_positive_import),
        onCancel = onCancel,
        cancelText = stringResource(id = R.string.dialog_negative_cancel)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            FileButton(
                text = stringResource(id = R.string.dialog_button_file),
                onClick = {
                    isLoading = true
                    jsonImportResult.launch()
                },
                errorText = errorText,
                isError = isError,
                isLoading = isLoading,
                modifier = Modifier.padding(8.dp)
            )

            CompactOptionCheckBox(
                text = stringResource(id = R.string.dialog_checkbox_overwrite),
                checked = overwrite,
                onClick = { overwrite = !overwrite },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
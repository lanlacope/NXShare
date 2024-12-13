package io.github.lanlacope.nxsharinghelper.activity.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import io.github.lanlacope.compose.ui.button.layout.FileButton
import io.github.lanlacope.compose.ui.dialog.GrowDialog
import io.github.lanlacope.compose.ui.text.input.OutlinedInputTextField
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.rememberImportJsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun MySetImportDialog(
    expanded: Boolean,
    onConfirm: (title: String, jsonObject: JSONObject) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember(expanded) { mutableStateOf("") }
    var mysetObject: JSONObject? by remember(expanded) { mutableStateOf(null) }

    var buttonText by remember(expanded) { mutableStateOf(context.getString(R.string.dialog_button_file)) }
    var isLoading by remember(expanded) { mutableStateOf(false) }
    var isError by remember(expanded) { mutableStateOf(false) }
    var errorText by remember(expanded) { mutableStateOf("") }

    val jsonImportResult = rememberImportJsonResult(
        onSuccess = { jsonObject ->
            mysetObject = jsonObject
            isLoading = false
            isError = false
            buttonText = context.getString(R.string.dialog_button_file_selected)
        },
        onFailed = {
            mysetObject = null
            isLoading = false
            isError = true
            errorText = context.getString(R.string.dialog_inport_warning_failed)
            buttonText = context.getString(R.string.dialog_button_file)
        }
    )

    GrowDialog(
        title = stringResource(id = R.string.dialog_title_myset_import),
        expanded = expanded,
        onConfirm = {
            if (mysetObject != null) {
                onConfirm(title, mysetObject!!)
            } else {
                isError = true
                errorText = context.getString(R.string.dialog_inport_warning_unselected)
            }
        },
        confirmText = stringResource(id = R.string.dialog_positive_import),
        onCancel = onCancel,
        cancelText = stringResource(id = R.string.dialog_negative_cancel)
    ) {
        Column(modifier = Modifier.wrapContentHeight()) {

            FileButton(
                text = stringResource(id = R.string.dialog_button_file),
                onClick = {
                    isLoading = true
                    scope.launch(Dispatchers.IO) {
                        jsonImportResult.launch()
                    }
                },
                errorText = errorText,
                isError = isError,
                isLoading = isLoading,
                modifier = Modifier.padding(8.dp)
            )

            OutlinedInputTextField(
                text = title,
                onTextChange = { title = it },
                hintText = buttonText,
                singleLine = true,
                useLabel = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            )
        }
    }
}
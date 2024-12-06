package io.github.lanlacope.nxsharinghelper.activity.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.lanlacope.compose.ui.dialog.GrowDialog
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.DialogTextField
import io.github.lanlacope.nxsharinghelper.activity.component.rememberImportJsonResult
import org.json.JSONObject

@Composable
fun MySetImportDialog(
    expanded: Boolean,
    onConfirm: (title: String, jsonObject: JSONObject) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember(expanded) { mutableStateOf("") }

    val jsonImportResult = rememberImportJsonResult { jsonObject ->
        onConfirm(title, jsonObject)
    }

    GrowDialog(
        title = stringResource(id = R.string.dialog_title_myset_import),
        expanded = expanded,
        onConfirm = { jsonImportResult.launch() },
        confirmText = stringResource(id = R.string.dialog_positive_import),
        onCancel = onCancel,
        cancelText = stringResource(id = R.string.dialog_negative_cancel)
    ) {
        Column(modifier = Modifier.wrapContentHeight()) {

            DialogTextField(
                text = title,
                onTextChange = { title = it },
                hint = stringResource(id = R.string.hint_myset_title),
                eroorText = stringResource(id = R.string.dialog_warning_exists),
                singleLine = true,
                useLabel = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            )
        }
    }
}
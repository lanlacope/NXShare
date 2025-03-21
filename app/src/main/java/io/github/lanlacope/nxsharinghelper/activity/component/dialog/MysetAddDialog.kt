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
import io.github.lanlacope.rewheel.ui.dialog.GrowDialog
import io.github.lanlacope.rewheel.ui.text.input.OutlinedInputTextField
import io.github.lanlacope.nxsharinghelper.R

@Composable
fun MysetAddDialog(
    expanded: Boolean,
    onConfirm: (title: String) -> Unit,
    onCancel: () -> Unit,
    isError: Boolean,
) {
    var title by remember(expanded) { mutableStateOf("") }

    GrowDialog(
        title = stringResource(id = R.string.dialog_title_myset_add),
        expanded = expanded,
        onConfirm = { onConfirm(title) },
        confirmText = stringResource(id = R.string.dialog_positive_add),
        onCancel = onCancel,
        cancelText = stringResource(id = R.string.dialog_negative_cancel)
    ) {
        Column(modifier = Modifier.wrapContentHeight()) {

            OutlinedInputTextField(
                text = title,
                onTextChange = { title = it },
                hintText = stringResource(id = R.string.hint_myset_title),
                errorText = stringResource(id = R.string.dialog_input_warning_exists),
                singleLine = true,
                useLabel = true,
                isError = isError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            )
        }
    }
}
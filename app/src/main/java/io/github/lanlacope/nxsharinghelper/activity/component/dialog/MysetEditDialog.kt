package io.github.lanlacope.nxsharinghelper.activity.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.lanlacope.compose.ui.dialog.GrowDialog
import io.github.lanlacope.compose.ui.text.input.OutlinedInputTextField
import io.github.lanlacope.nxsharinghelper.R

@Composable
fun MySetEditDialog(
    expanded: Boolean,
    title: String,
    headText: String,
    tailText: String,
    onConfirm: (title: String, headText: String, tailText: String) -> Unit,
    onCancel: () -> Unit,
) {
    var dTitle by rememberSaveable(expanded) { mutableStateOf(title) }
    var dHeadText by rememberSaveable(expanded) { mutableStateOf(headText) }
    var dTailText by rememberSaveable(expanded) { mutableStateOf(tailText) }

    GrowDialog(
        title = stringResource(id = R.string.dialog_title_myset_edit),
        expanded = expanded,
        onConfirm = { onConfirm(dTitle, dHeadText, dTailText) },
        confirmText = stringResource(id = R.string.dialog_positive_apply),
        onCancel = onCancel,
        cancelText = stringResource(id = R.string.dialog_negative_cancel)
    ) {
        Column(modifier = Modifier.wrapContentHeight()) {

            OutlinedInputTextField(
                text = title,
                onTextChange = { dTitle = it },
                hintText = stringResource(id = R.string.hint_myset_title),
                errorText = stringResource(id = R.string.dialog_input_warning_exists),
                singleLine = true,
                useLabel = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            )

            OutlinedInputTextField(
                text = dHeadText,
                onTextChange = { dHeadText = it },
                hintText = stringResource(id = R.string.hint_myset_prefix),
                useLabel = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = false)
                    .padding(all = 8.dp)
            )

            OutlinedInputTextField(
                text = dTailText,
                onTextChange = { dTailText = it },
                hintText = stringResource(id = R.string.hint_myset_suffix),
                useLabel = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = false)
                    .padding(all = 8.dp)
            )
        }
    }
}
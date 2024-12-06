package io.github.lanlacope.nxsharinghelper.activity.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.lanlacope.compose.ui.dialog.GrowDialog
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.DialogTextField

@Composable
fun GameAddDialog(
    expanded: Boolean,
    onConfirm: (id: String, title: String, text: String) -> Unit,
    onCancel: () -> Unit,
    isError: Boolean
) {
    var title by rememberSaveable(expanded) { mutableStateOf("") }
    var id by rememberSaveable(expanded) { mutableStateOf("") }
    var text by rememberSaveable(expanded) { mutableStateOf("") }

    GrowDialog(
        title = stringResource(id = R.string.dialog_title_game_add),
        expanded = expanded,
        onConfirm = { onConfirm(id, title, text) },
        confirmText = stringResource(id = R.string.dialog_positive_add),
        onCancel = onCancel,
        cancelText = stringResource(id = R.string.dialog_negative_cancel)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            DialogTextField(
                text = id,
                onTextChange = { id = it },
                hint = stringResource(id = R.string.hint_game_id),
                eroorText = stringResource(id = R.string.dialog_warning_exists),
                singleLine = true,
                useLabel = true,
                isError = isError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            )

            DialogTextField(
                text = title,
                onTextChange = { title = it },
                hint = stringResource(id = R.string.hint_game_title),
                singleLine = true,
                useLabel = true,
                isError = isError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            )

            DialogTextField(
                text = text,
                onTextChange = { text = it },
                hint = stringResource(id = R.string.hint_game_text),
                useLabel = true,
                isError = isError,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(all = 8.dp)
            )
        }
    }
}
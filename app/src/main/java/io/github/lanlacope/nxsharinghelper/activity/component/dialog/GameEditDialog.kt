package io.github.lanlacope.nxsharinghelper.activity.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.lanlacope.compose.composeable.ui.click.BoxButton
import io.github.lanlacope.compose.ui.button.combined.CombinedBoxButton
import io.github.lanlacope.compose.ui.dialog.GrowDialog
import io.github.lanlacope.compose.ui.text.input.OutlinedInputTextField
import io.github.lanlacope.nxsharinghelper.R

@Composable
fun GameEditDialog(
    expanded: Boolean,
    id: String,
    title: String,
    text: String,
    onConfirm: (title: String, text: String) -> Unit,
    onCancel: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current

    var dTitle by rememberSaveable(expanded) { mutableStateOf(title) }
    var dText by rememberSaveable(expanded) { mutableStateOf(text) }

    GrowDialog(
        title = stringResource(id = R.string.dialog_title_game_edit),
        expanded = expanded,
        onConfirm = { onConfirm(dTitle, dText) },
        confirmText = stringResource(id = R.string.dialog_positive_apply),
        onCancel = onCancel,
        cancelText = stringResource(id = R.string.dialog_negative_cancel)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            CombinedBoxButton(
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(id))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Start)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = id,
                    maxLines = 1,
                    modifier = Modifier.wrapContentSize()
                )
            }

            OutlinedInputTextField(
                text = title,
                onTextChange = { dTitle = it },
                hintText = stringResource(id = R.string.hint_game_title),
                singleLine = true,
                useLabel = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            )

            OutlinedInputTextField(
                text = dText,
                onTextChange = { dText = it },
                hintText = stringResource(id = R.string.hint_game_text),
                useLabel = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = false)
                    .padding(all = 8.dp)
            )
        }
    }
}
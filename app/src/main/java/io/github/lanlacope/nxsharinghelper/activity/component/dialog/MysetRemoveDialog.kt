package io.github.lanlacope.nxsharinghelper.activity.component.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lanlacope.compose.ui.dialog.BasicDialog
import io.github.lanlacope.nxsharinghelper.R

@Composable
fun MysetRemoveDialog(
    expanded: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    BasicDialog(
        title = stringResource(id = R.string.dialog_title_comfirm),
        expanded = expanded,
        onConfirm = onConfirm,
        confirmText = stringResource(id = R.string.dialog_positive_apply),
        onCancel = onCancel,
        cancelText = stringResource(id = R.string.dialog_negative_cancel),
    ) {
        Text(
            text = stringResource(id = R.string.confirm_text_myset_remove),
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)

        )
    }
}